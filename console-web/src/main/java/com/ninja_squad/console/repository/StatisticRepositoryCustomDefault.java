package com.ninja_squad.console.repository;

import com.google.common.collect.Lists;
import com.ninja_squad.console.model.MapReducedStatistic;
import com.ninja_squad.console.model.Statistic;
import com.ninja_squad.console.model.TimeUnit;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapreduce.MapReduceResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.List;

import static org.springframework.data.mongodb.core.mapreduce.MapReduceOptions.options;

/**
 * Specific query for mapreducing stats.
 * You can try it in mongo shell
 * <p/>
 * mapreduce in mongo shell
 * db.statistics.mapReduce(m, r, {out: { inline : 1}, query: {timeUnit: "SECOND", elementId:""}})
 * <p/>
 * map function in mongo shell
 * var m = function(){ emit(this.elementId, {completed: this.completed} ); };
 * <p/>
 * reduce function in mongo shell
 * var r = function(k,v){ var count = 0; v.forEach(function(value){ count += value.completed}); return count;};
 */
@Slf4j
public class StatisticRepositoryCustomDefault implements StatisticRepositoryCustom {

    @Inject
    @Setter
    private MongoTemplate mongoTemplate;

    @Override
    public Statistic aggregateStatistics(String elementId, long from, long to) {
        // building query
        Query query = buildQuery(elementId, from, to);

        // map reducing
        MapReduceResults<MapReducedStatistic> aggregation =
                mongoTemplate.mapReduce(
                        // query and collection
                        query, "statistics",
                        // map and reduce functions
                        "classpath:js/map_statistic.js", "classpath:js/reduce_statistic.js",
                        // options and target class
                        options().outputTypeInline(), MapReducedStatistic.class);

        List<MapReducedStatistic> statistics = Lists.newArrayList(aggregation);
        log.debug("stats mapreduced " + statistics);
        //if no result
        if (CollectionUtils.isEmpty(statistics)) {
            return buildDefaultStatistic(elementId, from);
        }
        Statistic statistic = statistics.get(0).getValue();
        if (statistic == null) {
            return buildDefaultStatistic(elementId, from);
        }

        // setting range and elementId before return
        statistic.setRange(from);
        statistic.setElementId(elementId);
        return statistic;
    }

    private Query buildQuery(String elementId, long from, long to) {
        Criteria criteria = new Criteria().andOperator(
                // only for this element
                Criteria.where("elementId").is(elementId),
                // with a range greater or equal than from
                Criteria.where("range").gte(from),
                // with a range lesser or equal than to
                Criteria.where("range").lte(to));
        return Query.query(criteria);
    }

    private Statistic buildDefaultStatistic(String elementId, long from) {
        return new Statistic(elementId, from, TimeUnit.SECOND, 0, 0, 0, 0, 0);
    }
}
