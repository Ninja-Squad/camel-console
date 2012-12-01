package com.ninja_squad.console.repository;

import com.google.common.collect.Lists;
import com.mongodb.Mongo;
import com.ninja_squad.console.model.Statistic;
import com.ninja_squad.console.model.TimeUnit;
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
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.inject.Inject;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class StatisticRepositoryTest {

    public static final String ROUTE_1 = "route1";

    static int port = 27016;
    private static MongodProcess mongod;

    @Inject
    private StatisticRepository statisticRepository;

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
        statisticRepository.deleteAll();
    }

    @Test
    public void shouldAggregateStats() throws Exception {
        // given a stats from january to september
        storeStats();

        // when aggregating stats
        long january = new DateTime(2012, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        long october = new DateTime(2012, 10, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        Statistic stat = statisticRepository.aggregateStatistics(ROUTE_1, january, october);

        // then should be aggregated
        assertThat(stat.getRange()).isEqualTo(january);
        assertThat(stat.getElementId()).isEqualTo(ROUTE_1);
        assertThat(stat.getCompleted()).isEqualTo(10000);
        assertThat(stat.getFailed()).isEqualTo(270);
        assertThat(stat.getMin()).isEqualTo(30);
        assertThat(stat.getMax()).isEqualTo(450);
        assertThat(stat.getAverage()).isEqualTo((200 * 2000 + 150 * 8000) / 10000);
    }

    @Test
    public void shouldAggregateStatsConsideringTheUpperBound() throws Exception {
        // given a stats from january to september
        storeStats();

        // when aggregating stats
        long january = new DateTime(2012, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        long september = new DateTime(2012, 9, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        Statistic stat = statisticRepository.aggregateStatistics(ROUTE_1, january, september);

        // then should be aggregated
        assertThat(stat.getRange()).isEqualTo(january);
        assertThat(stat.getElementId()).isEqualTo(ROUTE_1);
        assertThat(stat.getCompleted()).isEqualTo(7000);
        assertThat(stat.getFailed()).isEqualTo(170);
        assertThat(stat.getMin()).isEqualTo(30);
        assertThat(stat.getMax()).isEqualTo(450);
        assertThat(stat.getAverage()).isEqualTo((200 * 2000 + 150 * 5000) / 7000);
    }

    @Test
    public void shouldAggregateStatsConsideringTheLowerBound() throws Exception {
        // given a stats from january to september
        storeStats();

        // when aggregating stats
        long february = new DateTime(2012, 2, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        long september = new DateTime(2012, 9, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        Statistic stat = statisticRepository.aggregateStatistics(ROUTE_1, february, september);

        // then should be aggregated
        assertThat(stat.getRange()).isEqualTo(february);
        assertThat(stat.getElementId()).isEqualTo(ROUTE_1);
        assertThat(stat.getCompleted()).isEqualTo(6000);
        assertThat(stat.getFailed()).isEqualTo(160);
        assertThat(stat.getMin()).isEqualTo(30);
        assertThat(stat.getMax()).isEqualTo(400);
        assertThat(stat.getAverage()).isEqualTo((200 * 1000 + 150 * 5000) / 6000);
    }

    private void storeStats() {
        long january = new DateTime(2012, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        Statistic januaryStats = new Statistic(ROUTE_1, january, TimeUnit.MONTH, 10, 1000, 100, 450, 200);
        long march = new DateTime(2012, 3, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        Statistic marchStats = new Statistic(ROUTE_1, march, TimeUnit.MONTH, 10, 1000, 100, 400, 200);
        long june = new DateTime(2012, 6, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        Statistic juneStats = new Statistic(ROUTE_1, june, TimeUnit.MONTH, 50, 2000, 30, 300, 150);
        long september = new DateTime(2012, 9, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        Statistic septemberStats = new Statistic(ROUTE_1, september, TimeUnit.MONTH, 100, 3000, 50, 200, 150);
        long october = new DateTime(2012, 10, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        Statistic octoberStats = new Statistic(ROUTE_1, october, TimeUnit.MONTH, 100, 3000, 50, 200, 150);
        statisticRepository.save(Lists.newArrayList(januaryStats, marchStats, juneStats, septemberStats, octoberStats));
    }

}
