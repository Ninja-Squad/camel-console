package com.ninja_squad.console.jmx;

import com.google.common.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import javax.management.Notification;
import javax.management.NotificationListener;
import java.util.Map;

@Slf4j
public class CamelJmxNotificationListener implements NotificationListener {

    private EventBus notificationBus;

    public CamelJmxNotificationListener(EventBus notificationBus) {
        this.notificationBus = notificationBus;
    }

    /**
     * Called every time a notification is received.
     * Convert a jmx notification with raw data to a more useful object {@link CamelJmxNotification} then store it {@link #storeNotification(CamelJmxNotification)}
     *
     * @param notification the notification received over jmx
     * @param object       unused
     */
    @Override
    public void handleNotification(Notification notification, Object object) {

        Map<String, Object> notificationAttributes = (Map<String, Object>) notification.getUserData();

        CamelJmxNotification camelJmxNotification = new CamelJmxNotification();
        camelJmxNotification.setDestination((String) notificationAttributes.get("EndpointURI"));
        camelJmxNotification.setExchangeId((String) notificationAttributes.get("ExchangeId"));
        camelJmxNotification.setBody(notificationAttributes.get("Body"));
        camelJmxNotification.setHeaders((Map<String, String>) notificationAttributes.get("Headers"));
        camelJmxNotification.setBreadcrumbId(camelJmxNotification.getHeaders().get("breadcrumbid"));
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
        notificationBus.post(notification);
    }

}