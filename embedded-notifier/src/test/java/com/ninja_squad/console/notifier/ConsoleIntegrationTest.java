package com.ninja_squad.console.notifier;

import com.mongodb.*;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.apache.camel.management.event.ExchangeCompletedEvent;
import org.apache.camel.management.event.ExchangeFailedEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ConsoleIntegrationTest {

    protected Logger log = LoggerFactory.getLogger(getClass());

    private CamelContext context;
    private MongodProcess mongod;
    private DBCollection exchangeStatistics;
    private DBCollection routes;
    private DBCollection routeStates;
    private DBCollection states;
    private DBCollection routeStatistics;
    private ConsoleEventNotifier notifier;
    private ConsoleLifecycleStrategy consoleLifecycleStrategy;

    @Before
    public void setUp() throws Exception {
        // getting properties
        Properties properties = new Properties();
        properties.load(getClass().getClassLoader().getResourceAsStream("database.properties"));
        String host = properties.getProperty("mongodb.host");
        int port = Integer.parseInt(properties.getProperty("mongodb.port"));

        // setting up mongodb embedded
        MongodConfig mongodConfig = new MongodConfig(Version.Main.V2_0, port, Network.localhostIsIPv6());
        MongodStarter runtime = MongodStarter.getDefaultInstance();
        MongodExecutable mongodExecutable = runtime.prepare(mongodConfig);
        mongod = mongodExecutable.start();
        Mongo mongo = new Mongo(host, port);
        DB db = mongo.getDB("console");
        exchangeStatistics = db.getCollection(ConsoleRepositoryJongo.EXCHANGE_STATISTICS);
        routes = db.getCollection(ConsoleRepositoryJongo.ROUTES);
        routeStates = db.getCollection(ConsoleRepositoryJongo.ROUTE_STATES);
        states = db.getCollection(ConsoleRepositoryJongo.APP_STATES);
        routeStatistics = db.getCollection(ConsoleRepositoryJongo.ROUTE_STATISTICS);

        // setting up notifiers and tracers
        notifier = spy(new ConsoleEventNotifier());
        ConsoleRepositoryJongo repository = new ConsoleRepositoryJongo(host, port);
        consoleLifecycleStrategy = spy(new ConsoleLifecycleStrategy());
        consoleLifecycleStrategy.setRepository(repository);
    }

    @After
    public void tearDown() throws Exception {
        if (mongod != null) {
            mongod.stop();
        }
    }

    private void startCamelApp() throws Exception {
        // start Camel application with two routes
        context = new DefaultCamelContext();

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:route1").routeId("route1").to("mock:result");
            }
        });
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:route2").routeId("route2")//
                        .process(new Processor() {
                            @Override
                            public void process(Exchange exchange) throws Exception {
                                exchange.getOut().setBody("route2 - 2");
                            }

                            @Override
                            public String toString() {
                                return "route2-processor";
                            }
                        })//
                        .to("direct:route1");
            }
        });
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:route3").routeId("route3")//
                        .onException(NullPointerException.class).log(LoggingLevel.ERROR, "NPE").end()//
                        .to("direct:route1").process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        throw new NullPointerException();
                    }
                });
            }
        });
        // add the lifecycle strategy
        context.addLifecycleStrategy(consoleLifecycleStrategy);
        // and the management strategy
        context.getManagementStrategy().addEventNotifier(notifier);
        // add the intercept strategy
        context.addInterceptStrategy(new ConsoleTracer(notifier));

        context.start();
    }

    private void stopCamelApp() {
        try {
            context.stop();
        } catch (Exception e) {
            log.error("Couldn't stop the application");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldSeeNotificationsAndCompletedEvent() throws Exception {
        // given a started app
        startCamelApp();

        // when sending a message in route 2
        ProducerTemplate template = new DefaultProducerTemplate(context);
        template.start();
        template.sendBody("direct:route2", "route2 - 1");

        // then notifyExchangeCompletedEvent should have been called
        verify(notifier, timeout(1000).times(1)).notifyExchangeCompletedEvent(any(ExchangeCompletedEvent.class));

        stopCamelApp();

        // should be 1 message in database
        DBCursor dbObjects = exchangeStatistics.find();
        assertThat(dbObjects.count()).isEqualTo(1);
        DBObject message = dbObjects.next();

        // with 3 steps
        List<DBObject> steps = (List<DBObject>) message.get("s");
        for (DBObject stepStat : steps) {
            log.debug(stepStat.toString());
            assertThat(stepStat.get("s")).isIn(0, 1, 2);
        }

        // and 2 exchangeStatistic (1 for route2 and 1 for route1)
        dbObjects = routeStatistics.find();
        assertThat(dbObjects.count()).isEqualTo(2);

        for (DBObject stat : dbObjects) {
            log.debug(stat.toString());
            assertThat(stat.get("routeId")).isIn("route1", "route2");
            assertThat((Boolean) stat.get("failed")).isFalse();
            assertThat((Integer) stat.get("duration")).isGreaterThan(0);
            assertThat((Long) stat.get("timestamp")).isGreaterThan(0);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldSeeNotificationsAndErrorEvent() throws Exception {
        // given a started camel app
        startCamelApp();

        // when sending a message in route 3
        try {
            ProducerTemplate template = new DefaultProducerTemplate(context);
            template.start();
            template.sendBody("direct:route3", "route3 - 1");
        } catch (CamelExecutionException e) {
            //should receive a NPE but do nothing
        }

        // then notifyExchangeFailedEvent should have been called
        verify(notifier, timeout(1000).times(1)).notifyExchangeFailedEvent(any(ExchangeFailedEvent.class));

        stopCamelApp();

        // should be 1 message in database
        DBCursor dbObjects = exchangeStatistics.find();
        assertThat(dbObjects.count()).isEqualTo(1);
        DBObject message = dbObjects.next();

        List<DBObject> steps = (List<DBObject>) message.get("s");
        for (DBObject stepStat : steps) {
            log.debug(stepStat.toString());
            assertThat(stepStat.get("s")).isIn(0, 1, 2, 3, 4);
        }

        // and 2 exchangeStatistic (1 for route3 and 1 for route1)
        dbObjects = routeStatistics.find();
        assertThat(dbObjects.count()).isEqualTo(2);

        for (DBObject stat : dbObjects) {
            log.debug(stat.toString());
            assertThat(stat.get("routeId")).isIn("route1", "route3");
            if (stat.get("routeId").equals("route3")) {
                assertThat((Boolean) stat.get("failed")).isTrue();
                assertThat((Integer) stat.get("duration")).isEqualTo(0);
            } else {
                assertThat((Boolean) stat.get("failed")).isFalse();
                assertThat((Integer) stat.get("duration")).isGreaterThan(0);
            }
            assertThat((Long) stat.get("timestamp")).isGreaterThan(0);
        }
    }

    @Test
    public void shouldSeeInstanceStateAndRouteState() throws Exception {
        // given a started camel app
        startCamelApp();

        // when started
        // then we should see 3 routes
        DBCursor dbObjects = routes.find();
        assertThat(dbObjects.count()).isEqualTo(3);
        for (DBObject route : dbObjects) {
            log.debug(route.toString());
            assertThat(route.get("routeId")).isIn("route1", "route2", "route3");
            assertThat(route.get("uri")).isIn("direct://route1", "direct://route2", "direct://route3");
            assertThat(route.get("definition")).isNotNull();
        }

        dbObjects = states.find();
        assertThat(dbObjects.count()).isEqualTo(1);
        for (DBObject state : dbObjects) {
            log.debug(state.toString());
            assertThat(state.get("name")).isEqualTo(context.getName());
            assertThat(state.get("state")).isEqualTo("Started");
        }

        dbObjects = routeStates.find();
        assertThat(dbObjects.count()).isEqualTo(3);
        for (DBObject state : dbObjects) {
            log.debug(state.toString());
            assertThat(state.get("routeId")).isIn("route1", "route2", "route3");
            assertThat(state.get("state")).isEqualTo("Started");
        }

        // when stopped
        stopCamelApp();

        // then we should see state stopped
        dbObjects = states.find();
        assertThat(dbObjects.count()).isEqualTo(2);
        for (DBObject state : dbObjects) {
            log.debug(state.toString());
            assertThat(state.get("name")).isEqualTo(context.getName());
            assertThat(state.get("state")).isIn("Started", "Stopped");
        }

        dbObjects = routeStates.find();
        assertThat(dbObjects.count()).isEqualTo(6);
        for (DBObject state : dbObjects) {
            log.debug(state.toString());
            assertThat(state.get("routeId")).isIn("route1", "route2", "route3");
            assertThat(state.get("state")).isIn("Started", "Stopped");
        }
    }
}
