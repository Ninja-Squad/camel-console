package com.ninja_squad.console.repository;

import com.ninja_squad.console.model.RouteStatistic;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RouteStatisticRepository extends MongoRepository<RouteStatistic, String> {

    List<RouteStatistic> findByHandledExistsOrderByTimestampAsc(boolean exists);

}
