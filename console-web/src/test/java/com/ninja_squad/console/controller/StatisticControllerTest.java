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

        // when toJson
        DateTime now = new DateTime(2012, 10, 30, 0, 0, 0, DateTimeZone.UTC);
        String json = controller.toJson(counts, TimeUnit.SECOND, null, null, now);

        // then the result should have null values to fill the missing datas
        assertThat(json).isEqualTo("[[1351153523000,0,1,100,100,100], [1351153524000,0,0,0,0,0], " +
                "[1351153525000,0,1,100,100,100], [1351153526000,0,0,0,0,0], " +
                "[1351153528000,0,0,0,0,0], [1351153529000,0,1,100,100,100], " +
                "[1351153530000,0,0,0,0,0], [1351555200000,0,0,0,0,0]]");
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

        // when toJson
        DateTime now = new DateTime(2012, 10, 24, 0, 0, 0, DateTimeZone.UTC);
        String json = controller.toJson(counts, TimeUnit.MONTH, null, null, now);

        // then the result should have null values to fill the missing datas
        long february = new DateTime(2012, 2, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        long april = new DateTime(2012, 4, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        long august = new DateTime(2012, 8, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        long october = new DateTime(2012, 10, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertThat(json).isEqualTo("[[" + january + ",0,1,100,100,100], [" + february + ",0,0,0,0,0], " +
                "[" + march + ",0,1,100,100,100], [" + april + ",0,0,0,0,0], " +
                "[" + august + ",0,0,0,0,0], [" + september + ",0,1,100,100,100], " +
                "[" + october + ",0,0,0,0,0]]");
    }

    @Test
    public void filterRangeShouldRemoveOutOfRangeStats() throws Exception {
        // given some stats
        long february = new DateTime(2012, 2, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        long april = new DateTime(2012, 4, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        long august = new DateTime(2012, 8, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        long october = new DateTime(2012, 10, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        List<Statistic> statistics = Lists.newArrayList(new Statistic("route1", february, TimeUnit.MONTH, 0, 0, 0, 0, 0),
                new Statistic("route1", april, TimeUnit.MONTH, 0, 0, 0, 0, 0),
                new Statistic("route1", august, TimeUnit.MONTH, 0, 0, 0, 0, 0),
                new Statistic("route1", october, TimeUnit.MONTH, 0, 0, 0, 0, 0));

        // when we filter from april to september
        long september = new DateTime(2012, 9, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        List<Statistic> filtered = new StatisticController().filterRanges(statistics, april, september);

        // then the stats out of range should be removed
        assertThat(filtered).hasSize(2);
        assertThat(filtered).contains(new Statistic("route1", april, TimeUnit.MONTH, 0, 0, 0, 0, 0),
                new Statistic("route1", august, TimeUnit.MONTH, 0, 0, 0, 0, 0));
    }

    @Test
    public void filterRangeShouldRemoveOutOfRangeStatsWithFromNull() throws Exception {
        // given some stats
        long february = new DateTime(2012, 2, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        long april = new DateTime(2012, 4, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        long august = new DateTime(2012, 8, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        long october = new DateTime(2012, 10, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        List<Statistic> statistics = Lists.newArrayList(new Statistic("route1", february, TimeUnit.MONTH, 0, 0, 0, 0, 0),
                new Statistic("route1", april, TimeUnit.MONTH, 0, 0, 0, 0, 0),
                new Statistic("route1", august, TimeUnit.MONTH, 0, 0, 0, 0, 0),
                new Statistic("route1", october, TimeUnit.MONTH, 0, 0, 0, 0, 0));

        // when we filter from april to september
        long september = new DateTime(2012, 9, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        List<Statistic> filtered = new StatisticController().filterRanges(statistics, null, september);

        // then the stats out of range should be removed
        assertThat(filtered).hasSize(3);
        assertThat(filtered).contains(new Statistic("route1", february, TimeUnit.MONTH, 0, 0, 0, 0, 0),
                new Statistic("route1", april, TimeUnit.MONTH, 0, 0, 0, 0, 0),
                new Statistic("route1", august, TimeUnit.MONTH, 0, 0, 0, 0, 0));
    }

    public void filterRangeShouldRemoveOutOfRangeStatsWithToNull() throws Exception {
        // given some stats
        long february = new DateTime(2012, 2, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        long april = new DateTime(2012, 4, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        long august = new DateTime(2012, 8, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        long october = new DateTime(2012, 10, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        List<Statistic> statistics = Lists.newArrayList(new Statistic("route1", february, TimeUnit.MONTH, 0, 0, 0, 0, 0),
                new Statistic("route1", april, TimeUnit.MONTH, 0, 0, 0, 0, 0),
                new Statistic("route1", august, TimeUnit.MONTH, 0, 0, 0, 0, 0),
                new Statistic("route1", october, TimeUnit.MONTH, 0, 0, 0, 0, 0));

        // when we filter from april to september
        long september = new DateTime(2012, 9, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        List<Statistic> filtered = new StatisticController().filterRanges(statistics, april, null);

        // then the stats out of range should be removed
        assertThat(filtered).hasSize(3);
        assertThat(filtered).contains(new Statistic("route1", april, TimeUnit.MONTH, 0, 0, 0, 0, 0),
                new Statistic("route1", august, TimeUnit.MONTH, 0, 0, 0, 0, 0),
                new Statistic("route1", october, TimeUnit.MONTH, 0, 0, 0, 0, 0));
    }
}
