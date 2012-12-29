package com.ninja_squad.console.repository;

import com.ninja_squad.console.model.ExchangeStatistic;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ExchangeStatRepository extends MongoRepository<ExchangeStatistic, String> {

    List<ExchangeStatistic> findByHandledExistsOrderByTimestampAsc(boolean exists, Pageable pageable);

}
