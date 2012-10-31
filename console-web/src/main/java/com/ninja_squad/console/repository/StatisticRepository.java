package com.ninja_squad.console.repository;

import com.ninja_squad.console.model.Statistic;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StatisticRepository extends MongoRepository<Statistic, String> {

    Statistic findOneByRange(long range);

}
