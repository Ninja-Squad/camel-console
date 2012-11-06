package com.ninja_squad.console.notifier;

import com.mongodb.Mongo;
import com.ninja_squad.console.*;
import lombok.extern.slf4j.Slf4j;
import org.jongo.Jongo;
import org.jongo.MongoCollection;

import java.net.UnknownHostException;
import java.util.Iterator;

@Slf4j
public class ConsoleRepositoryJongo implements ConsoleRepository {

    public static final String ROUTES = "routes";
    public static final String APP_STATES = "app_states";
    public static final String ROUTE_STATES = "route_states";
    public static final String ROUTE_STATISTICS = "route_statistics";
    public static final String EXCHANGE_STATISTICS = "exchange_statistics";

    private MongoCollection exchangeStatistics;
    private MongoCollection states;
    private MongoCollection routes;
    private MongoCollection routeStates;
    private MongoCollection routeStatistics;

    public ConsoleRepositoryJongo(String host, int port) {
        Mongo mongo = null;
        try {
            mongo = new Mongo(host, port);
        } catch (UnknownHostException e) {
            log.error("No Mongo running");
        }
        Jongo jongo = new Jongo(mongo.getDB("console"));
        routes = jongo.getCollection(ROUTES);
        states = jongo.getCollection(APP_STATES);
        routeStates = jongo.getCollection(ROUTE_STATES);
        routeStatistics = jongo.getCollection(ROUTE_STATISTICS);
        exchangeStatistics = jongo.getCollection(EXCHANGE_STATISTICS);
    }

    @Override
    public void save(ExchangeStatistic exchangeStatistic) {
        exchangeStatistics.save(exchangeStatistic);
    }

    @Override
    public void save(InstanceState state) {
        states.save(state);
    }

    @Override
    public void save(Route route) {
        routes.save(route);
    }

    @Override
    public Route findRoute(String routeId) {
        return routes.findOne("{routeId:#}", routeId).as(Route.class);
    }

    @Override
    public void save(RouteState routeState) {
        routeStates.save(routeState);
    }

    @Override
    public RouteState lastRouteState(String routeId) {
        Iterator<RouteState> iterator = routeStates.find("{routeId:#}", routeId)
                .sort("{timestamp:-1}")
                .limit(1)
                .as(RouteState.class)
                .iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    @Override
    public void save(RouteStatistic routeStatistic) {
        routeStatistics.save(routeStatistic);
    }

}
