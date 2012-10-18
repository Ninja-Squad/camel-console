package com.ninja_squad.console.notifier;

import com.google.common.collect.Sets;

import java.util.Collection;

public class Message {

    private String exchangeId;
    private Collection<Notification> notifications = Sets.newHashSet();

    public Collection<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(Collection<Notification> notifications) {
        this.notifications = notifications;
    }

    public String getExchangeId() {
        return exchangeId;
    }

    public void setExchangeId(String exchangeId) {
        this.exchangeId = exchangeId;
    }

    @Override
    public String toString() {
        return "Message{" +
                "exchangeId='" + exchangeId + '\'' +
                ", notifications=" + notifications +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;

        Message message = (Message) o;

        if (exchangeId != null ? !exchangeId.equals(message.exchangeId) : message.exchangeId != null) return false;
        if (notifications != null ? !notifications.equals(message.notifications) : message.notifications != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = exchangeId != null ? exchangeId.hashCode() : 0;
        result = 31 * result + (notifications != null ? notifications.hashCode() : 0);
        return result;
    }
}
