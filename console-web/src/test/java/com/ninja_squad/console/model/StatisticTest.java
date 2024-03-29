package com.ninja_squad.console.model;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class StatisticTest {

    @Test
    public void addFailed() throws Exception {
        Statistic statistic = new Statistic("route1", 1000L, TimeUnit.SECOND, 0, 10, 10, 100, 50);
        statistic.addFailed();
        assertThat(statistic).isEqualTo(new Statistic("route1", 1000L, TimeUnit.SECOND, 1, 10, 10, 100, 50));
    }

    @Test
    public void addCompletedWithSameAverage() throws Exception {
        Statistic statistic = new Statistic("route1", 1000L, TimeUnit.SECOND, 0, 10, 10, 100, 50);
        statistic.addCompleted(50);
        assertThat(statistic).isEqualTo(new Statistic("route1", 1000L, TimeUnit.SECOND, 0, 11, 10, 100, 50));
    }

    @Test
    public void computeAverageTruncate() throws Exception {
        Statistic statistic = new Statistic("route1", 1000L, TimeUnit.SECOND, 0, 10, 10, 100, 50);
        statistic.addCompleted(104);
        assertThat(statistic).isEqualTo(new Statistic("route1", 1000L, TimeUnit.SECOND, 0, 11, 10, 104, 54));
    }

    @Test
    public void computeAverageExact() throws Exception {
        Statistic statistic = new Statistic("route1", 1000L, TimeUnit.SECOND, 0, 10, 10, 100, 50);
        statistic.addCompleted(105);
        assertThat(statistic).isEqualTo(new Statistic("route1", 1000L, TimeUnit.SECOND, 0, 11, 10, 105, 55));
    }

    @Test
    public void updateMin() throws Exception {
        Statistic statistic = new Statistic("route1", 1000L, TimeUnit.SECOND, 0, 10, 10, 100, 50);
        statistic.addCompleted(4);
        assertThat(statistic).isEqualTo(new Statistic("route1", 1000L, TimeUnit.SECOND, 0, 11, 4, 100, 45));
    }

    @Test
    public void updateMax() throws Exception {
        Statistic statistic = new Statistic("route1", 1000L, TimeUnit.SECOND, 0, 10, 10, 100, 50);
        statistic.addCompleted(104);
        assertThat(statistic).isEqualTo(new Statistic("route1", 1000L, TimeUnit.SECOND, 0, 11, 10, 104, 54));
    }

    @Test
    public void addCompletedWithDifferentAverageAndMax() throws Exception {
        Statistic statistic = new Statistic("route1", 1000L, TimeUnit.SECOND, 0, 10, 10, 100, 50);
        statistic.addCompleted(105);
        assertThat(statistic).isEqualTo(new Statistic("route1", 1000L, TimeUnit.SECOND, 0, 11, 10, 105, 55));
    }

}
