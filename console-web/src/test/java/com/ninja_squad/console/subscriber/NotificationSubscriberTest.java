package com.ninja_squad.console.subscriber;

import com.google.common.collect.Lists;
import com.mongodb.Mongo;
import com.ninja_squad.console.Notification;
import com.ninja_squad.console.model.Message;
import com.ninja_squad.console.model.Statistic;
import com.ninja_squad.console.model.TimeUnit;
import com.ninja_squad.console.repository.MessageRepository;
import com.ninja_squad.console.repository.StatisticRepository;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.extractProperty;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class NotificationSubscriberTest {

    static int port = 27016;
    private static MongodProcess mongod;

    @Inject
    private MessageRepository messageRepository;

    @Inject
    private StatisticRepository statisticRepository;

    private NotificationSubscriber subscriber;

    @Configuration
    @EnableMongoRepositories(value = "com.ninja_squad.console.repository", repositoryImplementationPostfix = "CustomDefault")
    static class ContextConfiguration extends AbstractMongoConfiguration {

        @Override
        protected String getDatabaseName() {
            return "test";
        }

        @Override
        public Mongo mongo() throws Exception {
            return new Mongo("localhost", port);
        }

    }

    @BeforeClass
    public static void setUpDatabase() throws Exception {
        MongodConfig mongodConfig = new MongodConfig(Version.Main.V2_0, port, Network.localhostIsIPv6());
        MongodStarter runtime = MongodStarter.getDefaultInstance();
        MongodExecutable mongodExecutable = runtime.prepare(mongodConfig);
        mongod = mongodExecutable.start();
    }

    @AfterClass
    public static void tearDownDatabase() throws Exception {
        if (mongod != null) { mongod.stop(); }
    }

    @Before
    public void setUp() throws Exception {
        messageRepository.deleteAll();
        statisticRepository.deleteAll();
        subscriber = new NotificationSubscriber();
        subscriber.setMessageRepository(messageRepository);
        subscriber.setStatisticRepository(statisticRepository);
    }

    private Message createMessage(String id, DateTime time, boolean handled) {
        Message message = new Message();
        message.setId(id);
        message.setTimestamp(time.getMillis());
        if (handled) { message.setHandled(true); }
        return messageRepository.save(message);
    }

    private Message createMessage(String id, DateTime now, boolean handled, Collection<Notification> notifications) {
        Message message = createMessage(id, now, handled);
        message.setNotifications(notifications);
        return messageRepository.save(message);
    }

    @Test
    public void getPendingNotificationsShouldReturnThePendingNotifications() throws Exception {
        // given 2 notifications handled among 5
        createMessage("1", DateTime.now(), true);
        createMessage("2", DateTime.now(), false);
        createMessage("3", DateTime.now(), true);
        createMessage("4", DateTime.now(), false);
        createMessage("5", DateTime.now(), false);

        // when
        List<Message> messages = subscriber.getPendingNotifications();

        // then should return the most recent handled notification
        assertThat(messages).hasSize(3);
        assertThat(extractProperty("id").from(messages)).containsExactly("2", "4", "5");
    }

    @Test
    public void getOrderedStepsShouldReturnNotificationsOrdered() throws Exception {
        // given a message with 3 steps
        Message message = createMessage("1", DateTime.now(), true);
        Notification notification0 = new Notification();
        notification0.setStep(0);
        Notification notification1 = new Notification();
        notification1.setStep(1);
        Notification notification2 = new Notification();
        notification2.setStep(2);
        message.setNotifications(Lists.newArrayList(notification1, notification2, notification0));

        // when ordering
        List<Notification> orderedSteps = subscriber.getOrderedSteps(message);

        // then should be sorted
        assertThat(orderedSteps).containsSequence(notification0, notification1, notification2);
    }

    @Test
    public void computeDurationShouldComputeGlobalMessageTime() throws Exception {
        // given a message of 2 seconds
        DateTime now = DateTime.now();
        Message message = createMessage("1", now, true);
        Notification notification0 = new Notification();
        notification0.setStep(0);
        notification0.setTimestamp(now.minusSeconds(2).getMillis());
        Notification notification1 = new Notification();
        notification1.setStep(1);
        notification1.setTimestamp(now.minusSeconds(1).getMillis());
        Notification notification2 = new Notification();
        notification2.setStep(2);
        message.setNotifications(Lists.newArrayList(notification1, notification2, notification0));

        // when compute time
        long time = subscriber.computeDuration(message);

        // then should be 2000
        assertThat(time).isEqualTo(2000);
    }

    @Test
    public void updateMessagesPerSecondShouldCreateStats() throws Exception {
        // given a message completed in 200ms
        long now = 1351523921246L;

        // when updateMessagesPerSecond
        Statistic statistic = subscriber.updateMessagesPer(TimeUnit.SECOND, now, 200, false);

        // then should be 1 message in range with 200 everywhere
        assertThat(statistic).isEqualTo(new Statistic(1351523921000L, TimeUnit.SECOND, 0, 1, 200, 200, 200));
    }

    @Test
    public void updateMessagesPerSecondShouldUpdateStats() throws Exception {
        // given a message completed in 200ms in the same range than a previous one
        long now = 1351523921246L;
        statisticRepository.save(new Statistic(1351523921000L, TimeUnit.SECOND, 0, 1, 100, 100, 100));

        // when updateMessagesPerSecond
        Statistic statistic = subscriber.updateMessagesPer(TimeUnit.SECOND, now, 200, false);

        // then should be 2 messages in the range with update min and average
        assertThat(statistic).isEqualTo(new Statistic(1351523921000L, TimeUnit.SECOND, 0, 2, 100, 200, 150));
    }

    @Test
    public void updateMessagesPerSecondShouldUpdateStatsIfFailed() throws Exception {
        // given a message completed in 200ms in the same range than a previous one
        long now = 1351523921246L;
        statisticRepository.save(new Statistic(1351523921000L, TimeUnit.SECOND, 0, 1, 100, 100, 100));

        // when updateMessagesPerSecond
        Statistic statistic = subscriber.updateMessagesPer(TimeUnit.SECOND, now, 200, true);

        // then should be 2 messages in the range with update min and average
        assertThat(statistic).isEqualTo(new Statistic(1351523921000L, TimeUnit.SECOND, 1, 1, 100, 100, 100));
    }

    @Test
    public void subscribeShouldHandlePendingNotifsAndUpdateStats() throws Exception {
        // given 3 pending notifications 2 in the same second, all in the same minute
        DateTime now = new DateTime(2012, 10, 31, 16, 0, 0, 432, DateTimeZone.UTC);
        createMessage("1", now, true);
        Notification notification0 = new Notification();
        notification0.setStep(0);
        notification0.setTimestamp(now.minusMillis(200).getMillis());
        createMessage("2", now, false, Lists.newArrayList(notification0));
        createMessage("3", now, true);
        notification0 = new Notification();
        notification0.setStep(0);
        notification0.setTimestamp(now.minusMillis(100).getMillis());
        createMessage("4", now, false, Lists.newArrayList(notification0));
        now = new DateTime(2012, 10, 31, 16, 0, 30, 232, DateTimeZone.UTC);
        notification0 = new Notification();
        notification0.setStep(0);
        notification0.setTimestamp(now.minusMillis(300).getMillis());
        createMessage("5", now, false, Lists.newArrayList(notification0));

        // when subscribing (already started as it's a @PostConstruct method)
        subscriber.subscribe();
        Thread.sleep(2000);

        // then we should have a new Statistic for each time unit and 2 for SECOND
        Sort sort = new Sort(Sort.Direction.DESC, "timestamp");
        List<Statistic> all = statisticRepository.findAll(sort);
        assertThat(all).hasSize(TimeUnit.values().length + 1);
        Statistic statistic = all.get(0);
        long millis = new DateTime(2012, 10, 31, 16, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(statistic).isEqualTo(new Statistic(millis, TimeUnit.SECOND, 0, 2, 100, 200, 150));
        statistic = all.get(1);
        assertThat(statistic).isEqualTo(new Statistic(millis, TimeUnit.MINUTE, 0, 3, 100, 300, 200));
        statistic = all.get(2);
        assertThat(statistic).isEqualTo(new Statistic(millis, TimeUnit.HOUR, 0, 3, 100, 300, 200));
        statistic = all.get(3);
        millis = new DateTime(2012, 10, 31, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(statistic).isEqualTo(new Statistic(millis, TimeUnit.DAY, 0, 3, 100, 300, 200));
        statistic = all.get(4);
        millis = new DateTime(2012, 10, 28, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(statistic).isEqualTo(new Statistic(millis, TimeUnit.WEEK, 0, 3, 100, 300, 200));
        statistic = all.get(5);
        millis = new DateTime(2012, 10, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(statistic).isEqualTo(new Statistic(millis, TimeUnit.MONTH, 0, 3, 100, 300, 200));
        statistic = all.get(6);
        millis = new DateTime(2012, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(statistic).isEqualTo(new Statistic(millis, TimeUnit.YEAR, 0, 3, 100, 300, 200));
        statistic = all.get(7);
        millis = new DateTime(2012, 10, 31, 16, 0, 30, 0, DateTimeZone.UTC).getMillis();
        assertThat(statistic).isEqualTo(new Statistic(millis, TimeUnit.SECOND, 0, 1, 300, 300, 300));
        // no more pending notifications
        assertThat(messageRepository.findByHandledExistsOrderByTimestampAsc(false)).hasSize(0);

    }
}
