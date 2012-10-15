package com.ninja_squad.console.notifier;

import org.apache.camel.management.event.ExchangeCompletedEvent;
import org.apache.camel.support.EventNotifierSupport;

import java.util.Collection;
import java.util.EventObject;

public class ConsoleEventNotifier extends EventNotifierSupport {

    private NotifierRepository repository;
    private ConsoleTraceHandler traceHandler;

    public void notify(EventObject event) throws Exception {
        if (event instanceof ExchangeCompletedEvent) {
            ExchangeCompletedEvent sent = (ExchangeCompletedEvent) event;
            notifyExchangeCompletedEvent(sent);
        }
    }

    protected void notifyExchangeCompletedEvent(ExchangeCompletedEvent event) {
        log.debug(event.getExchange().getFromRouteId() + " : " + event.getExchange().getExchangeId()
                + " completed.");
        //get notifications related
        final String id = event.getExchange().getExchangeId();
        Collection<Notification> notifications = traceHandler.getNotifications(id);
        log.debug("notifications for completed event " + id  + " : " + notifications);
        //persist them
        repository.save(notifications);
        traceHandler.removeNotifications(id);
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

    public void setTraceHandler(ConsoleTraceHandler traceHandler) {
        this.traceHandler = traceHandler;
    }
}
