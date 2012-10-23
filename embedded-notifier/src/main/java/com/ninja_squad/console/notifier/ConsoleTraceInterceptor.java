package com.ninja_squad.console.notifier;

import com.ninja_squad.console.Notification;
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

    private ConsolePerformanceCounter counter;

    private final ProcessorDefinition<?> node;

    public ConsoleTraceInterceptor(ConsoleEventNotifier eventNotifier, ProcessorDefinition<?> node, Processor target) {
        super(target);
        this.eventNotifier = eventNotifier;
        this.node = node;
        counter = new ConsolePerformanceCounter(node.toString());
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
        final StopWatch watch = (counter != null) ? new StopWatch() : null;

        boolean sync = super.process(exchange, new AsyncCallback() {
            public void done(boolean doneSync) {
                try {
                    // record end time
                    if (watch != null) {
                        recordTime(exchange, watch.stop());
                    }
                } finally {
                    // and let the original callback know we are done as well
                    callback.done(doneSync);
                }
            }

            @Override
            public String toString() {
                return ConsoleTraceInterceptor.this.toString();
            }
        });

        try {
            traceExchange(exchange, counter);
        } catch (Exception e) {
            log.info("Error while tracing exchange");
        }

        return sync;
    }

    protected void recordTime(Exchange exchange, long duration) {
        if (!exchange.isFailed() && exchange.getException() == null) {
            counter.completedExchange(exchange, duration);
        } else {
            counter.failedExchange(exchange);
        }
    }

    protected void traceExchange(Exchange exchange, ConsolePerformanceCounter counter) throws Exception {
        Notification notification = new Notification();
        notification.setRouteId(exchange.getFromRouteId());
        String exchangeId = exchange.getExchangeId();
        notification.setExchangeId(exchangeId);
        notification.setBody(exchange.getIn().getBody());
        notification.setTimestamp(DateTime.now());
        notification.setSource(exchange.getFromEndpoint() == null ? "" : exchange.getFromEndpoint().getEndpointUri());
        notification.setDestination(node.getLabel());
        notification.setDuration(counter.getLastProcessingTime());
        eventNotifier.addNotification(exchangeId, notification);
        log.debug(notification.toString());
    }

}
