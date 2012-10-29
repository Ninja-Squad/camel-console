package com.ninja_squad.console.notifier;

import com.mongodb.Mongo;
import com.ninja_squad.console.InstanceState;
import com.ninja_squad.console.Message;
import com.ninja_squad.console.Route;
import com.ninja_squad.console.RouteState;
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

    public ConsoleRepositoryJongo() {
        Mongo mongo = null;
        try {
            mongo = new Mongo("127.0.0.1", 27017);
        } catch (UnknownHostException e) {
            log.error("No Mongo running");
        }
        Jongo jongo = new Jongo(mongo.getDB("console"));
        messages = jongo.getCollection("notifications");
        states = jongo.getCollection("states");
        routeStates = jongo.getCollection("routestates");
        routes = jongo.getCollection("routes");
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
    public void updateRoute(String routeId, long exchangesCompleted, long exchangesFailed, long exchangesTotal) {
        routes.update("{routeId:#}", routeId)
                .with("{$set:{exchangesCompleted: #, exchangesFailed: #, exchangesTotal: #}}",
                        exchangesCompleted, exchangesFailed, exchangesTotal);
    }
}
