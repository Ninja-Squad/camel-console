package com.ninja_squad.console.notifier;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.processor.interceptor.TraceEventHandler;
import org.apache.camel.processor.interceptor.TraceInterceptor;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class ConsoleTraceHandler implements TraceEventHandler {
    protected Logger log = LoggerFactory.getLogger(getClass());

    private Multimap<String, Notification> exchanges = HashMultimap.create();

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
        log.debug(notification.toString());
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

    public Collection<Notification> getNotifications(String id) {
        //then remove events
        Collection<Notification> notifications = exchanges.get(id);
        return notifications;
    }

    public void removeNotifications(String id) {
        exchanges.removeAll(id);
    }
}
