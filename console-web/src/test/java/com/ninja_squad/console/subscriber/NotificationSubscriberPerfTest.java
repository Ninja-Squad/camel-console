package com.ninja_squad.console.subscriber;


import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.mongodb.Mongo;
import com.mongodb.WriteResult;
import com.ninja_squad.console.model.RouteStatistic;
import com.ninja_squad.console.repository.ExchangeStatRepository;
import com.ninja_squad.console.repository.RouteStatisticRepository;
import com.ninja_squad.console.repository.StatisticRepository;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.inject.Inject;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
@Slf4j
public class NotificationSubscriberPerfTest {

    @Rule
    public MethodRule benchmarkRun = new BenchmarkRule();

    static int port = 27017;

    @Inject
    @Setter
    private ExchangeStatRepository exchangeStatRepository;

    @Inject
    @Setter
    private StatisticRepository statisticRepository;

    @Inject
    @Setter
    private RouteStatisticRepository routeStatisticRepository;

    private NotificationSubscriber subscriber;

    @Inject
    private MongoTemplate mongoTemplate;

    @Configuration
    @EnableMongoRepositories(value = "com.ninja_squad.console.repository", repositoryImplementationPostfix = "CustomDefault")
    static class ContextConfiguration extends AbstractMongoConfiguration {

        @Override
        protected String getDatabaseName() {
            return "console";
        }

        @Override
        public Mongo mongo() throws Exception {
            return new Mongo("localhost", port);
        }
    }

    @Before
    public void setUp() throws Exception {
        // reset handled
        WriteResult result = mongoTemplate.updateMulti(new Query(where("handled").exists(true)),
                new Update().unset("handled"),
                RouteStatistic.class);

        log.info(result.getN() + " stats");

        exchangeStatRepository.deleteAll();
        statisticRepository.deleteAll();
        subscriber = new NotificationSubscriber();
        subscriber.setExchangeStatRepository(exchangeStatRepository);
        subscriber.setStatisticRepository(statisticRepository);
        subscriber.setRouteStatisticRepository(routeStatisticRepository);
    }

    @Test
    public void shouldHandlePendingStats() throws Exception {
        subscriber.pendingRouteStats();
    }
}
