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

        boolean sync = super.process(exchange, new AsyncCallback() {
            public void done(boolean doneSync) {
                try {
                    // record end time and store exchange
                    if (watch != null) {
                        recordTimeAndTrace(exchange, watch.stop());
                    }
                } finally {
                    // and let the original callback know we are done as well
                    callback.done(doneSync);
                }
            }
        });
        return sync;
    }

    protected void recordTimeAndTrace(Exchange exchange, long duration) {
        Notification notification;
        if (!exchange.isFailed() && exchange.getException() == null) {
            notification = completedExchange(exchange, duration);
        } else {
            notification = failedExchange(exchange, duration);
        }
        storeNotification(notification);
    }

    protected Notification buildNotification(Exchange exchange, long duration) {
        Notification notification = new Notification();
        notification.setRouteId(exchange.getFromRouteId());
        String exchangeId = exchange.getExchangeId();
        notification.setExchangeId(exchangeId);
        notification.setTimestamp(DateTime.now().getMillis());
        notification.setDestination(node.getLabel());
        notification.setDuration(duration);
        return notification;
    }

    protected Notification completedExchange(Exchange exchange, long duration) {
        Notification notification = buildNotification(exchange, duration);
        notification.setFailed(false);
        return notification;
    }

    protected Notification failedExchange(Exchange exchange, long duration) {
        Notification notification = buildNotification(exchange, duration);
        notification.setFailed(true);
        notification.setErrorBody(exchange.getIn().getBody());
        notification.setErrorHeaders(exchange.getIn().getHeaders());
        notification.setException(exchange.getException());
        storeNotification(notification);
        return notification;
    }

    protected void storeNotification(Notification notification) {
        log.debug(notification.toString());
        eventNotifier.addNotification(notification.getExchangeId(), notification);
    }

}
