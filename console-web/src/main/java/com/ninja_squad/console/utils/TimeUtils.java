package com.ninja_squad.console.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ninja_squad.console.model.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

@Slf4j
public class TimeUtils {

    private static Cache<String, Long> cache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .build();

    public static long getRoundedTimestamp(long timestamp, TimeUnit unit) {
        if (unit == null) { return timestamp; }
        // check if in cache
        Long cached = cache.getIfPresent(timestamp + unit.toString());
        if (cached != null) { return cached; }
        // if not compute it
        DateTime time = new DateTime(timestamp, DateTimeZone.UTC);
        log.trace("Rounding " + time + " in " + unit);
        switch (unit) {
            case SECOND:
                time = time.minusMillis(time.getMillisOfSecond());
                break;
            case MINUTE:
                long roundedS = getRoundedTimestamp(timestamp, TimeUnit.SECOND);
                time = new DateTime(roundedS, DateTimeZone.UTC).minusSeconds(time.getSecondOfMinute());
                break;
            case HOUR:
                long roundedM = getRoundedTimestamp(timestamp, TimeUnit.MINUTE);
                time = new DateTime(roundedM, DateTimeZone.UTC).minusMinutes(time.getMinuteOfHour());
                break;
            case DAY:
                long roundedD = getRoundedTimestamp(timestamp, TimeUnit.HOUR);
                time = new DateTime(roundedD, DateTimeZone.UTC).minusHours(time.getHourOfDay());
                break;
            case WEEK:
                long roundedW = getRoundedTimestamp(timestamp, TimeUnit.DAY);
                time = new DateTime(roundedW, DateTimeZone.UTC).minusDays(time.getDayOfWeek());
                break;
            case MONTH:
                long roundedMo = getRoundedTimestamp(timestamp, TimeUnit.DAY);
                time = new DateTime(roundedMo, DateTimeZone.UTC).minusDays(time.getDayOfMonth() - 1);
                break;
            case YEAR:
                long roundedY = getRoundedTimestamp(timestamp, TimeUnit.DAY);
                time = new DateTime(roundedY, DateTimeZone.UTC).minusDays(time.getDayOfYear() - 1);
                break;
        }
        log.trace("Rounded " + time);
        // store in cache
        cache.put(timestamp + unit.toString(), time.getMillis());
        return time.getMillis();
    }

    public static long getNextRange(long timestamp, TimeUnit unit) {
        return getRange(timestamp, unit, true);
    }

    public static long getPreviousRange(long timestamp, TimeUnit unit) {
        return getRange(timestamp, unit, false);
    }

    public static long getRange(long timestamp, TimeUnit unit, boolean next) {
        if (unit == null) { return timestamp; }
        DateTime time = new DateTime(timestamp, DateTimeZone.UTC);
        log.trace((next ? "Next" : "Previous") + " timestamp for " + time + " in " + unit);
        switch (unit) {
            case SECOND:
                time = next ? time.plusSeconds(1) : time.minusSeconds(1);
                break;
            case MINUTE:
                time = next ? time.plusMinutes(1) : time.minusMinutes(1);
                break;
            case HOUR:
                time = next ? time.plusHours(1) : time.minusHours(1);
                break;
            case DAY:
                time = next ? time.plusDays(1) : time.minusDays(1);
                break;
            case WEEK:
                time = next ? time.plusWeeks(1) : time.minusWeeks(1);
                break;
            case MONTH:
                time = next ? time.plusMonths(1) : time.minusMonths(1);
                break;
            case YEAR:
                time = next ? time.plusYears(1) : time.minusYears(1);
                break;
        }
        log.trace((next ? "Next " : "Previous ") + time);
        return getRoundedTimestamp(time.getMillis(), unit);
    }
}
