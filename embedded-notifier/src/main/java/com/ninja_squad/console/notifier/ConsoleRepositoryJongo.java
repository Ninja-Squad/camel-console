package com.ninja_squad.console.notifier;

import com.mongodb.Mongo;
import com.ninja_squad.console.InstanceState;
import com.ninja_squad.console.Message;
import com.ninja_squad.console.Route;
import com.ninja_squad.console.RouteState;
import com.ninja_squad.console.model.ExchangeStatistic;
import org.jongo.Jongo;
import org.jongo.MongoCollection;

import java.net.UnknownHostException;
import java.util.Iterator;

public class ConsoleRepositoryJongo implements ConsoleRepository {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConsoleRepositoryJongo.class);

    private MongoCollection messages;
    private MongoCollection states;
    private MongoCollection routes;
    private MongoCollection routeStates;
    private MongoCollection exchangeStats;

    public ConsoleRepositoryJongo(String host, int port) {
        Mongo mongo = null;
        try {
            mongo = new Mongo(host, port);
        } catch (UnknownHostException e) {
            log.error("No Mongo running");
        }
        Jongo jongo = new Jongo(mongo.getDB("console"));
        messages = jongo.getCollection("notifications");
        states = jongo.getCollection("states");
        routeStates = jongo.getCollection("routestates");
        routes = jongo.getCollection("routes");
        exchangeStats = jongo.getCollection("exchanges");
    }

    @Override
    public void save(Message message) {
        messages.save(message);
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
    public void save(ExchangeStatistic exchangeStatistic) {
        exchangeStats.save(exchangeStatistic);
    }

}
