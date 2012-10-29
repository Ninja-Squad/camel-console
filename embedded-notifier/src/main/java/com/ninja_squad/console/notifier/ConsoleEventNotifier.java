package com.ninja_squad.console.notifier;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.ninja_squad.console.Message;
import com.ninja_squad.console.Notification;
import org.apache.camel.management.event.ExchangeCompletedEvent;
import org.apache.camel.management.event.ExchangeFailedEvent;
import org.apache.camel.support.EventNotifierSupport;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.EventObject;

public class ConsoleEventNotifier extends EventNotifierSupport {

    private ConsoleRepository repository;

    private ConsoleLifecycleStrategy consoleLifecycleStrategy;

    private Multimap<String, Notification> exchanges = HashMultimap.create();

    public ConsoleEventNotifier(ConsoleLifecycleStrategy consoleLifecycleStrategy) {
        this.repository = new ConsoleRepositoryJongo();
        this.consoleLifecycleStrategy = consoleLifecycleStrategy;
    }

    public void notify(EventObject event) throws Exception {
        if (event instanceof ExchangeCompletedEvent) {
            ExchangeCompletedEvent sent = (ExchangeCompletedEvent) event;
            notifyExchangeCompletedEvent(sent);
        } else if (event instanceof ExchangeFailedEvent) {
            ExchangeFailedEvent sent = (ExchangeFailedEvent) event;
            notifyExchangeFailedEvent(sent);
        }
    }

    protected void notifyExchangeFailedEvent(ExchangeFailedEvent event) {
        log.debug(event.getExchange().getFromRouteId() + " : " + event.getExchange().getExchangeId()
                + " failed.");
        //get notifications related
        final String id = event.getExchange().getExchangeId();
        persistMessage(id);
        updateRouteStat(event.getExchange().getFromRouteId());
    }

    protected void notifyExchangeCompletedEvent(ExchangeCompletedEvent event) {
        log.debug(event.getExchange().getFromRouteId() + " : " + event.getExchange().getExchangeId()
                + " completed.");
        //get notifications related
        final String id = event.getExchange().getExchangeId();
        persistMessage(id);
        updateRouteStat(event.getExchange().getFromRouteId());
    }

    protected synchronized void addNotification(String exchangeId, Notification notification) {
        //setting step number
        Collection<Notification> notifications = exchanges.get(exchangeId);
        notification.setStep(notifications.size());
        //saving
        exchanges.put(exchangeId, notification);
    }

    protected synchronized Collection<Notification> getNotifications(String id) {
        //then remove events
        Collection<Notification> notifications = exchanges.get(id);
        return notifications;
    }

    protected synchronized void removeNotifications(String id) {
        exchanges.removeAll(id);
    }

    private void persistMessage(String id) {
        Collection<Notification> notifications = getNotifications(id);
        log.debug("notifications for event " + id + " : " + notifications);
        //persist them
        Message message = new Message();
        message.setExchangeId(id);
        message.setNotifications(notifications);
        message.setTimestamp(DateTime.now().getMillis());
        repository.save(message);
        removeNotifications(id);
    }

    protected void updateRouteStat(String routeId) {
        ConsolePerformanceCounter counter = consoleLifecycleStrategy.getCounter(routeId);
        if (counter != null) {
            try {
                log.debug("update stats of " + routeId + " : "
                        + counter.getExchangesCompleted() + "/"
                        + counter.getExchangesFailed() + "/"
                        + counter.getExchangesTotal() + " from " + counter.toString()) ;
                repository.updateRoute(routeId, counter.getExchangesCompleted(), counter.getExchangesFailed(), counter.getExchangesTotal());
            } catch (Exception e) {
                log.error("Error while retrieving performance statistics on route " + routeId, e);
            }
        }
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

    public void setRepository(ConsoleRepository repository) {
        this.repository = repository;
    }
}
