package com.ninja_squad.console.notifier;

import org.apache.camel.Exchange;
import org.apache.camel.api.management.PerformanceCounter;
import org.apache.camel.api.management.mbean.ManagedPerformanceCounterMBean;
import org.apache.camel.management.mbean.Statistic;
import org.apache.camel.util.ExchangeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class ConsolePerformanceCounter implements PerformanceCounter, ManagedPerformanceCounterMBean {

    private Statistic exchangesTotal;
    private Statistic exchangesCompleted;
    private Statistic exchangesFailed;
    private Statistic failuresHandled;
    private Statistic redeliveries;
    private Statistic externalRedeliveries;
    private Statistic minProcessingTime;
    private Statistic maxProcessingTime;
    private Statistic totalProcessingTime;
    private Statistic lastProcessingTime;
    private Statistic meanProcessingTime;
    private Statistic firstExchangeCompletedTimestamp;
    private String firstExchangeCompletedExchangeId;
    private Statistic firstExchangeFailureTimestamp;
    private String firstExchangeFailureExchangeId;
    private Statistic lastExchangeCompletedTimestamp;
    private String lastExchangeCompletedExchangeId;
    private Statistic lastExchangeFailureTimestamp;
    private String lastExchangeFailureExchangeId;
    private boolean statisticsEnabled = true;
    private Logger log = LoggerFactory.getLogger(getClass());

    public ConsolePerformanceCounter(String element) {
        log.debug("Performance Counter for node " + element);
        this.exchangesTotal = new Statistic("org.apache.camel.exchangesTotal", this, Statistic.UpdateMode.COUNTER);
        this.exchangesCompleted = new Statistic("org.apache.camel.exchangesCompleted", this, Statistic.UpdateMode.COUNTER);
        this.exchangesFailed = new Statistic("org.apache.camel.exchangesFailed", this, Statistic.UpdateMode.COUNTER);

        this.failuresHandled = new Statistic("org.apache.camel.failuresHandled", this, Statistic.UpdateMode.COUNTER);
        this.redeliveries = new Statistic("org.apache.camel.redeliveries", this, Statistic.UpdateMode.COUNTER);
        this.externalRedeliveries = new Statistic("org.apache.camel.externalRedeliveries", this, Statistic.UpdateMode.COUNTER);

        this.minProcessingTime = new Statistic("org.apache.camel.minimumProcessingTime", this, Statistic.UpdateMode.MINIMUM);
        this.maxProcessingTime = new Statistic("org.apache.camel.maximumProcessingTime", this, Statistic.UpdateMode.MAXIMUM);
        this.totalProcessingTime = new Statistic("org.apache.camel.totalProcessingTime", this, Statistic.UpdateMode.COUNTER);
        this.lastProcessingTime = new Statistic("org.apache.camel.lastProcessingTime", this, Statistic.UpdateMode.VALUE);
        this.meanProcessingTime = new Statistic("org.apache.camel.meanProcessingTime", this, Statistic.UpdateMode.VALUE);

        this.firstExchangeCompletedTimestamp = new Statistic("org.apache.camel.firstExchangeCompletedTimestamp", this, Statistic.UpdateMode.VALUE);
        this.firstExchangeFailureTimestamp = new Statistic("org.apache.camel.firstExchangeFailureTimestamp", this, Statistic.UpdateMode.VALUE);
        this.lastExchangeCompletedTimestamp = new Statistic("org.apache.camel.lastExchangeCompletedTimestamp", this, Statistic.UpdateMode.VALUE);
        this.lastExchangeFailureTimestamp = new Statistic("org.apache.camel.lastExchangeFailureTimestamp", this, Statistic.UpdateMode.VALUE);
    }

    @Override
    public synchronized void reset() {
        exchangesTotal.reset();
        exchangesCompleted.reset();
        exchangesFailed.reset();
        failuresHandled.reset();
        redeliveries.reset();
        externalRedeliveries.reset();
        minProcessingTime.reset();
        maxProcessingTime.reset();
        totalProcessingTime.reset();
        lastProcessingTime.reset();
        meanProcessingTime.reset();
        firstExchangeCompletedTimestamp.reset();
        firstExchangeCompletedExchangeId = null;
        firstExchangeFailureTimestamp.reset();
        firstExchangeFailureExchangeId = null;
        lastExchangeCompletedTimestamp.reset();
        lastExchangeCompletedExchangeId = null;
        lastExchangeFailureTimestamp.reset();
        lastExchangeFailureExchangeId = null;
    }

    @Override
    public long getExchangesTotal() throws Exception {
        return exchangesTotal.getValue();
    }

    public long getExchangesCompleted() throws Exception {
        return exchangesCompleted.getValue();
    }

    public long getExchangesFailed() throws Exception {
        return exchangesFailed.getValue();
    }

    public long getFailuresHandled() throws Exception {
        return failuresHandled.getValue();
    }

    public long getRedeliveries() throws Exception {
        return redeliveries.getValue();
    }

    public long getExternalRedeliveries() throws Exception {
        return externalRedeliveries.getValue();
    }

    public long getMinProcessingTime() throws Exception {
        return minProcessingTime.getValue();
    }

    public long getMeanProcessingTime() throws Exception {
        return meanProcessingTime.getValue();
    }

    public long getMaxProcessingTime() throws Exception {
        return maxProcessingTime.getValue();
    }

    public long getTotalProcessingTime() throws Exception {
        return totalProcessingTime.getValue();
    }

    public long getLastProcessingTime() throws Exception {
        return lastProcessingTime.getValue();
    }

    public Date getLastExchangeCompletedTimestamp() {
        long value = lastExchangeCompletedTimestamp.getValue();
        return value > 0 ? new Date(value) : null;
    }

    public String getLastExchangeCompletedExchangeId() {
        return lastExchangeCompletedExchangeId;
    }

    public Date getFirstExchangeCompletedTimestamp() {
        long value = firstExchangeCompletedTimestamp.getValue();
        return value > 0 ? new Date(value) : null;
    }

    public String getFirstExchangeCompletedExchangeId() {
        return firstExchangeCompletedExchangeId;
    }

    public Date getLastExchangeFailureTimestamp() {
        long value = lastExchangeFailureTimestamp.getValue();
        return value > 0 ? new Date(value) : null;
    }

    public String getLastExchangeFailureExchangeId() {
        return lastExchangeFailureExchangeId;
    }

    public Date getFirstExchangeFailureTimestamp() {
        long value = firstExchangeFailureTimestamp.getValue();
        return value > 0 ? new Date(value) : null;
    }

    public String getFirstExchangeFailureExchangeId() {
        return firstExchangeFailureExchangeId;
    }

    public boolean isStatisticsEnabled() {
        return statisticsEnabled;
    }

    public void setStatisticsEnabled(boolean statisticsEnabled) {
        this.statisticsEnabled = statisticsEnabled;
    }

    public synchronized void completedExchange(Exchange exchange, long time) {
        log.debug("exchange completed on " + exchange.getFromRouteId() + " in " + time + "ms from " + this.toString());
        exchangesTotal.increment();
        exchangesCompleted.increment();

        if (ExchangeHelper.isFailureHandled(exchange)) {
            failuresHandled.increment();
        }
        Boolean externalRedelivered = exchange.isExternalRedelivered();
        if (externalRedelivered != null && externalRedelivered) {
            externalRedeliveries.increment();
        }

        minProcessingTime.updateValue(time);
        maxProcessingTime.updateValue(time);
        totalProcessingTime.updateValue(time);
        lastProcessingTime.updateValue(time);

        long now = new Date().getTime();
        if (firstExchangeCompletedTimestamp.getUpdateCount() == 0) {
            firstExchangeCompletedTimestamp.updateValue(now);
        }

        lastExchangeCompletedTimestamp.updateValue(now);
        if (firstExchangeCompletedExchangeId == null) {
            firstExchangeCompletedExchangeId = exchange.getExchangeId();
        }
        lastExchangeCompletedExchangeId = exchange.getExchangeId();

        // update mean
        long count = exchangesCompleted.getValue();
        long mean = count > 0 ? totalProcessingTime.getValue() / count : 0;
        meanProcessingTime.updateValue(mean);
    }

    public synchronized void failedExchange(Exchange exchange) {
        exchangesTotal.increment();
        exchangesFailed.increment();

        if (ExchangeHelper.isRedelivered(exchange)) {
            redeliveries.increment();
        }
        Boolean externalRedelivered = exchange.isExternalRedelivered();
        if (externalRedelivered != null && externalRedelivered) {
            externalRedeliveries.increment();
        }

        long now = new Date().getTime();
        if (firstExchangeFailureTimestamp.getUpdateCount() == 0) {
            firstExchangeFailureTimestamp.updateValue(now);
        }

        lastExchangeFailureTimestamp.updateValue(now);
        if (firstExchangeFailureExchangeId == null) {
            firstExchangeFailureExchangeId = exchange.getExchangeId();
        }
        lastExchangeFailureExchangeId = exchange.getExchangeId();
    }

    public String dumpStatsAsXml(boolean fullStats) {
        return null;
    }
}