package com.ninja_squad.console.utils;

import com.ninja_squad.console.model.TimeUnit;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

public class TimeUtilsTest {

    @Test
    public void getRoundedTimestampShouldReturnRoundedTimestamp() throws Exception {
        // given a timestamp
        long timestamp = new DateTime(2012, 10, 29, 16, 18, 41, 246, DateTimeZone.UTC).getMillis();
        TimeUtils timeUtils = spy(new TimeUtils());

        // when rounding
        long roundedSeconds = timeUtils.getRoundedTimestamp(timestamp, TimeUnit.SECOND);
        long roundedMinutes = timeUtils.getRoundedTimestamp(timestamp, TimeUnit.MINUTE);
        long roundedHours = timeUtils.getRoundedTimestamp(timestamp, TimeUnit.HOUR);
        long roundedDays = timeUtils.getRoundedTimestamp(timestamp, TimeUnit.DAY);
        long roundedWeeks = timeUtils.getRoundedTimestamp(timestamp, TimeUnit.WEEK);
        long roundedMonths = timeUtils.getRoundedTimestamp(timestamp, TimeUnit.MONTH);
        long roundedYears = timeUtils.getRoundedTimestamp(timestamp, TimeUnit.YEAR);

        // then should be rounded in seconds
        long expected = new DateTime(2012, 10, 29, 16, 18, 41, 0, DateTimeZone.UTC).getMillis();
        assertThat(roundedSeconds).isEqualTo(expected);
        expected = new DateTime(2012, 10, 29, 16, 18, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(roundedMinutes).isEqualTo(expected);
        expected = new DateTime(2012, 10, 29, 16, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(roundedHours).isEqualTo(expected);
        expected = new DateTime(2012, 10, 29, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(roundedDays).isEqualTo(expected);
        expected = new DateTime(2012, 10, 28, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(roundedWeeks).isEqualTo(expected);
        expected = new DateTime(2012, 10, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(roundedMonths).isEqualTo(expected);
        expected = new DateTime(2012, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(roundedYears).isEqualTo(expected);
        verify(timeUtils, times(13)).getRoundedTimestamp(anyLong(), any(TimeUnit.class));
    }

    @Test
    public void getNextRangeShouldReturnTheNextRangeForTheTimeUnit() throws Exception {
        // given a timestamp
        long timestamp = new DateTime(2012, 10, 29, 16, 18, 41, 246, DateTimeZone.UTC).getMillis();

        // when getting next range
        long roundedSeconds = TimeUtils.getNextRange(timestamp, TimeUnit.SECOND);
        long roundedMinutes = TimeUtils.getNextRange(timestamp, TimeUnit.MINUTE);
        long roundedHours = TimeUtils.getNextRange(timestamp, TimeUnit.HOUR);
        long roundedDays = TimeUtils.getNextRange(timestamp, TimeUnit.DAY);
        long roundedWeeks = TimeUtils.getNextRange(timestamp, TimeUnit.WEEK);
        long roundedMonths = TimeUtils.getNextRange(timestamp, TimeUnit.MONTH);
        long roundedYears = TimeUtils.getNextRange(timestamp, TimeUnit.YEAR);

        // then should be next rounded range
        long expected = new DateTime(2012, 10, 29, 16, 18, 42, 0, DateTimeZone.UTC).getMillis();
        assertThat(roundedSeconds).isEqualTo(expected);
        expected = new DateTime(2012, 10, 29, 16, 19, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(roundedMinutes).isEqualTo(expected);
        expected = new DateTime(2012, 10, 29, 17, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(roundedHours).isEqualTo(expected);
        expected = new DateTime(2012, 10, 30, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(roundedDays).isEqualTo(expected);
        expected = new DateTime(2012, 11, 4, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(roundedWeeks).isEqualTo(expected);
        expected = new DateTime(2012, 11, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(roundedMonths).isEqualTo(expected);
        expected = new DateTime(2013, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(roundedYears).isEqualTo(expected);
    }

    @Test
    public void getPreviousRangeShouldReturnThePreviousRangeForTheTimeUnit() throws Exception {
        // given a timestamp
        long timestamp = new DateTime(2012, 10, 27, 16, 18, 41, 246, DateTimeZone.UTC).getMillis();

        // when getting next range
        long roundedSeconds = TimeUtils.getPreviousRange(timestamp, TimeUnit.SECOND);
        long roundedMinutes = TimeUtils.getPreviousRange(timestamp, TimeUnit.MINUTE);
        long roundedHours = TimeUtils.getPreviousRange(timestamp, TimeUnit.HOUR);
        long roundedDays = TimeUtils.getPreviousRange(timestamp, TimeUnit.DAY);
        long roundedWeeks = TimeUtils.getPreviousRange(timestamp, TimeUnit.WEEK);
        long roundedMonths = TimeUtils.getPreviousRange(timestamp, TimeUnit.MONTH);
        long roundedYears = TimeUtils.getPreviousRange(timestamp, TimeUnit.YEAR);

        // then should be previous rounded range
        long expected = new DateTime(2012, 10, 27, 16, 18, 40, 0, DateTimeZone.UTC).getMillis();
        assertThat(roundedSeconds).isEqualTo(expected);
        expected = new DateTime(2012, 10, 27, 16, 17, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(roundedMinutes).isEqualTo(expected);
        expected = new DateTime(2012, 10, 27, 15, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(roundedHours).isEqualTo(expected);
        expected = new DateTime(2012, 10, 26, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(roundedDays).isEqualTo(expected);
        expected = new DateTime(2012, 10, 14, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(roundedWeeks).isEqualTo(expected);
        expected = new DateTime(2012, 9, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(roundedMonths).isEqualTo(expected);
        expected = new DateTime(2011, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(roundedYears).isEqualTo(expected);
    }

    @Test
    public void winterTimeShouldBeHandledProperly() throws Exception {
        // given a timestamp on a day with winter time change
        long timestamp = new DateTime(2012, 10, 28, 16, 18, 41, 246, DateTimeZone.UTC).getMillis();

        // when rounding it to the previous day
        long roundedDays = TimeUtils.getRoundedTimestamp(timestamp, TimeUnit.DAY);

        // then should be 0AM (and not 1AM)
        long expected = new DateTime(2012, 10, 28, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(roundedDays).isEqualTo(expected);
    }
}
