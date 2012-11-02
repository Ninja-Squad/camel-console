package com.ninja_squad.console.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ninja_squad.console.model.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

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
        DateTime time = new DateTime(timestamp);
        log.debug("Rounding " + time + " in " + unit);
        switch (unit) {
            case SECOND:
                time = time.minusMillis(time.getMillisOfSecond());
                break;
            case MINUTE:
                long roundedS = getRoundedTimestamp(timestamp, TimeUnit.SECOND);
                time = new DateTime(roundedS).minusSeconds(time.getSecondOfMinute());
                break;
            case HOUR:
                long roundedM = getRoundedTimestamp(timestamp, TimeUnit.MINUTE);
                time = new DateTime(roundedM).minusMinutes(time.getMinuteOfHour());
                break;
            case DAY:
                long roundedD = getRoundedTimestamp(timestamp, TimeUnit.HOUR);
                time = new DateTime(roundedD).minusHours(time.getHourOfDay());
                break;
            case WEEK:
                long roundedW = getRoundedTimestamp(timestamp, TimeUnit.DAY);
                time = new DateTime(roundedW).minusDays(time.getDayOfWeek());
                break;
            case MONTH:
                long roundedMo = getRoundedTimestamp(timestamp, TimeUnit.DAY);
                time = new DateTime(roundedMo).minusDays(time.getDayOfMonth() - 1);
                break;
            case YEAR:
                long roundedY = getRoundedTimestamp(timestamp, TimeUnit.DAY);
                time = new DateTime(roundedY).minusDays(time.getDayOfYear() - 1);
                break;
        }
        log.debug("Rounded " + time);
        // store in cache
        cache.put(timestamp + unit.toString(), time.getMillis());
        return time.getMillis();
    }
}
