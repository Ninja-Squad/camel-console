package com.ninja_squad.console.repository;

import com.ninja_squad.console.model.Statistic;

public interface StatisticRepositoryCustom {

    /**
     * Aggregates the statistics for an element from a timestamp to another timestamp in one statistic
     *
     * @param elementId the element id (route id or step id, or overall)
     * @param from      the starting time in millisecond
     * @param to        the ending time in millisecond
     * @return only one statistic with all fields aggregated
     */
    Statistic aggregateStatistics(String elementId, long from, long to);
}
