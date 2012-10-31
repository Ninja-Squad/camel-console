package com.ninja_squad.console.subscriber;

import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import com.ninja_squad.console.Notification;
import com.ninja_squad.console.model.Message;
import com.ninja_squad.console.model.Statistic;
import com.ninja_squad.console.model.TimeUnit;
import com.ninja_squad.console.repository.MessageRepository;
import com.ninja_squad.console.repository.StatisticRepository;
import com.ninja_squad.console.utils.TimeUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.List;

@Slf4j
public class NotificationSubscriber {

    @Inject
    @Setter
    private MessageRepository messageRepository;

    @Inject
    @Setter
    private StatisticRepository statisticRepository;

    public void subscribe() {
        log.debug("Start subscribing");
        List<Message> pendingNotifications = getPendingNotifications();
        for (Message pendingNotification : pendingNotifications) {
            // compute duration
            int duration = computeDuration(pendingNotification);
            pendingNotification.setDuration(duration);

            // add message to each range
            long timestamp = pendingNotification.getTimestamp();
            boolean isFailed = pendingNotification.isFailed();
            for (TimeUnit unit : TimeUnit.values()) {
                updateMessagesPer(unit, timestamp, duration, isFailed);
            }

            // notification is not pending anymore
            pendingNotification.setHandled(true);
            messageRepository.save(pendingNotification);
        }
    }

    protected Statistic updateMessagesPer(TimeUnit unit, long timestamp, int duration, boolean isFailed) {
        long range = TimeUtils.getRoundedTimestamp(timestamp, unit);
        Statistic statistic = statisticRepository.findOneByRangeAndTimeUnit(range, unit);
        if (statistic == null) {
            // create a new one
            statistic = new Statistic(range, unit, isFailed ? 1 : 0, isFailed ? 0 : 1, duration, duration, duration);
        } else {
            // update existing one
            statistic = updateStatistic(statistic, duration, isFailed);
        }
        // saving it
        return statisticRepository.save(statistic);
    }

    protected Statistic updateStatistic(Statistic statistic, int duration, boolean failed) {
        if (failed) { statistic.addFailed(); } else { statistic.addCompleted(duration); }
        return statistic;
    }

    protected int computeDuration(Message message) {
        List<Notification> notifications = getOrderedSteps(message);
        if(notifications.isEmpty()) return 0;
        return (int) (message.getTimestamp() - notifications.get(0).getTimestamp());
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
        return messageRepository.findByHandledIsFalseOrderByTimestampAsc();
    }
}
