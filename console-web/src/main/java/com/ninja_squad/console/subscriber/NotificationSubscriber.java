package com.ninja_squad.console.subscriber;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import com.ninja_squad.console.Notification;
import com.ninja_squad.console.model.Message;
import com.ninja_squad.console.model.TimeUnit;
import com.ninja_squad.console.repository.MessageRepository;
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

    private Cache<String, Long> cache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .build();

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
        long range = getRoundedTimestamp(timestamp, TimeUnit.SECONDS);
    }

    protected long getRoundedTimestamp(long timestamp, TimeUnit unit) {
        Long cached = cache.getIfPresent(timestamp + unit.toString());
        if(cached != null) {
            return cached;
        }
        DateTime time = new DateTime(timestamp);
        log.debug("Rounding " + time + " in " + unit);
        switch (unit) {
            case SECONDS:
                time = time.minusMillis(time.getMillisOfSecond());
                break;
            case MINUTES:
                long roundedS = getRoundedTimestamp(timestamp, TimeUnit.SECONDS);
                time = new DateTime(roundedS).minusSeconds(time.getSecondOfMinute());
                break;
            case HOURS:
                long roundedM = getRoundedTimestamp(timestamp, TimeUnit.MINUTES);
                time = new DateTime(roundedM).minusMinutes(time.getMinuteOfHour());
                break;
            case DAYS:
                long roundedD = getRoundedTimestamp(timestamp, TimeUnit.HOURS);
                time = new DateTime(roundedD).minusHours(time.getHourOfDay());
                break;
            case WEEKS:
                long roundedW = getRoundedTimestamp(timestamp, TimeUnit.DAYS);
                time = new DateTime(roundedW).minusDays(time.getDayOfWeek());
                break;
            case MONTHS:
                long roundedMo = getRoundedTimestamp(timestamp, TimeUnit.DAYS);
                time = new DateTime(roundedMo).minusDays(time.getDayOfMonth() - 1);
                break;
            case YEARS:
                long roundedY = getRoundedTimestamp(timestamp, TimeUnit.DAYS);
                time = new DateTime(roundedY).minusDays(time.getDayOfYear() - 1);
                break;
        }
        log.debug("Rounded " + time);
        cache.put(timestamp + unit.toString(), time.getMillis());
        return time.getMillis();
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
