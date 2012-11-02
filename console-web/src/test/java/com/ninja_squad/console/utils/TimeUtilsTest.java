package com.ninja_squad.console.utils;

import com.ninja_squad.console.model.TimeUnit;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

public class TimeUtilsTest {
    @Test
    public void getRoundedTimestampShouldReturnRoundedTimestamp() throws Exception {
        // given a timestamp
        long timestamp = 1351523921246L;
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
        assertThat(roundedSeconds).isEqualTo(1351523921000L);
        assertThat(roundedMinutes).isEqualTo(1351523880000L);
        assertThat(roundedHours).isEqualTo(1351522800000L);
        assertThat(roundedDays).isEqualTo(1351465200000L);
        assertThat(roundedWeeks).isEqualTo(1351375200000L);
        assertThat(roundedMonths).isEqualTo(1349042400000L);
        assertThat(roundedYears).isEqualTo(1325372400000L);
        verify(timeUtils, times(13)).getRoundedTimestamp(anyLong(), any(TimeUnit.class));
    }
}
