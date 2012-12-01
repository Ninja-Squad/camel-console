package com.ninja_squad.console.repository;

import com.ninja_squad.console.model.Statistic;
import com.ninja_squad.console.model.TimeUnit;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface StatisticRepository extends MongoRepository<Statistic, String>, StatisticRepositoryCustom {

    /**
     * Finds the statistic for an element, a range and a timunit
     *
     * @param elementId the element id (route id or step id, or overall)
     * @param range     the starting time in millisecond
     * @param timeUnit  the time unit {@link TimeUnit}
     * @return the statistic
     */
    Statistic findOneByElementIdAndRangeAndTimeUnit(String elementId, long range, TimeUnit timeUnit);

    /**
     * Finds all statistics for an element and a time unit
     *
     * @param elementId the element id (route id or step id, or overall)
     * @param timeUnit  the time unit {@link TimeUnit}
     * @return all the statistics for this element and time unit
     */
    List<Statistic> findAllByElementIdAndTimeUnit(String elementId, TimeUnit timeUnit);

}
