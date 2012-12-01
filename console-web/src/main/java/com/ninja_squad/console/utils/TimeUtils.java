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
            .maximumSize(10000)
            .build();

    /**
     * Rounds a timestamp to the timeUnit asked.
     * Ex : rounds 12:01:45:000 to 12:00:00:000 if timeUnit is HOUR
     * <p/>
     * This implementation uses recursive calls with a cache.
     *
     * @param timestamp the current timestamp (in millis)
     * @param unit      the timeUnit needed
     * @return a rounded timestamp in millis
     */
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

    /**
     * Returns the next for a timestamp and a timeUnit.
     *
     * @param timestamp the current timestamp (in millis)
     * @param unit      the timeUnit
     * @return the next range (in millis)
     */
    public static long getNextRange(long timestamp, TimeUnit unit) {
        return getRange(timestamp, unit, true);
    }

    /**
     * Returns previous range for a timestamp and a timeUnit.
     *
     * @param timestamp the current timestamp (in millis)
     * @param unit      the timeUnit
     * @return the previous range (in millis)
     */
    public static long getPreviousRange(long timestamp, TimeUnit unit) {
        return getRange(timestamp, unit, false);
    }

    /**
     * Returns the next or previous range for a timestamp and a timeUnit, depending on next value.
     *
     * @param timestamp the current timestamp (in millis)
     * @param unit      the timeUnit
     * @param next      if true returns next range, if false returns previous
     * @return the next or previous range (in millis)
     */
    private static long getRange(long timestamp, TimeUnit unit, boolean next) {
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

    /**
     * Finds the highest possible granularity between two timestamps.
     *
     * @param from the lower timestamp (in millis)
     * @param to   the highest timestamp (in millis)
     * @return the highest possible granularity as timeUnit
     */
    public static TimeUnit getTimeUnit(long from, long to) {
        long gap = to - from;
        // second if less than 60 seconds
        if (gap < 60 * 1000L) {
            return TimeUnit.SECOND;
        }
        // minute if less than 60 minutes
        else if (gap < 60 * 60 * 1000L) {
            return TimeUnit.MINUTE;
        }
        // hour if less than 24 hours
        else if (gap < 24 * 60 * 60 * 1000L) {
            return TimeUnit.HOUR;
        }
        // day if less 7 days
        else if (gap < 7 * 24 * 60 * 60 * 1000L) {
            return TimeUnit.DAY;
        }
        // week if less 4 weeks
        else if (gap < 4 * 7 * 24 * 60 * 60 * 1000L) {
            return TimeUnit.WEEK;
        }
        // month if less 12 weeks
        else if (gap < 12 * 4 * 7 * 24 * 60 * 60 * 1000L) {
            return TimeUnit.MONTH;
        }
        return TimeUnit.YEAR;
    }
}
