package com.ninja_squad.console.notifier;

import com.google.common.collect.Sets;

import java.util.Collection;

public class Message {

    Collection<Notification> notifications = Sets.newHashSet();

    public Collection<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(Collection<Notification> notifications) {
        this.notifications = notifications;
    }
}
