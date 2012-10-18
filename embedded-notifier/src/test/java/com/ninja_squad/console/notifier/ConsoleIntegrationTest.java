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
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.processor.interceptor.TraceInterceptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ConsoleIntegrationTest {

    protected Logger log = LoggerFactory.getLogger(getClass());

    private CamelContext context;
    private MongodProcess mongod;
    private DBCollection notifications;
    private ConsoleEventNotifier notifier;
    private ConsoleTraceHandler traceHandler;


    @Before
    public void setUp() throws Exception {

        //setting up mongodb embedded
        int port = 27017;
        MongodConfig mongodConfig = new MongodConfig(Version.Main.V2_0, port, Network.localhostIsIPv6());
        MongodStarter runtime = MongodStarter.getDefaultInstance();
        MongodExecutable mongodExecutable = runtime.prepare(mongodConfig);
        mongod = mongodExecutable.start();
        Mongo mongo = new Mongo("localhost", port);
        DB db = mongo.getDB("console");
        notifications = db.getCollection("notifications");

        //setting up notifiers and tracers
        traceHandler = spy(new ConsoleTraceHandler());
        notifier = spy(new ConsoleEventNotifier());
        notifier.setTraceHandler(traceHandler);

    }

    @After
    public void tearDown() throws Exception {
        if (mongod != null) {
            mongod.stop();
        }
    }

    private void startCamelApp() throws Exception {
        //start Camel application with two routes
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
        //add the intercept strategy
        context.addInterceptStrategy(new ConsoleTracer(traceHandler));
        //context.setTracing(true);
        //Tracer defaultTracer = (Tracer) context.getDefaultTracer();
        //defaultTracer.getTraceHandlers().clear();
        //defaultTracer.addTraceHandler(traceHandler);
        //defaultTracer.setLogLevel(LoggingLevel.TRACE);
        // and the management strategy
        context.getManagementStrategy().addEventNotifier(notifier);
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
    public void shouldSeeNotificationsAndCompletedEvent() throws Exception {
        startCamelApp();

        //send a message in route 2
        ProducerTemplate template = new DefaultProducerTemplate(context);
        template.start();
        template.sendBody("direct:route2", "route1 - 1");

        //then notifyExchangeSentEvent should have been called
        verify(traceHandler, timeout(1000).times(3)).traceExchange(any(ProcessorDefinition.class),
                any(Processor.class), any(TraceInterceptor.class), any(Exchange.class));
        //and notifyExchangeCompletedEvent should have been called
        verify(notifier, timeout(1000).times(1)).notifyExchangeCompletedEvent(any(ExchangeCompletedEvent.class));

        Thread.sleep(2000);

        //should be 1 message in database
        DBCursor dbObjects = notifications.find();
        assertThat(dbObjects.count()).isEqualTo(1);
        DBObject message = dbObjects.next();

        List<DBObject> notifs = (List<DBObject>) message.get("notifications");
        for (DBObject notification : notifs) {
            log.debug(notification.toString());
            assertThat(notification.get("step")).isIn(0, 1, 2);
        }

        stopCamelApp();
    }

    @Test
    public void shouldSeeNotificationsAndErrorEvent() throws Exception {
        startCamelApp();

        //send a message in route 2
        try {
            ProducerTemplate template = new DefaultProducerTemplate(context);
            template.start();
            template.sendBody("direct:route3", "route3 - 1");
        } catch (CamelExecutionException e) {
            //should receive a NPE but do nothing
        }

        //then notifyExchangeSentEvent should have been called
        verify(traceHandler, timeout(1000).times(5)).traceExchange(any(ProcessorDefinition.class),
                any(Processor.class), any(TraceInterceptor.class), any(Exchange.class));
        //and notifyExchangeFailedEvent should have been called
        verify(notifier, timeout(1000).times(1)).notifyExchangeFailedEvent(any(ExchangeFailedEvent.class));

        Thread.sleep(2000);

        //should be 1 message in database
        DBCursor dbObjects = notifications.find();
        assertThat(dbObjects.count()).isEqualTo(1);
        DBObject message = dbObjects.next();

        List<DBObject> notifs = (List<DBObject>) message.get("notifications");
        for (DBObject notification : notifs) {
            log.debug(notification.toString());
            assertThat(notification.get("step")).isIn(0, 1, 2, 3, 4);
        }

        stopCamelApp();
    }
}
