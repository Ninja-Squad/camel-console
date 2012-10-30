package com.ninja_squad.console.subscriber;

import com.google.common.collect.Lists;
import com.mongodb.Mongo;
import com.ninja_squad.console.Notification;
import com.ninja_squad.console.model.Message;
import com.ninja_squad.console.model.TimeUnit;
import com.ninja_squad.console.repository.MessageRepository;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.extractProperty;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class NotificationSubscriberTest {

    static int port = 27016;
    private static MongodProcess mongod;

    @Inject
    @Named("messageRepository")
    private MessageRepository repository;

    @Inject
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

        @Bean
        public NotificationSubscriber subscriber() {
            return new NotificationSubscriber();
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
        if (mongod != null) mongod.stop();
    }

    @Before
    public void setUp() throws Exception {
        repository.deleteAll();
    }

    private Message createMessage(String id, DateTime time, boolean handled) {
        Message message = new Message();
        message.setId(id);
        message.setTimestamp(time.getMillis());
        message.setHandled(handled);
        message = repository.save(message);
        return message;
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
    public void getRoundedTimestampShouldReturnRoundedTimestamp() throws Exception {
        // given a timestamp
        long timestamp = 1351523921246L;

        // when rounding
        long roundedSeconds = subscriber.getRoundedTimestamp(timestamp, TimeUnit.SECONDS);
        long roundedMinutes = subscriber.getRoundedTimestamp(timestamp, TimeUnit.MINUTES);
        long roundedHours = subscriber.getRoundedTimestamp(timestamp, TimeUnit.HOURS);
        long roundedDays = subscriber.getRoundedTimestamp(timestamp, TimeUnit.DAYS);
        long roundedWeeks = subscriber.getRoundedTimestamp(timestamp, TimeUnit.WEEKS);
        long roundedMonths = subscriber.getRoundedTimestamp(timestamp, TimeUnit.MONTHS);
        long roundedYears = subscriber.getRoundedTimestamp(timestamp, TimeUnit.YEARS);

        // then should be rounded in seconds
        assertThat(roundedSeconds).isEqualTo(1351523921000L);
        assertThat(roundedMinutes).isEqualTo(1351523880000L);
        assertThat(roundedHours).isEqualTo(1351522800000L);
        assertThat(roundedDays).isEqualTo(1351465200000L);
        assertThat(roundedWeeks).isEqualTo(1351375200000L);
        assertThat(roundedMonths).isEqualTo(1349042400000L);
        assertThat(roundedYears).isEqualTo(1325372400000L);
    }
}
