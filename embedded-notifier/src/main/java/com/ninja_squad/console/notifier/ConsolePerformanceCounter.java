package com.ninja_squad.console.notifier;

import com.ninja_squad.console.RouteStatistic;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.api.management.PerformanceCounter;
import org.joda.time.DateTime;

@Slf4j
public class ConsolePerformanceCounter implements PerformanceCounter {

    private String routeId;
    private ConsoleRepository repository;

    public ConsolePerformanceCounter(String element, ConsoleRepository repository) {
        log.debug("Performance Counter for node " + element);
        this.routeId = element;
        this.repository = repository;
    }

    public synchronized void completedExchange(Exchange exchange, long time) {
        log.debug("exchange completed on " + routeId + " in " + time + "ms from " + this.toString());
        RouteStatistic routeStatistic = new RouteStatistic(DateTime.now().getMillis(), exchange.getExchangeId(), routeId, false, (int) time);
        repository.save(routeStatistic);
    }

    public synchronized void failedExchange(Exchange exchange) {
        log.debug("exchange failed on " + routeId + " from " + this.toString());
        RouteStatistic routeStatistic = new RouteStatistic(DateTime.now().getMillis(), exchange.getExchangeId(), routeId, true, 0);
        repository.save(routeStatistic);
    }

    @Override
    public boolean isStatisticsEnabled() {
        return true;
    }

    @Override
    public void setStatisticsEnabled(boolean statisticsEnabled) {
        // nothing to do
    }

}