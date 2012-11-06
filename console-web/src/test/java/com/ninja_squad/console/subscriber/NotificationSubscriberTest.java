package com.ninja_squad.console.subscriber;

import com.google.common.collect.Lists;
import com.mongodb.Mongo;
import com.ninja_squad.console.StepStatistic;
import com.ninja_squad.console.model.ExchangeStatistic;
import com.ninja_squad.console.model.RouteStatistic;
import com.ninja_squad.console.model.Statistic;
import com.ninja_squad.console.model.TimeUnit;
import com.ninja_squad.console.repository.ExchangeStatRepository;
import com.ninja_squad.console.repository.RouteStatisticRepository;
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
    private ExchangeStatRepository exchangeStatRepository;

    @Inject
    private StatisticRepository statisticRepository;

    @Inject
    private RouteStatisticRepository routeStatisticRepository;

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
        exchangeStatRepository.deleteAll();
        statisticRepository.deleteAll();
        subscriber = new NotificationSubscriber();
        subscriber.setExchangeStatRepository(exchangeStatRepository);
        subscriber.setStatisticRepository(statisticRepository);
        subscriber.setRouteStatisticRepository(routeStatisticRepository);
    }

    private ExchangeStatistic createExchangeStatistic(String id, DateTime time, boolean handled) {
        ExchangeStatistic exchangeStatistic = new ExchangeStatistic();
        exchangeStatistic.setId(id);
        exchangeStatistic.setTimestamp(time.getMillis());
        if (handled) { exchangeStatistic.setHandled(true); }
        return exchangeStatRepository.save(exchangeStatistic);
    }

    private ExchangeStatistic createExchangeStatistic(String id, DateTime now, boolean handled, Collection<StepStatistic> stepStatistics) {
        ExchangeStatistic exchangeStatistic = createExchangeStatistic(id, now, handled);
        exchangeStatistic.setSteps(stepStatistics);
        return exchangeStatRepository.save(exchangeStatistic);
    }

    private RouteStatistic createRouteStatistic(String exchangeId, String routeId, DateTime timestamp, int duration, boolean handled) {
        RouteStatistic routeStatistic = new RouteStatistic();
        routeStatistic.setExchangeId(exchangeId);
        routeStatistic.setRouteId(routeId);
        routeStatistic.setTimestamp(timestamp.getMillis());
        routeStatistic.setDuration(duration);
        if (handled) { routeStatistic.setHandled(true); }
        return routeStatisticRepository.save(routeStatistic);
    }

    @Test
    public void getPendingstepStatisticsShouldReturnThePendingExchangeStats() throws Exception {
        // given 2 stepStatistics handled among 5
        createExchangeStatistic("1", DateTime.now(), true);
        createExchangeStatistic("2", DateTime.now(), false);
        createExchangeStatistic("3", DateTime.now(), true);
        createExchangeStatistic("4", DateTime.now(), false);
        createExchangeStatistic("5", DateTime.now(), false);

        // when
        List<ExchangeStatistic> pendingExchangeStats = subscriber.getPendingExchangeStats();

        // then should return the most recent handled stepStatistic
        assertThat(pendingExchangeStats).hasSize(3);
        assertThat(extractProperty("id").from(pendingExchangeStats)).containsExactly("2", "4", "5");
    }

    @Test
    public void getOrderedStepsShouldReturnStepStatsOrdered() throws Exception {
        // given a exchangeStatistic with 3 steps
        ExchangeStatistic exchangeStatistic = createExchangeStatistic("1", DateTime.now(), true);
        StepStatistic stepStatistic0 = new StepStatistic();
        stepStatistic0.setStep(0);
        StepStatistic stepStatistic1 = new StepStatistic();
        stepStatistic1.setStep(1);
        StepStatistic stepStatistic2 = new StepStatistic();
        stepStatistic2.setStep(2);
        exchangeStatistic.setSteps(Lists.newArrayList(stepStatistic1, stepStatistic2, stepStatistic0));

        // when ordering
        List<StepStatistic> orderedSteps = subscriber.getOrderedSteps(exchangeStatistic);

        // then should be sorted
        assertThat(orderedSteps).containsSequence(stepStatistic0, stepStatistic1, stepStatistic2);
    }

    @Test
    public void computeDurationShouldComputeGlobalMessageTime() throws Exception {
        // given a message of 2 seconds
        DateTime now = DateTime.now();
        ExchangeStatistic message = createExchangeStatistic("1", now, true);
        StepStatistic stepStatistic0 = new StepStatistic();
        stepStatistic0.setStep(0);
        stepStatistic0.setTimestamp(now.minusSeconds(2).getMillis());
        StepStatistic stepStatistic1 = new StepStatistic();
        stepStatistic1.setStep(1);
        stepStatistic1.setTimestamp(now.minusSeconds(1).getMillis());
        StepStatistic stepStatistic2 = new StepStatistic();
        stepStatistic2.setStep(2);
        message.setSteps(Lists.newArrayList(stepStatistic1, stepStatistic2, stepStatistic0));

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
        Statistic statistic = subscriber.updateStatisticForElement("route1", TimeUnit.SECOND, now, 200, false);

        // then should be 1 message in range with 200 everywhere
        assertThat(statistic).isEqualTo(new Statistic("route1", 1351523921000L, TimeUnit.SECOND, 0, 1, 200, 200, 200));
    }

    @Test
    public void updateMessagesPerSecondShouldUpdateStats() throws Exception {
        // given a message completed in 200ms in the same range than a previous one
        long now = 1351523921246L;
        statisticRepository.save(new Statistic("route1", 1351523921000L, TimeUnit.SECOND, 0, 1, 100, 100, 100));

        // when updateMessagesPerSecond
        Statistic statistic = subscriber.updateStatisticForElement("route1", TimeUnit.SECOND, now, 200, false);

        // then should be 2 messages in the range with update min and average
        assertThat(statistic).isEqualTo(new Statistic("route1", 1351523921000L, TimeUnit.SECOND, 0, 2, 100, 200, 150));
    }

    @Test
    public void updateMessagesPerSecondShouldUpdateStatsIfFailed() throws Exception {
        // given a message completed in 200ms in the same range than a previous one
        long now = 1351523921246L;
        statisticRepository.save(new Statistic("route1", 1351523921000L, TimeUnit.SECOND, 0, 1, 100, 100, 100));

        // when updateMessagesPerSecond
        Statistic statistic = subscriber.updateStatisticForElement("route1", TimeUnit.SECOND, now, 200, true);

        // then should be 2 messages in the range with update min and average
        assertThat(statistic).isEqualTo(new Statistic("route1", 1351523921000L, TimeUnit.SECOND, 1, 1, 100, 100, 100));
    }

    @Test
    public void subscribeShouldHandlePendingExchangeStatsAndUpdateStats() throws Exception {
        // given 3 pending stepStatistics 2 in the same second, all in the same minute
        DateTime now = new DateTime(2012, 10, 31, 16, 0, 0, 432, DateTimeZone.UTC);
        createExchangeStatistic("1", now, true);
        StepStatistic stepStatistic0 = new StepStatistic();
        stepStatistic0.setStep(0);
        stepStatistic0.setDestination("mock:step0");
        stepStatistic0.setDuration(200);
        stepStatistic0.setTimestamp(now.minusMillis(200).getMillis());
        createExchangeStatistic("2", now, false, Lists.newArrayList(stepStatistic0));
        createExchangeStatistic("3", now, true);
        stepStatistic0 = new StepStatistic();
        stepStatistic0.setStep(0);
        stepStatistic0.setDestination("mock:step0");
        stepStatistic0.setDuration(100);
        stepStatistic0.setTimestamp(now.minusMillis(100).getMillis());
        createExchangeStatistic("4", now, false, Lists.newArrayList(stepStatistic0));
        now = new DateTime(2012, 10, 31, 16, 0, 30, 232, DateTimeZone.UTC);
        stepStatistic0 = new StepStatistic();
        stepStatistic0.setStep(0);
        stepStatistic0.setDestination("mock:step0");
        stepStatistic0.setTimestamp(now.minusMillis(500).getMillis());
        stepStatistic0.setDuration(300);
        StepStatistic stepStatistic1 = new StepStatistic();
        stepStatistic1.setStep(1);
        stepStatistic1.setDestination("mock:step1");
        stepStatistic1.setTimestamp(now.minusMillis(200).getMillis());
        stepStatistic1.setDuration(200);
        createExchangeStatistic("5", now, false, Lists.newArrayList(stepStatistic0, stepStatistic1));

        // when subscribing (already started as it's a @PostConstruct method)
        subscriber.pendingExchangeStats();
        Thread.sleep(2000);

        // then we should have a new Statistic for each 3 elements and for each time unit (and 2 for SECOND)
        Sort sort = new Sort(Sort.Direction.DESC, "elementId", "timestamp");
        List<Statistic> all = statisticRepository.findAll(sort);
        assertThat(all).hasSize(3 * (TimeUnit.values().length) + 2);
        Statistic statistic = all.get(0);
        long millis = new DateTime(2012, 10, 31, 16, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(statistic).isEqualTo(new Statistic(Statistic.ALL, millis, TimeUnit.SECOND, 0, 2, 100, 200, 150));
        statistic = all.get(1);
        assertThat(statistic).isEqualTo(new Statistic(Statistic.ALL, millis, TimeUnit.MINUTE, 0, 3, 100, 500, 266));
        statistic = all.get(2);
        assertThat(statistic).isEqualTo(new Statistic(Statistic.ALL, millis, TimeUnit.HOUR, 0, 3, 100, 500, 266));
        statistic = all.get(3);
        millis = new DateTime(2012, 10, 31, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(statistic).isEqualTo(new Statistic(Statistic.ALL, millis, TimeUnit.DAY, 0, 3, 100, 500, 266));
        statistic = all.get(4);
        millis = new DateTime(2012, 10, 28, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(statistic).isEqualTo(new Statistic(Statistic.ALL, millis, TimeUnit.WEEK, 0, 3, 100, 500, 266));
        statistic = all.get(5);
        millis = new DateTime(2012, 10, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(statistic).isEqualTo(new Statistic(Statistic.ALL, millis, TimeUnit.MONTH, 0, 3, 100, 500, 266));
        statistic = all.get(6);
        millis = new DateTime(2012, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(statistic).isEqualTo(new Statistic(Statistic.ALL, millis, TimeUnit.YEAR, 0, 3, 100, 500, 266));
        statistic = all.get(7);
        millis = new DateTime(2012, 10, 31, 16, 0, 30, 0, DateTimeZone.UTC).getMillis();
        assertThat(statistic).isEqualTo(new Statistic(Statistic.ALL, millis, TimeUnit.SECOND, 0, 1, 500, 500, 500));

        // and same thing mock:step1
        statistic = all.get(8);
        millis = new DateTime(2012, 10, 31, 16, 0, 30, 0, DateTimeZone.UTC).getMillis();
        assertThat(statistic).isEqualTo(new Statistic("mock:step1", millis, TimeUnit.SECOND, 0, 1, 200, 200, 200));
        statistic = all.get(9);
        millis = new DateTime(2012, 10, 31, 16, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(statistic).isEqualTo(new Statistic("mock:step1", millis, TimeUnit.MINUTE, 0, 1, 200, 200, 200));
        statistic = all.get(10);
        assertThat(statistic).isEqualTo(new Statistic("mock:step1", millis, TimeUnit.HOUR, 0, 1, 200, 200, 200));
        statistic = all.get(11);
        millis = new DateTime(2012, 10, 31, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(statistic).isEqualTo(new Statistic("mock:step1", millis, TimeUnit.DAY, 0, 1, 200, 200, 200));
        statistic = all.get(12);
        millis = new DateTime(2012, 10, 28, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(statistic).isEqualTo(new Statistic("mock:step1", millis, TimeUnit.WEEK, 0, 1, 200, 200, 200));
        statistic = all.get(13);
        millis = new DateTime(2012, 10, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(statistic).isEqualTo(new Statistic("mock:step1", millis, TimeUnit.MONTH, 0, 1, 200, 200, 200));
        statistic = all.get(14);
        millis = new DateTime(2012, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(statistic).isEqualTo(new Statistic("mock:step1", millis, TimeUnit.YEAR, 0, 1, 200, 200, 200));

        // and same thing for mock:step0
        statistic = all.get(15);
        millis = new DateTime(2012, 10, 31, 16, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(statistic).isEqualTo(new Statistic("mock:step0", millis, TimeUnit.SECOND, 0, 2, 100, 200, 150));
        statistic = all.get(16);
        assertThat(statistic).isEqualTo(new Statistic("mock:step0", millis, TimeUnit.MINUTE, 0, 3, 100, 300, 200));
        statistic = all.get(17);
        assertThat(statistic).isEqualTo(new Statistic("mock:step0", millis, TimeUnit.HOUR, 0, 3, 100, 300, 200));
        statistic = all.get(18);
        millis = new DateTime(2012, 10, 31, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(statistic).isEqualTo(new Statistic("mock:step0", millis, TimeUnit.DAY, 0, 3, 100, 300, 200));
        statistic = all.get(19);
        millis = new DateTime(2012, 10, 28, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(statistic).isEqualTo(new Statistic("mock:step0", millis, TimeUnit.WEEK, 0, 3, 100, 300, 200));
        statistic = all.get(20);
        millis = new DateTime(2012, 10, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(statistic).isEqualTo(new Statistic("mock:step0", millis, TimeUnit.MONTH, 0, 3, 100, 300, 200));
        statistic = all.get(21);
        millis = new DateTime(2012, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(statistic).isEqualTo(new Statistic("mock:step0", millis, TimeUnit.YEAR, 0, 3, 100, 300, 200));
        statistic = all.get(22);
        millis = new DateTime(2012, 10, 31, 16, 0, 29, 0, DateTimeZone.UTC).getMillis();
        assertThat(statistic).isEqualTo(new Statistic("mock:step0", millis, TimeUnit.SECOND, 0, 1, 300, 300, 300));

        // no more pending stepStatistics
        assertThat(exchangeStatRepository.findByHandledExistsOrderByTimestampAsc(false)).hasSize(0);
    }

    @Test
    public void subscribeShouldHandlePendingRouteStatsAndUpdateStats() throws Exception {
        // given 3 pending stepStatistics 2 in the same second, all in the same minute
        DateTime now = new DateTime(2012, 10, 31, 16, 0, 0, 432, DateTimeZone.UTC);
        createRouteStatistic("1", "route1", now, 0, true);
        createRouteStatistic("2", "route1", now, 200, false);
        createRouteStatistic("3", "route1", now, 0, true);
        createRouteStatistic("4", "route1", now, 100, false);
        now = new DateTime(2012, 10, 31, 16, 0, 30, 232, DateTimeZone.UTC);
        createRouteStatistic("5", "route1", now, 300, false);

        // when subscribing (already started as it's a @PostConstruct method)
        subscriber.pendingRouteStats();
        Thread.sleep(2000);

        // then we should have a new Statistic for each time unit and 2 for SECOND
        Sort sort = new Sort(Sort.Direction.DESC, "timestamp");
        List<Statistic> all = statisticRepository.findAll(sort);
        assertThat(all).hasSize(TimeUnit.values().length + 1);
        Statistic statistic = all.get(0);
        long millis = new DateTime(2012, 10, 31, 16, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(statistic).isEqualTo(new Statistic("route1", millis, TimeUnit.SECOND, 0, 2, 100, 200, 150));
        statistic = all.get(1);
        assertThat(statistic).isEqualTo(new Statistic("route1", millis, TimeUnit.MINUTE, 0, 3, 100, 300, 200));
        statistic = all.get(2);
        assertThat(statistic).isEqualTo(new Statistic("route1", millis, TimeUnit.HOUR, 0, 3, 100, 300, 200));
        statistic = all.get(3);
        millis = new DateTime(2012, 10, 31, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(statistic).isEqualTo(new Statistic("route1", millis, TimeUnit.DAY, 0, 3, 100, 300, 200));
        statistic = all.get(4);
        millis = new DateTime(2012, 10, 28, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(statistic).isEqualTo(new Statistic("route1", millis, TimeUnit.WEEK, 0, 3, 100, 300, 200));
        statistic = all.get(5);
        millis = new DateTime(2012, 10, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(statistic).isEqualTo(new Statistic("route1", millis, TimeUnit.MONTH, 0, 3, 100, 300, 200));
        statistic = all.get(6);
        millis = new DateTime(2012, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(statistic).isEqualTo(new Statistic("route1", millis, TimeUnit.YEAR, 0, 3, 100, 300, 200));
        statistic = all.get(7);
        millis = new DateTime(2012, 10, 31, 16, 0, 30, 0, DateTimeZone.UTC).getMillis();
        assertThat(statistic).isEqualTo(new Statistic("route1", millis, TimeUnit.SECOND, 0, 1, 300, 300, 300));
        // no more pending stepStatistics
        assertThat(exchangeStatRepository.findByHandledExistsOrderByTimestampAsc(false)).hasSize(0);
    }
}
