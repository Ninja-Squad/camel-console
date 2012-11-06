package com.ninja_squad.console.notifier;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.ninja_squad.console.ExchangeStatistic;
import com.ninja_squad.console.StepStatistic;
import lombok.Setter;
import org.apache.camel.management.event.ExchangeCompletedEvent;
import org.apache.camel.management.event.ExchangeFailedEvent;
import org.apache.camel.support.EventNotifierSupport;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.EventObject;
import java.util.Properties;

public class ConsoleEventNotifier extends EventNotifierSupport {

    @Setter
    private ConsoleRepository repository;

    private Multimap<String, StepStatistic> stepStatisticsPerExchangeId = HashMultimap.create();

    public ConsoleEventNotifier() {
        String property = null;
        String host = null;
        try {
            Properties properties = new Properties();
            properties.load(getClass().getClassLoader().getResourceAsStream("database.properties"));
            property = properties.getProperty("mongodb.port");
            host = properties.getProperty("mongodb.host");
        } catch (Exception e) {
            log.error("no database.properties on classpath : will use default values localhost:27017");
        }
        host = host == null ? "localhost" : host;
        int port = Integer.parseInt(property == null ? "27017" : property);
        this.repository = new ConsoleRepositoryJongo(host, port);
    }

    @Override
    public void notify(EventObject event) throws Exception {
        if (event instanceof ExchangeCompletedEvent) {
            ExchangeCompletedEvent sent = (ExchangeCompletedEvent) event;
            notifyExchangeCompletedEvent(sent);
        } else if (event instanceof ExchangeFailedEvent) {
            ExchangeFailedEvent sent = (ExchangeFailedEvent) event;
            notifyExchangeFailedEvent(sent);
        }
    }

    protected void notifyExchangeCompletedEvent(ExchangeCompletedEvent event) {
        log.debug(event.getExchange().getFromRouteId() + " : " + event.getExchange().getExchangeId() + " completed.");
        //get notifications related
        final String id = event.getExchange().getExchangeId();
        ExchangeStatistic exchangeStatistic = buildExchangeStatistic(id);
        exchangeStatistic.setFailed(false);
        persistExchangeStatistic(exchangeStatistic);
    }

    protected void notifyExchangeFailedEvent(ExchangeFailedEvent event) {
        log.debug(event.getExchange().getFromRouteId() + " : " + event.getExchange().getExchangeId() + " failed.");
        //get notifications related
        final String id = event.getExchange().getExchangeId();
        ExchangeStatistic exchangeStatistic = buildExchangeStatistic(id);
        exchangeStatistic.setFailed(true);
        if (event.getExchange().getException() != null) {
            exchangeStatistic.setException(event.getExchange().getException().getClass().getSimpleName());
            exchangeStatistic.setExceptionMessage(event.getExchange().getException().getMessage());
        }
        persistExchangeStatistic(exchangeStatistic);
    }

    protected synchronized void addStepStatistic(String exchangeId, StepStatistic stepStatistic) {
        //setting step number
        Collection<StepStatistic> steps = stepStatisticsPerExchangeId.get(exchangeId);
        stepStatistic.setStep(steps.size());
        //saving
        stepStatisticsPerExchangeId.put(exchangeId, stepStatistic);
    }

    protected synchronized Collection<StepStatistic> getStepStatistics(String id) {
        return stepStatisticsPerExchangeId.get(id);
    }

    protected synchronized void removeStepStatistics(String id) {
        stepStatisticsPerExchangeId.removeAll(id);
    }

    private void persistExchangeStatistic(ExchangeStatistic exchangeStatistic) {
        repository.save(exchangeStatistic);
        removeStepStatistics(exchangeStatistic.getExchangeId());
    }

    private ExchangeStatistic buildExchangeStatistic(String id) {
        Collection<StepStatistic> steps = getStepStatistics(id);
        log.debug("StepStatistics for event " + id + " : " + steps);
        //persist them
        ExchangeStatistic exchangeStatistic = new ExchangeStatistic();
        exchangeStatistic.setExchangeId(id);
        exchangeStatistic.setSteps(steps);
        exchangeStatistic.setTimestamp(DateTime.now().getMillis());
        return exchangeStatistic;
    }

    @Override
    public boolean isEnabled(EventObject event) {
        return true;
    }

    @Override
    protected void doStart() throws Exception {
        //nothing to do
    }

    @Override
    protected void doStop() throws Exception {
        //nothing to do
    }

    @Override
    public boolean isStarted() {
        return true;
    }
}
