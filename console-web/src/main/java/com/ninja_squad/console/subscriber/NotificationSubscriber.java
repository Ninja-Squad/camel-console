package com.ninja_squad.console.subscriber;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import com.ninja_squad.console.Notification;
import com.ninja_squad.console.model.Message;
import com.ninja_squad.console.model.TimeUnit;
import com.ninja_squad.console.repository.MessageRepository;
import com.ninja_squad.console.utils.TimeUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

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
            //compute duration
            long duration = computeDuration(pendingNotification);
            pendingNotification.setDuration(duration);

            //add message to each range
            long timestamp = pendingNotification.getTimestamp();
            boolean isFailed = pendingNotification.isFailed();
            updateMessagesPerSecond(timestamp, duration, isFailed);
        }
    }

    protected void updateMessagesPerSecond(long timestamp, long duration, boolean isFailed) {
        long range = TimeUtils.getRoundedTimestamp(timestamp, TimeUnit.SECONDS);
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
