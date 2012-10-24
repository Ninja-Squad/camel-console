package com.ninja_squad.console.repository;

import com.google.common.collect.Lists;
import com.ninja_squad.console.model.TimestampCount;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapreduce.MapReduceOptions;
import org.springframework.data.mongodb.core.mapreduce.MapReduceResults;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.mongodb.core.mapreduce.MapReduceOptions.options;

@Slf4j
public class MessageDefaultRepository implements MessageRepositoryCustom {

    @Inject
    @Setter
    private MongoTemplate mongoTemplate;

    @Override
    public List<TimestampCount> getNotificationBySecond() {
        MapReduceResults<TimestampCount> count =
                mongoTemplate.mapReduce("routes",
                        "classpath:js/map.js",
                        "classpath:js/reduce.js",
                        options().finalizeFunction("classpath:js/finalize.js").outputTypeInline(),
                        TimestampCount.class);
        List<TimestampCount> timestampCounts = Lists.newArrayList(count);
        log.info("MapReducer " + timestampCounts.toString());
        return timestampCounts;
    }

}
