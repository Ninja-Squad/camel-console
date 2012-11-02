package com.ninja_squad.console.repository;

import com.ninja_squad.console.model.Statistic;
import com.ninja_squad.console.model.TimeUnit;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface StatisticRepository extends MongoRepository<Statistic, String> {

    Statistic findOneByRangeAndTimeUnit(long range, TimeUnit timeUnit);

    List<Statistic> findAllByTimeUnit(TimeUnit timeUnit);
}
