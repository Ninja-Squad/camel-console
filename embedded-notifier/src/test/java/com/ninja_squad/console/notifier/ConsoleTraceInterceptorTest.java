package com.ninja_squad.console.notifier;

import com.google.common.collect.Maps;
import com.ninja_squad.console.StepStatistic;
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
    ConsoleEventNotifier notifier = spy(new ConsoleEventNotifier());
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
        doReturn("destination").when(node).getId();

        // when the exchange is completed
        long start = DateTime.now().getMillis();
        StepStatistic stepStatistic = consoleTraceInterceptor.buildStepStatistic(exchange, 110L);

        // then the stepStatistic should be filled
        assertThat(stepStatistic.getExchangeId()).isEqualTo(exchange.getExchangeId());
        assertThat(stepStatistic.getDestination()).isEqualTo("destination");
        assertThat(stepStatistic.getDuration()).isEqualTo(110L);
        assertThat(stepStatistic.getErrorBody()).isNull();
        assertThat(stepStatistic.getErrorHeaders()).isNull();
        assertThat(stepStatistic.getRouteId()).isEqualTo("route1");
        assertThat(stepStatistic.getStep()).isEqualTo(0);
        assertThat(stepStatistic.getTimestamp())
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
        StepStatistic stepStatistic = consoleTraceInterceptor.completedExchange(exchange, 110L);

        // then the stepStatistic should be filled
        assertThat(stepStatistic.isFailed()).isEqualTo(false);
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
        StepStatistic stepStatistic = consoleTraceInterceptor.failedExchange(exchange, 110L);

        // then the stepStatistic should be filled
        assertThat(stepStatistic.isFailed()).isEqualTo(true);
        assertThat(stepStatistic.getErrorBody()).isEqualTo(body);
        assertThat(stepStatistic.getErrorHeaders()).isEqualTo(headers);
        assertThat(stepStatistic.getException()).isEqualTo("NullPointerException");
        assertThat(stepStatistic.getExceptionMessage()).isNull();
    }

    @Test
    public void storeNotificationShouldCallEventNotifier() throws Exception {
        // given a stepStatistic completed
        StepStatistic stepStatistic = new StepStatistic();
        stepStatistic.setExchangeId("1");

        // when the stepStatistic is stored
        consoleTraceInterceptor.storeStepStatistic(stepStatistic);

        // then event notifier should be called
        verify(notifier).addStepStatistic("1", stepStatistic);
    }
}
