package com.ninja_squad.console.controller;

import com.google.common.collect.Lists;
import com.ninja_squad.console.model.Statistic;
import com.ninja_squad.console.model.TimeUnit;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class StatisticControllerTest {

    @Test
    public void shouldAddNullValueForMissingPoints() throws Exception {
        // given a time series with missing value
        StatisticController controller = new StatisticController();
        Statistic count1 = new Statistic("route1", 1351153523000L, TimeUnit.SECOND, 0, 1, 100, 100, 100);
        Statistic count2 = new Statistic("route1", 1351153525000L, TimeUnit.SECOND, 0, 1, 100, 100, 100);
        Statistic count3 = new Statistic("route1", 1351153529000L, TimeUnit.SECOND, 0, 1, 100, 100, 100);
        List<Statistic> counts = Lists.newArrayList(count1, count2, count3);

        // when fillMissingValues
        DateTime now = new DateTime(2012, 10, 25, 8, 25, 30, DateTimeZone.UTC);
        List<Statistic> stats = controller.fillMissingValues(counts, TimeUnit.SECOND, null, null, now);

        // then the result should have null values to fill the missing datas
        assertThat(stats).containsExactly(count1,
                new Statistic("route1", 1351153524000L, TimeUnit.SECOND, 0, 0, 0, 0, 0),
                count2,
                new Statistic("route1", 1351153526000L, TimeUnit.SECOND, 0, 0, 0, 0, 0),
                new Statistic("route1", 1351153527000L, TimeUnit.SECOND, 0, 0, 0, 0, 0),
                new Statistic("route1", 1351153528000L, TimeUnit.SECOND, 0, 0, 0, 0, 0),
                count3,
                new Statistic("route1", 1351153530000L, TimeUnit.SECOND, 0, 0, 0, 0, 0),
                new Statistic("route1", 1351153531000L, TimeUnit.SECOND, 0, 0, 0, 0, 0)
        );
    }

    @Test
    public void shouldAddNullValueForMissingPointsButNotDuplicate() throws Exception {
        // given a time series with missing value
        StatisticController controller = new StatisticController();
        long january = new DateTime(2012, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        Statistic count1 = new Statistic("route1", january, TimeUnit.MONTH, 0, 1, 100, 100, 100);
        long march = new DateTime(2012, 3, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        Statistic count2 = new Statistic("route1", march, TimeUnit.MONTH, 0, 1, 100, 100, 100);
        long september = new DateTime(2012, 9, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        Statistic count3 = new Statistic("route1", september, TimeUnit.MONTH, 0, 1, 100, 100, 100);
        List<Statistic> counts = Lists.newArrayList(count1, count2, count3);

        // when fillMissingValues
        DateTime now = new DateTime(2012, 10, 24, 0, 0, 0, DateTimeZone.UTC);
        List<Statistic> statistics = controller.fillMissingValues(counts, TimeUnit.MONTH, null, null, now);

        // then the result should have zero values to fill the missing datas
        long february = new DateTime(2012, 2, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        long april = new DateTime(2012, 4, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        long may = new DateTime(2012, 5, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        long june = new DateTime(2012, 6, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        long july = new DateTime(2012, 7, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        long august = new DateTime(2012, 8, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        long october = new DateTime(2012, 10, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(statistics).contains(count1,
                new Statistic("route1", february, TimeUnit.MONTH, 0, 0, 0, 0, 0),
                count2,
                new Statistic("route1", april, TimeUnit.MONTH, 0, 0, 0, 0, 0),
                new Statistic("route1", may, TimeUnit.MONTH, 0, 0, 0, 0, 0),
                new Statistic("route1", june, TimeUnit.MONTH, 0, 0, 0, 0, 0),
                new Statistic("route1", july, TimeUnit.MONTH, 0, 0, 0, 0, 0),
                new Statistic("route1", august, TimeUnit.MONTH, 0, 0, 0, 0, 0),
                count3,
                new Statistic("route1", october, TimeUnit.MONTH, 0, 0, 0, 0, 0)
        );
    }

}
