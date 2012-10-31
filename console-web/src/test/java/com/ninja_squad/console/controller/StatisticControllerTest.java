package com.ninja_squad.console.controller;

import com.google.common.collect.Lists;
import com.ninja_squad.console.model.Statistic;
import com.ninja_squad.console.model.TimeUnit;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class StatisticControllerTest {

    @Test
    public void shouldAddNullValueForMissingPoints() throws Exception {
        // given a time series with missing value
        StatisticController controller = new StatisticController();
        Statistic count1 = new Statistic(1351153523000L, TimeUnit.SECONDS, 0, 1, 100, 100, 100);
        Statistic count2 = new Statistic(1351153525000L, TimeUnit.SECONDS, 0, 1, 100, 100, 100);
        Statistic count3 = new Statistic(1351153529000L, TimeUnit.SECONDS, 0, 1, 100, 100, 100);
        List<Statistic> counts = Lists.newArrayList(count1, count2, count3);

        // when toJson
        DateTime now = new DateTime(2012, 10, 24, 00, 00, 00);
        String json = controller.toJson(counts, now);

        // then the result should have null values to fill the missing datas
        assertThat(json).isEqualTo("[[1351153523000,0,1,100,100,100], [1351153524000,0,0,0,0,0], " +
                "[1351153525000,0,1,100,100,100], [1351153526000,0,0,0,0,0], " +
                "[1351153528000,0,0,0,0,0], [1351153529000,0,1,100,100,100], " +
                "[1351153530000,0,0,0,0,0], [1351029600000,0,0,0,0,0]]");
    }
}
