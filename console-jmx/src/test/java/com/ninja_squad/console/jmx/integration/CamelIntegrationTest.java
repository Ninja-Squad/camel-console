package com.ninja_squad.console.jmx.integration;


import com.google.common.collect.Sets;
import com.ninja_squad.console.jmx.CamelJmxConnector;
import com.ninja_squad.console.jmx.CamelJmxNotification;
import com.ninja_squad.console.jmx.CamelJmxNotificationListener;
import com.ninjasquad.console.Instance;
import com.ninjasquad.console.Route;
import com.ninjasquad.console.State;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.apache.camel.management.JmxSystemPropertyKeys;
import org.fest.assertions.core.Condition;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.extractProperty;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


@Slf4j
public class CamelIntegrationTest {

    private CamelJmxConnector camelJmxConnector = new CamelJmxConnector(new Instance());
    private CamelContext context;

    private void startCamelApp() throws Exception {
        //Camel should start with a jmx connection
        System.setProperty(JmxSystemPropertyKeys.CREATE_CONNECTOR, "true");

        //start Camel application with two routes
        context = new DefaultCamelContext();
        //tracer must be enable
        context.setTracing(true);

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
    public void shouldFindInstanceState() throws Exception {
        //new instance is not started
        State state = camelJmxConnector.connect();
        assertThat(state).isEqualTo(State.Stopped);

        //start instance
        startCamelApp();

        //reconnect
        state = camelJmxConnector.connect();
        assertThat(state).isEqualTo(State.Started);

        //stop instance
        stopCamelApp();

        //reconnect
        state = camelJmxConnector.connect();
        assertThat(state).isEqualTo(State.Stopped);
    }

    @Test
    public void shouldFindTheRoutes() throws Exception {
        //new instance not started
        Set<Route> routes = camelJmxConnector.getRoutes();
        //have found two routes
        assertThat(routes).isEmpty();

        //start instance
        startCamelApp();

        //reconnect
        routes = camelJmxConnector.getRoutes();
        //have found two routes
        assertThat(routes).hasSize(2);
        //both started
        assertThat(extractProperty("state").from(routes)).contains(State.Started, State.Started);
        //with corect names
        assertThat(extractProperty("routeId").from(routes)).contains("route1", "route2");

        //stop route1
        context.stopRoute("route1");
        routes = camelJmxConnector.getRoutes();
        //have found two routes
        assertThat(routes).hasSize(2);
        //one started and the other started
        assertThat(extractProperty("state").from(routes)).contains(State.Stopped, State.Started);
        //with corect names
        assertThat(extractProperty("routeId").from(routes)).contains("route1", "route2");

        //remove route1
        context.removeRoute("route1");
        routes = camelJmxConnector.getRoutes();
        //have found two routes
        assertThat(routes).hasSize(1);
        //started
        assertThat(extractProperty("state").from(routes)).contains(State.Started
        );
        //with correct name
        assertThat(extractProperty("routeId").from(routes)).contains("route2");

        //stop instance
        stopCamelApp();
    }

    @Test
    public void shouldSeeOneMessageGoingThroughRoute1() throws Exception {
        //spying on notification listener
        CamelJmxNotificationListener listener = spy(new CamelJmxNotificationListener());
        camelJmxConnector.setNotificationListener(listener);

        //start instance
        startCamelApp();

        //listen on route 1
        camelJmxConnector.listen();

        //send a message in route 1
        ProducerTemplate template = new DefaultProducerTemplate(context);
        template.start();
        template.sendBody("direct:route1", "route1 - 1");

        //wait to receive notification
        verify(listener, timeout(1000).times(1)).storeNotification(any(CamelJmxNotification.class));

        //stop instance
        stopCamelApp();

        //then
        List<CamelJmxNotification> notifications = camelJmxConnector.getNotifications();
        assertThat(extractProperty("body").from(notifications)).containsExactly("route1 - 1");
        assertThat(extractProperty("destination").from(notifications)).containsExactly("mock:result");
    }

    @Test
    public void shouldSeeTwoMessagesGoingThroughRoute2() throws Exception {
        //spying on notification listener
        CamelJmxNotificationListener listener = spy(new CamelJmxNotificationListener());
        camelJmxConnector.setNotificationListener(listener);

        //start instance
        startCamelApp();

        //listen on route 1
        camelJmxConnector.listen();

        //send a message in route 1
        final DateTime start = DateTime.now();
        final ProducerTemplate template = new DefaultProducerTemplate(context);
        template.start();
        template.sendBody("direct:route2", "route2 - 1");

        //wait to receive notification
        verify(listener, timeout(1000).times(3)).storeNotification(any(CamelJmxNotification.class));

        //stop instance
        stopCamelApp();
        final DateTime stop = DateTime.now();

        //then
        List<CamelJmxNotification> notifications = camelJmxConnector.getNotifications();
        assertThat(extractProperty("body").from(notifications)).containsExactly("route2 - 1", "route2 - 2", "route2 - 2");
        assertThat(extractProperty("source").from(notifications)).containsExactly("direct://route2", "direct://route2", "direct://route1");
        assertThat(extractProperty("destination").from(notifications)).containsExactly("route2-processor", "direct:route1", "mock:result");
        // all exchangeIds should be equals
        assertThat(Sets.newHashSet(extractProperty("exchangeId").from(notifications))).hasSize(1);
        // all timestamps should be between start and stop
        assertThat(extractProperty("timestamp").from(notifications)).hasSize(3).are(new Condition<Object>() {
            @Override
            public boolean matches(Object value) {
                DateTime timestamp = (DateTime) value;
                return timestamp.isAfter(start) && timestamp.isBefore(stop);
            }
        });
    }
}
