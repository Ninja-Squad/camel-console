package com.ninja_squad.console.notifier;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.management.event.ExchangeCompletedEvent;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.processor.interceptor.TraceEventHandler;
import org.apache.camel.processor.interceptor.TraceInterceptor;
import org.apache.camel.support.EventNotifierSupport;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.EventObject;

public class ConsoleEventNotifier extends EventNotifierSupport implements TraceEventHandler {

    private NotifierRepository repository;

    private Multimap<String, Notification> exchanges = HashMultimap.create();

    public void notify(EventObject event) throws Exception {
        if (event instanceof ExchangeCompletedEvent) {
            ExchangeCompletedEvent sent = (ExchangeCompletedEvent) event;
            notifyExchangeCompletedEvent(sent);
        }
    }

    protected void notifyExchangeCompletedEvent(ExchangeCompletedEvent event) {
        log.debug(event.getExchange().getFromRouteId() + " : " + event.getExchange().getExchangeId()
                + " completed.");
        //get events related
        final String id = event.getExchange().getExchangeId();
        Collection<Notification> events = exchanges.get(id);

        //persist them
        repository.save(events);

        //then remove events
        exchanges.removeAll(id);
    }

    public boolean isEnabled(EventObject event) {
        return true;
    }

    @Override
    protected void doStart() throws Exception {
        //nothing to do
    }

    @Override
    protected void doStop() throws Exception {
        //nothing to do
    }

    @Override
    public boolean isStarted() {
        return true;
    }

    public void setRepository(NotifierRepository repository) {
        this.repository = repository;
    }

    @Override
    public void traceExchange(ProcessorDefinition<?> node, Processor target, TraceInterceptor traceInterceptor, Exchange exchange) throws Exception {
        Notification notification = new Notification();
        notification.setRouteId(exchange.getFromRouteId());
        String exchangeId = exchange.getExchangeId();
        notification.setExchangeId(exchangeId);
        notification.setBody(exchange.getIn().getBody());
        notification.setTimestamp(DateTime.now());
        notification.setSource(exchange.getFromEndpoint() == null ? "" : exchange.getFromEndpoint().getEndpointUri());
        notification.setDestination(node.getLabel());
        addNotification(exchangeId, notification);
    }

    protected void addNotification(String exchangeId, Notification notification) {
        exchanges.put(exchangeId, notification);
    }

    @Override
    public Object traceExchangeIn(ProcessorDefinition<?> node, Processor target, TraceInterceptor traceInterceptor, Exchange exchange) throws Exception {
        return null;
    }

    @Override
    public void traceExchangeOut(ProcessorDefinition<?> node, Processor target, TraceInterceptor traceInterceptor, Exchange exchange, Object traceState) throws Exception {
    }
}
