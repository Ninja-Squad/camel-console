package com.ninja_squad.console.subscriber;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
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

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executors;

@Slf4j
public class NotificationSubscriber {

    @Inject
    @Setter
    private ExchangeStatRepository exchangeStatRepository;

    @Inject
    @Setter
    private StatisticRepository statisticRepository;

    @Inject
    @Setter
    private RouteStatisticRepository routeStatisticRepository;

    //@PostConstruct
    public void subscribe() {
        log.debug("Start subscribing");
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                pendingRouteStats();
                pendingExchangeStats();
            }
        }, 0, 1, java.util.concurrent.TimeUnit.MINUTES);
    }

    protected void pendingRouteStats() {
        List<RouteStatistic> routeStatistics = getPendingRouteStatistics();
        NotificationSubscriber.log.info(routeStatistics.size() + " pending stats");
        List<Statistic> statistics = Lists.newArrayList();
        for (RouteStatistic routeStatistic : routeStatistics) {
            // add exchangeStatistic to each range
            long timestamp = routeStatistic.getTimestamp();
            boolean isFailed = routeStatistic.isFailed();
            for (TimeUnit unit : TimeUnit.values()) {
                String routeId = routeStatistic.getRouteId();
                int duration = routeStatistic.getDuration();
                Statistic statistic = updateStatisticForElement(routeId, unit, timestamp, duration, isFailed);
                statistics.add(statistic);
            }
            routeStatistic.setHandled(true);
        }
        routeStatisticRepository.save(routeStatistics);
        statisticRepository.save(statistics);
    }

    protected void pendingExchangeStats() {
        List<ExchangeStatistic> pendingExchangeStats = getPendingExchangeStats();
        NotificationSubscriber.log.info(pendingExchangeStats.size() + " pending notifications");
        List<Statistic> statistics = Lists.newArrayList();
        for (ExchangeStatistic exchangeStat : pendingExchangeStats) {
            // compute duration
            int duration = computeDuration(exchangeStat);
            exchangeStat.setDuration(duration);

            // add exchangeStatistic to each range
            long timestamp = exchangeStat.getTimestamp();
            boolean isFailed = exchangeStat.isFailed();
            for (TimeUnit unit : TimeUnit.values()) {
                Statistic statistic = updateStatisticForElement(Statistic.ALL, unit, timestamp, duration, isFailed);
                statistics.add(statistic);
            }

            Collection<StepStatistic> steps = exchangeStat.getSteps();
            for (StepStatistic stepStatistic : steps) {
                for (TimeUnit unit : TimeUnit.values()) {
                    String elementId = stepStatistic.getDestination();
                    timestamp = stepStatistic.getTimestamp();
                    duration = (int) stepStatistic.getDuration();
                    isFailed = stepStatistic.isFailed();
                    Statistic statistic = updateStatisticForElement(elementId, unit, timestamp, duration, isFailed);
                    statistics.add(statistic);
                }
            }

            // notification is not pending anymore
            exchangeStat.setHandled(true);
        }
        exchangeStatRepository.save(pendingExchangeStats);
        statisticRepository.save(statistics);
    }

    protected Statistic updateStatisticForElement(String elementId, TimeUnit unit, long timestamp, int duration, boolean isFailed) {
        long range = TimeUtils.getRoundedTimestamp(timestamp, unit);
        Statistic statistic = statisticRepository.findOneByElementIdAndRangeAndTimeUnit(elementId, range, unit);
        if (statistic == null) {
            // create a new one
            statistic = new Statistic(elementId, range, unit,
                    isFailed ? 1 : 0, isFailed ? 0 : 1, duration, duration, duration);
        } else {
            // update existing one
            statistic = updateStatistic(statistic, duration, isFailed);
        }
        return statistic;
    }

    protected Statistic updateStatistic(Statistic statistic, int duration, boolean failed) {
        if (failed) { statistic.addFailed(); } else { statistic.addCompleted(duration); }
        return statistic;
    }

    protected int computeDuration(ExchangeStatistic exchangeStatistic) {
        List<StepStatistic> stepStatistics = getOrderedSteps(exchangeStatistic);
        if (stepStatistics.isEmpty()) { return 0; }
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
        return exchangeStatRepository.findByHandledExistsOrderByTimestampAsc(false);
    }

    private List<RouteStatistic> getPendingRouteStatistics() {
        return routeStatisticRepository.findByHandledExistsOrderByTimestampAsc(false);
    }
}
