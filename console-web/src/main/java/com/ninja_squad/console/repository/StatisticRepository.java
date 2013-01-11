package com.ninja_squad.console.repository;

import com.ninja_squad.console.model.Statistic;
import com.ninja_squad.console.model.TimeUnit;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface StatisticRepository extends MongoRepository<Statistic, String>, StatisticRepositoryCustom {

    /**
     * Finds all statistics for an element and a time unit.
     *
     * @param elementId the element id (route id or step id, or overall).
     * @param timeUnit  the time unit {@link TimeUnit}.
     * @return all the statistics for this element and time unit.
     */
    List<Statistic> findAllByElementIdAndTimeUnit(String elementId, TimeUnit timeUnit);

    /**
     * Finds all statistics for all element and time unit between min and max timestamp.
     *
     * @param min      the lower bound timestamp.
     * @param timeUnit the time unit {@link TimeUnit}.
     * @param max      the higher bound timestamp.
     * @return all the statistics between the bounds.
     */
    List<Statistic> findAllByRangeBetweenAndTimeUnit(Long min, Long max, TimeUnit timeUnit);
}
