package com.ninja_squad.console.notifier;

import com.ninja_squad.console.StepStatistic;
import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.processor.DelegateAsyncProcessor;
import org.apache.camel.util.StopWatch;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleTraceInterceptor extends DelegateAsyncProcessor {

    private static final transient Logger log = LoggerFactory.getLogger(ConsoleTraceInterceptor.class);

    private ConsoleEventNotifier eventNotifier;

    private final ProcessorDefinition<?> node;

    public ConsoleTraceInterceptor(ConsoleEventNotifier eventNotifier, ProcessorDefinition<?> node, Processor target) {
        super(target);
        this.eventNotifier = eventNotifier;
        this.node = node;
    }

    @Override
    public String toString() {
        return "ConsoleTraceInterceptor[" + node + "]";
    }

    @Override
    public boolean process(final Exchange exchange, final AsyncCallback callback) {
        // interceptor will also trace routes supposed only for TraceEvents so we need to skip
        // logging TraceEvents to avoid infinite looping
        if (exchange.getProperty(Exchange.TRACE_EVENT, false, Boolean.class)) {
            // but we must still process to allow routing of TraceEvents to eg a JPA endpoint
            return super.process(exchange, callback);
        }

        // only record time if stats is enabled
        final StopWatch watch = new StopWatch();

        return super.process(exchange, new AsyncCallback() {
            public void done(boolean doneSync) {
                try {
                    // record end time and store exchange
                    recordTimeAndTrace(exchange, watch.stop());
                } finally {
                    // and let the original callback know we are done as well
                    callback.done(doneSync);
                }
            }
        });
    }

    protected void recordTimeAndTrace(Exchange exchange, long duration) {
        StepStatistic stepStatistic;
        if (!exchange.isFailed() && exchange.getException() == null) {
            stepStatistic = completedExchange(exchange, duration);
        } else {
            stepStatistic = failedExchange(exchange, duration);
        }
        storeStepStatistic(stepStatistic);
    }

    protected StepStatistic buildStepStatistic(Exchange exchange, long duration) {
        StepStatistic stepStatistic = new StepStatistic();
        stepStatistic.setRouteId(exchange.getFromRouteId());
        String exchangeId = exchange.getExchangeId();
        stepStatistic.setExchangeId(exchangeId);
        stepStatistic.setTimestamp(DateTime.now().getMillis());
        stepStatistic.setDestination(node.getId());
        stepStatistic.setDuration(duration);
        return stepStatistic;
    }

    protected StepStatistic completedExchange(Exchange exchange, long duration) {
        StepStatistic stepStatistic = buildStepStatistic(exchange, duration);
        stepStatistic.setFailed(false);
        return stepStatistic;
    }

    protected StepStatistic failedExchange(Exchange exchange, long duration) {
        StepStatistic stepStatistic = buildStepStatistic(exchange, duration);
        stepStatistic.setFailed(true);
        stepStatistic.setErrorBody(exchange.getIn().getBody());
        stepStatistic.setErrorHeaders(exchange.getIn().getHeaders());
        stepStatistic.setException(exchange.getException().getClass().getSimpleName());
        stepStatistic.setExceptionMessage(exchange.getException().getMessage());
        storeStepStatistic(stepStatistic);
        return stepStatistic;
    }

    protected void storeStepStatistic(StepStatistic stepStatistic) {
        log.debug(stepStatistic.toString());
        eventNotifier.addStepStatistic(stepStatistic.getExchangeId(), stepStatistic);
    }

}
