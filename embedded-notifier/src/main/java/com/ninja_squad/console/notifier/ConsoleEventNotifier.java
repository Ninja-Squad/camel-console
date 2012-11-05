package com.ninja_squad.console.notifier;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.ninja_squad.console.Message;
import com.ninja_squad.console.Notification;
import lombok.Setter;
import org.apache.camel.management.event.ExchangeCompletedEvent;
import org.apache.camel.management.event.ExchangeFailedEvent;
import org.apache.camel.support.EventNotifierSupport;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.EventObject;
import java.util.Properties;

public class ConsoleEventNotifier extends EventNotifierSupport {

    @Setter
    private ConsoleRepository repository;

    private Multimap<String, Notification> exchanges = HashMultimap.create();

    public ConsoleEventNotifier() {
        String property = null;
        String host = null;
        try {
            Properties properties = new Properties();
            properties.load(getClass().getClassLoader().getResourceAsStream("database.properties"));
            property = properties.getProperty("mongodb.port");
            host = properties.getProperty("mongodb.host");
        } catch (Exception e) {
            log.error("no database.properties on classpath : will use default values localhost:27017");
        }
        host = host == null ? "localhost" : host;
        int port = Integer.parseInt(property == null ? "27017" : property);
        this.repository = new ConsoleRepositoryJongo(host, port);
    }

    @Override
    public void notify(EventObject event) throws Exception {
        if (event instanceof ExchangeCompletedEvent) {
            ExchangeCompletedEvent sent = (ExchangeCompletedEvent) event;
            notifyExchangeCompletedEvent(sent);
        } else if (event instanceof ExchangeFailedEvent) {
            ExchangeFailedEvent sent = (ExchangeFailedEvent) event;
            notifyExchangeFailedEvent(sent);
        }
    }

    protected void notifyExchangeCompletedEvent(ExchangeCompletedEvent event) {
        log.debug(event.getExchange().getFromRouteId() + " : " + event.getExchange().getExchangeId()
                + " completed.");
        //get notifications related
        final String id = event.getExchange().getExchangeId();
        Message message = buildMessage(id);
        message.setFailed(false);
        persistMessage(message);
    }

    protected void notifyExchangeFailedEvent(ExchangeFailedEvent event) {
        log.debug(event.getExchange().getFromRouteId() + " : " + event.getExchange().getExchangeId()
                + " failed.");
        //get notifications related
        final String id = event.getExchange().getExchangeId();
        Message message = buildMessage(id);
        message.setFailed(true);
        if (event.getExchange().getException() != null) {
            message.setException(event.getExchange().getException().getClass().getSimpleName());
            message.setExceptionMessage(event.getExchange().getException().getMessage());
        }
        persistMessage(message);
    }

    protected synchronized void addNotification(String exchangeId, Notification notification) {
        //setting step number
        Collection<Notification> notifications = exchanges.get(exchangeId);
        notification.setStep(notifications.size());
        //saving
        exchanges.put(exchangeId, notification);
    }

    protected synchronized Collection<Notification> getNotifications(String id) {
        return exchanges.get(id);
    }

    protected synchronized void removeNotifications(String id) {
        exchanges.removeAll(id);
    }

    private void persistMessage(Message message) {
        repository.save(message);
        removeNotifications(message.getExchangeId());
    }

    private Message buildMessage(String id) {
        Collection<Notification> notifications = getNotifications(id);
        log.debug("notifications for event " + id + " : " + notifications);
        //persist them
        Message message = new Message();
        message.setExchangeId(id);
        message.setNotifications(notifications);
        message.setTimestamp(DateTime.now().getMillis());
        return message;
    }

    @Override
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
}
