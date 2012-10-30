package com.ninja_squad.console.subscriber;

import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import com.ninja_squad.console.Notification;
import com.ninja_squad.console.model.Message;
import com.ninja_squad.console.repository.MessageRepository;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Slf4j
public class NotificationSubscriber {

    @Inject
    @Named("messageRepository")
    @Setter
    private MessageRepository repository;

    public void subscribe() {
        log.debug("Start subscribing");
        List<Message> pendingNotifications = getPendingNotifications();
        for (Message pendingNotification : pendingNotifications) {
            long duration = computeDuration(pendingNotification);
        }
    }

    protected long computeDuration(Message message) {
        List<Notification> notifications = getOrderedSteps(message);
        return message.getTimestamp() - notifications.get(0).getTimestamp();
    }

    protected List<Notification> getOrderedSteps(Message pendingNotification) {
        Ordering<Notification> ordering = new Ordering<Notification>() {
            @Override
            public int compare(Notification left, Notification right) {
                return Ints.compare(left.getStep(), right.getStep());
            }
        };
        return ordering.sortedCopy(pendingNotification.getNotifications());
    }

    protected List<Message> getPendingNotifications() {
        return repository.findByHandledIsFalseOrderByTimestampAsc();
    }
}
