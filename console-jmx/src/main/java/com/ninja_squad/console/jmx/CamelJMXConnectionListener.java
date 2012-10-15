package com.ninja_squad.console.jmx;

import lombok.extern.slf4j.Slf4j;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.remote.JMXConnectionNotification;

@Slf4j
public class CamelJMXConnectionListener implements NotificationListener {

    @Override
    public void handleNotification(Notification notification, final Object retryer) {
        log.debug("Connection state has changed - " + notification.getMessage());
        //connection closed needs to call a retryer
        if (notification.getType().equals(JMXConnectionNotification.CLOSED)) {
            //retryer will try to connect
            ((CamelJmxConnectionRetryer) retryer).start();
        }
    }

}
