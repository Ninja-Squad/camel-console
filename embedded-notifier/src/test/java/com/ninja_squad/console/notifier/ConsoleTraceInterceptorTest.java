package com.ninja_squad.console.notifier;

import com.google.common.collect.Maps;
import com.ninja_squad.console.Notification;
import org.apache.camel.AsyncCallback;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.model.ProcessorDefinition;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

public class ConsoleTraceInterceptorTest {

    ProcessorDefinition<?> node = mock(ProcessorDefinition.class);
    ConsoleEventNotifier notifier = spy(new ConsoleEventNotifier(mock(ConsoleLifecycleStrategy.class)));
    ConsoleTraceInterceptor consoleTraceInterceptor = spy(new ConsoleTraceInterceptor(notifier, node, mock(Processor.class)));

    @Test
    public void processShouldCreateANotification() throws Exception {
        // given an ExchangeSentEvent
        CamelContext camelContext = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(camelContext);

        // when the EventNotifier receives it
        consoleTraceInterceptor.process(exchange, mock(AsyncCallback.class));

        // then notifyExchangeSentEvent should have been called
        verify(consoleTraceInterceptor).recordTimeAndTrace(any(Exchange.class), anyLong());
    }

    @Test
    public void processShouldNotTraceATracer() throws Exception {
        // given an ExchangeSentEvent
        CamelContext camelContext = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setProperty(Exchange.TRACE_EVENT, true);

        // when the EventNotifier receives it
        consoleTraceInterceptor.process(exchange, mock(AsyncCallback.class));

        // then notifyExchangeSentEvent should NOT have been called
        verify(consoleTraceInterceptor, times(0)).recordTimeAndTrace(any(Exchange.class), anyLong());
    }

    @Test
    public void recordShouldBuildACompletedExchange() throws Exception {
        // given an ExchangeSentEvent
        CamelContext camelContext = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(camelContext);

        // when record is called
        consoleTraceInterceptor.recordTimeAndTrace(exchange, 110L);

        // then completedExchange should be called
        verify(consoleTraceInterceptor).completedExchange(exchange, 110L);
    }

    @Test
    public void recordShouldBuildAFailedExchange() throws Exception {
        // given an ExchangeSentEvent
        CamelContext camelContext = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setException(mock(NullPointerException.class));

        // when record is called
        consoleTraceInterceptor.recordTimeAndTrace(exchange, 110L);

        // then completedExchange should be called
        verify(consoleTraceInterceptor).failedExchange(exchange, 110L);
    }

    @Test
    public void buildNotificationShouldBuildANotification() throws Exception {
        // given a completed exchange
        CamelContext camelContext = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setFromRouteId("route1");
        doReturn("destination").when(node).getLabel();

        // when the exchange is completed
        long start = DateTime.now().getMillis();
        Notification notification = consoleTraceInterceptor.buildNotification(exchange, 110L);

        // then the notification should be filled
        assertThat(notification.getExchangeId()).isEqualTo(exchange.getExchangeId());
        assertThat(notification.getDestination()).isEqualTo("destination");
        assertThat(notification.getDuration()).isEqualTo(110L);
        assertThat(notification.getErrorBody()).isNull();
        assertThat(notification.getErrorHeaders()).isNull();
        assertThat(notification.getRouteId()).isEqualTo("route1");
        assertThat(notification.getStep()).isEqualTo(0);
        assertThat(notification.getTimestamp())
                .isGreaterThanOrEqualTo(start)
                .isLessThanOrEqualTo(DateTime.now().getMillis());
    }

    @Test
    public void completedExchangeShouldBuildANotificationNotFailed() throws Exception {
        // given a completed exchange
        CamelContext camelContext = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setFromRouteId("route1");
        doReturn("destination").when(node).getLabel();

        // when the exchange is completed
        Notification notification = consoleTraceInterceptor.completedExchange(exchange, 110L);

        // then the notification should be filled
        assertThat(notification.isFailed()).isEqualTo(false);
    }

    @Test
    public void failedExchangeShouldBuildANotificationFailed() throws Exception {
        // given a completed exchange
        CamelContext camelContext = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setFromRouteId("route1");
        String body = "body failed";
        exchange.getIn().setBody(body);
        Map<String, Object> headers = Maps.newHashMap();
        headers.put("header1", "A");
        exchange.getIn().setHeaders(headers);
        doReturn("destination").when(node).getLabel();
        NullPointerException exception = new NullPointerException();
        exchange.setException(exception);

        // when the exchange is completed
        Notification notification = consoleTraceInterceptor.failedExchange(exchange, 110L);

        // then the notification should be filled
        assertThat(notification.isFailed()).isEqualTo(true);
        assertThat(notification.getErrorBody()).isEqualTo(body);
        assertThat(notification.getErrorHeaders()).isEqualTo(headers);
        assertThat(notification.getException()).isEqualTo(exception);
    }

    @Test
    public void storeNotificationShouldCallEventNotifier() throws Exception {
        // given a notification completed
        Notification notification = new Notification();
        notification.setExchangeId("1");

        // when the notification is stored
        consoleTraceInterceptor.storeNotification(notification);

        // then event notifier should be called
        verify(notifier).addNotification("1", notification);
    }
}
