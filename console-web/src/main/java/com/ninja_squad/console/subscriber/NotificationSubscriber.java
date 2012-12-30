package com.ninja_squad.console.subscriber;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import com.mongodb.WriteResult;
import com.ninja_squad.console.StepStatistic;
import com.ninja_squad.console.model.ExchangeStatistic;
import com.ninja_squad.console.model.RouteStatistic;
import com.ninja_squad.console.model.Statistic;
import com.ninja_squad.console.model.TimeUnit;
import com.ninja_squad.console.repository.ExchangeStatRepository;
import com.ninja_squad.console.repository.RouteStatisticRepository;
import com.ninja_squad.console.repository.StatisticRepository;
import com.ninja_squad.console.utils.TimeUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

@Slf4j
public class NotificationSubscriber {

    public static final int ROUTE_SIZE = 8000;
    public static final int EXCHANGE_SIZE = 10000;

    @Inject
    @Setter
    private ExchangeStatRepository exchangeStatRepository;

    @Inject
    @Setter
    private StatisticRepository statisticRepository;

    @Inject
    @Setter
    private RouteStatisticRepository routeStatisticRepository;

    @Inject
    @Setter
    private MongoTemplate mongoTemplate;

    @PostConstruct
    public void subscribe() {
        log.info("Start subscribing");
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                pendingRouteStats();
                pendingExchangeStats();
            }
        }, 0, 1, java.util.concurrent.TimeUnit.MINUTES);
    }

    protected void pendingRouteStats() {
        // pending messages
        List<RouteStatistic> routeStatistics = getPendingRouteStatistics();
        log.info(routeStatistics.size() + " pending stats");

        if (Iterables.isEmpty(routeStatistics)) {
            return;
        }

        // concerned stats
        Iterable<Long> timestamps = getTimestampsRoute(routeStatistics);
        List<Statistic> concernedStats = getConcernedStats(timestamps);

        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start();
        for (final RouteStatistic routeStatistic : routeStatistics) {
            // add stat to each time unit
            long timestamp = routeStatistic.getTimestamp();
            boolean isFailed = routeStatistic.isFailed();
            String routeId = routeStatistic.getRouteId();
            int duration = routeStatistic.getDuration();
            for (TimeUnit unit : TimeUnit.values()) {
                updateStatisticForElement(routeId, unit, timestamp, duration, isFailed, concernedStats);
            }
        }
        long computeTime = stopwatch.elapsedMillis();
        Set<String> ids = Sets.newHashSet(Iterables.transform(routeStatistics, new Function<RouteStatistic, String>() {
            @Override
            public String apply(RouteStatistic input) {
                return input.getId();
            }
        }));
        WriteResult writeResult = mongoTemplate.updateMulti(
                new Query(Criteria.where("_id").in(ids)),
                new Update().set("handled", true),
                RouteStatistic.class);
        if (writeResult.getError() != null) {
            log.error(writeResult.getError());
        }
        statisticRepository.saveAll(concernedStats);
        log.info(writeResult.getN() + " stats done in "
                + stopwatch.elapsedMillis() + " ms ("
                + computeTime + "/" + (stopwatch.elapsedMillis()-computeTime)
                + ")-> " + concernedStats.size());
    }

    protected Iterable<Long> getTimestampsRoute(List<RouteStatistic> routeStatistics) {
        return Iterables.transform(routeStatistics, new Function<RouteStatistic, Long>() {
            @Override
            public Long apply(RouteStatistic input) {
                return input.getTimestamp();
            }
        });
    }

    protected Iterable<Long> getTimestampsExchange(List<ExchangeStatistic> exchangeStats) {// get all timestamps
        return Iterables.transform(exchangeStats, new Function<ExchangeStatistic, Long>() {
            @Override
            public Long apply(ExchangeStatistic input) {
                return input.getTimestamp();
            }
        });
    }

    protected List<Statistic> getConcernedStats(Iterable<Long> timestamps) {
        // min and max (with an offset of 1 to include bounds)
        Long min = Ordering.natural().min(timestamps);
        Long max = Ordering.natural().max(timestamps);
        // find stats between
        List<Statistic> statsForEveryTimeUnit = Lists.newArrayList();
        for (TimeUnit unit : TimeUnit.values()) {
            List<Statistic> stats = statisticRepository.findAllByRangeBetweenAndTimeUnit(
                    TimeUtils.getPreviousRange(min, unit),
                    TimeUtils.getNextRange(max, unit),
                    unit);
            statsForEveryTimeUnit.addAll(stats);
        }
        return statsForEveryTimeUnit;
    }

    protected void pendingExchangeStats() {
        // pending messages
        List<ExchangeStatistic> pendingExchangeStats = getPendingExchangeStats();
        log.info(pendingExchangeStats.size() + " pending notifications");

        if (Iterables.isEmpty(pendingExchangeStats)) {
            return;
        }

        // get concerned stats
        Iterable<Long> timestamps = getTimestampsExchange(pendingExchangeStats);
        List<Statistic> concernedStats = getConcernedStats(timestamps);

        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start();
        for (ExchangeStatistic exchangeStat : pendingExchangeStats) {
            // compute duration
            int duration = computeDuration(exchangeStat);
            exchangeStat.setDuration(duration);

            // add exchangeStatistic to each range
            long timestamp = exchangeStat.getTimestamp();
            boolean isFailed = exchangeStat.isFailed();
            for (TimeUnit unit : TimeUnit.values()) {
                updateStatisticForElement(Statistic.ALL, unit, timestamp, duration, isFailed, concernedStats);
            }

            Collection<StepStatistic> steps = exchangeStat.getSteps();
            for (StepStatistic stepStatistic : steps) {
                for (TimeUnit unit : TimeUnit.values()) {
                    String elementId = stepStatistic.getDestination();
                    timestamp = stepStatistic.getTimestamp();
                    duration = (int) stepStatistic.getDuration();
                    isFailed = stepStatistic.isFailed();
                    updateStatisticForElement(elementId, unit, timestamp, duration, isFailed, concernedStats);
                }
            }

            // notification is not pending anymore
            exchangeStat.setHandled(true);
        }
        Set<String> ids = Sets.newHashSet(Iterables.transform(pendingExchangeStats, new Function<ExchangeStatistic, String>() {
            @Override
            public String apply(ExchangeStatistic input) {
                return input.getId();
            }
        }));
        WriteResult writeResult = mongoTemplate.updateMulti(
                new Query(Criteria.where("_id").in(ids)),
                new Update().set("handled", true),
                ExchangeStatistic.class);
        if (writeResult.getError() != null) {
            log.error(writeResult.getError());
        }
        statisticRepository.saveAll(concernedStats);
        log.info(pendingExchangeStats.size() + " notifs done in " + stopwatch.elapsedMillis() + " ms -> " + concernedStats.size());
    }

    protected Statistic updateStatisticForElement(String elementId, TimeUnit unit, long timestamp, int duration, boolean isFailed, List<Statistic> existingStats) {
        long range = TimeUtils.getRoundedTimestamp(timestamp, unit);
        Statistic statistic = findOneByElementIdAndRangeAndTimeUnit(existingStats, elementId, range, unit);
        if (statistic == null) {
            // create a new one
            statistic = new Statistic(elementId, range, unit,
                    isFailed ? 1 : 0, isFailed ? 0 : 1, duration, duration, duration);
            existingStats.add(statistic);
        } else {
            // update existing one
            statistic = updateStatistic(statistic, duration, isFailed);
        }
        // saving it
        return statistic;
    }

    private Statistic findOneByElementIdAndRangeAndTimeUnit(List<Statistic> existingStats, final String elementId, final long range, final TimeUnit unit) {
        Iterable<Statistic> filter = Iterables.filter(existingStats, new Predicate<Statistic>() {
            @Override
            public boolean apply(Statistic input) {
                return input.getElementId().equals(elementId) && input.getRange() == range && input.getTimeUnit().equals(unit);
            }
        });
        return filter.iterator().hasNext() ? filter.iterator().next() : null;
    }

    protected Statistic updateStatistic(Statistic statistic, int duration, boolean failed) {
        if (failed) {
            statistic.addFailed();
        } else {
            statistic.addCompleted(duration);
        }
        return statistic;
    }

    protected int computeDuration(ExchangeStatistic exchangeStatistic) {
        List<StepStatistic> stepStatistics = getOrderedSteps(exchangeStatistic);
        if (stepStatistics.isEmpty()) {
            return 0;
        }
        return (int) (exchangeStatistic.getTimestamp() - stepStatistics.get(0).getTimestamp());
    }

    protected List<StepStatistic> getOrderedSteps(ExchangeStatistic exchangeStatistic) {
        Ordering<StepStatistic> ordering = new Ordering<StepStatistic>() {
            @Override
            public int compare(StepStatistic left, StepStatistic right) {
                return Ints.compare(left.getStep(), right.getStep());
            }
        };
        return ordering.sortedCopy(exchangeStatistic.getSteps());
    }

    protected List<ExchangeStatistic> getPendingExchangeStats() {
        Pageable request = new PageRequest(0, EXCHANGE_SIZE);
        return exchangeStatRepository.findByHandledExistsOrderByTimestampAsc(false, request);
    }

    private List<RouteStatistic> getPendingRouteStatistics() {
        Pageable request = new PageRequest(0, ROUTE_SIZE);
        return routeStatisticRepository.findByHandledExistsOrderByTimestampAsc(false, request);
    }
}
