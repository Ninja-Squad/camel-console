package com.ninja_squad.console.jmx;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import javax.management.Notification;
import javax.management.NotificationListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class CamelJmxNotificationListener implements NotificationListener {

    @Getter
    private List<CamelJmxNotification> notifications = new ArrayList<CamelJmxNotification>();

    /**
     * Called every time a notification is received.
     * Convert a jmx notification with raw data to a more useful object {@link CamelJmxNotification} then store it {@link #storeNotification(CamelJmxNotification)}
     *
     * @param notification the notification received over jmx
     * @param arg1         unused
     */
    @Override
    public void handleNotification(Notification notification, Object arg1) {

        log.debug("notif -> " + notification.toString());

        Map<String, Object> notificationAttributes = (Map<String, Object>) notification.getUserData();

        CamelJmxNotification camelJmxNotification = new CamelJmxNotification();
        camelJmxNotification.setDestination((String) notificationAttributes.get("EndpointURI"));
        camelJmxNotification.setExchangeId((String) notificationAttributes.get("ExchangeId"));
        camelJmxNotification.setBody(notificationAttributes.get("Body"));
        camelJmxNotification.setHeaders((Map<String, String>) notificationAttributes.get("Headers"));
        camelJmxNotification.setProperties((Map<String, String>) notificationAttributes.get("Properties"));
        camelJmxNotification.setSource(camelJmxNotification.getProperties().get("CamelToEndpoint"));
        DateTime timestamp = new DateTime(notificationAttributes.get("TimeStamp"));
        camelJmxNotification.setTimestamp(timestamp);

        storeNotification(camelJmxNotification);
    }

    /**
     * Store the notification
     *
     * @param notification to store
     */
    public void storeNotification(CamelJmxNotification notification) {
        log.debug("store -> " + notification.toString());
        notifications.add(notification);
    }

}