package com.ninja_squad.console.notifier;

import com.google.common.collect.Maps;
import com.ninja_squad.console.State;
import org.apache.camel.*;
import org.apache.camel.impl.EventDrivenConsumerRoute;
import org.apache.camel.management.InstrumentationProcessor;
import org.apache.camel.spi.LifecycleStrategy;
import org.apache.camel.spi.RouteContext;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

public class ConsoleLifecycleStrategy implements LifecycleStrategy {

    private Logger log = LoggerFactory.getLogger(getClass());

    private ConsoleRepository repository = new ConsoleRepositoryJongo();
    private Map<String, ConsolePerformanceCounter> counters = Maps.newHashMap();

    /**
     * Store a notification of the application state and time
     *
     * @param context
     * @throws VetoCamelContextStartException
     */
    @Override
    public void onContextStart(CamelContext context) throws VetoCamelContextStartException {

        log.debug("Started " + context.getName());
        InstanceState instanceState = new InstanceState();
        instanceState.setName(context.getName());
        instanceState.setState(State.Started);
        instanceState.setTimestamp(DateTime.now().toString());
        repository.save(instanceState);
    }

    /**
     * Store a notification of the application state and hour
     *
     * @param context
     */
    @Override
    public void onContextStop(CamelContext context) {
        log.debug("Stopped " + context.getName());
        InstanceState instanceState = new InstanceState();
        instanceState.setName(context.getName());
        instanceState.setState(State.Stopped);
        instanceState.setTimestamp(DateTime.now().toString());
        repository.save(instanceState);
    }

    @Override
    public void onComponentAdd(String name, Component component) {
        //nothing to do
    }

    @Override
    public void onComponentRemove(String name, Component component) {
        //nothing to do
    }

    @Override
    public void onEndpointAdd(Endpoint endpoint) {
        //nothing to do
    }

    @Override
    public void onEndpointRemove(Endpoint endpoint) {
        //nothing to do
    }

    @Override
    public void onServiceAdd(CamelContext context, Service service, Route route) {
        //nothing to do
    }

    @Override
    public void onServiceRemove(CamelContext context, Service service, Route route) {
        //nothing to do
    }

    /**
     * Add the route in the database if it's not already, or update its state.
     *
     * @param routes
     */
    @Override
    public void onRoutesAdd(Collection<Route> routes) {
        for (Iterator<Route> iterator = routes.iterator(); iterator.hasNext(); ) {
            Route routeCamel = iterator.next();

            //adding a performance counter on the route
            if (routeCamel instanceof EventDrivenConsumerRoute) {
                EventDrivenConsumerRoute edcr = (EventDrivenConsumerRoute) routeCamel;
                Processor processor = edcr.getProcessor();
                if (processor instanceof InstrumentationProcessor) {
                    InstrumentationProcessor ip = (InstrumentationProcessor) processor;
                    ConsolePerformanceCounter counter = new ConsolePerformanceCounter(routeCamel.getId());
                    ip.setCounter(counter);
                    counters.put(routeCamel.getId(), counter);
                }
            }


            //saving route in database
            log.debug("Route added " + routeCamel.getId());
            com.ninja_squad.console.Route route = repository.findRoute(routeCamel.getId());
            if (route == null) {
                route = new com.ninja_squad.console.Route(routeCamel.getId())
                        .state(State.Started)
                        .uri(routeCamel.getEndpoint().getEndpointUri());
                repository.save(route);
            }

            //saving state in database
            RouteState routeState = repository.lastRouteState(routeCamel.getId());
            if (routeState == null || routeState.getState().equals(State.Stopped)) {
                routeState = new RouteState();
                routeState.setRouteId(routeCamel.getId());
                routeState.setState(State.Started);
                routeState.setTimestamp(DateTime.now().toString());
                repository.save(routeState);
            }
        }
    }

    @Override
    public void onRoutesRemove(Collection<Route> routes) {
        for (Iterator<Route> iterator = routes.iterator(); iterator.hasNext(); ) {
            Route routeCamel = iterator.next();
            log.debug("Route stopped : " + routeCamel.getId());
            RouteState routeState = repository.lastRouteState(routeCamel.getId());
            if (routeState == null || routeState.getState().equals(State.Started)) {
                routeState = new RouteState();
                routeState.setRouteId(routeCamel.getId());
                routeState.setState(State.Stopped);
                routeState.setTimestamp(DateTime.now().toString());
                repository.save(routeState);
            }
        }
    }

    @Override
    public void onRouteContextCreate(RouteContext routeContext) {
        //nothing to do
    }

    @Override
    public void onErrorHandlerAdd(RouteContext routeContext, Processor errorHandler, ErrorHandlerFactory errorHandlerBuilder) {
        //nothing to do
    }

    @Override
    public void onErrorHandlerRemove(RouteContext routeContext, Processor errorHandler, ErrorHandlerFactory errorHandlerBuilder) {
        //nothing to do
    }

    @Override
    public void onThreadPoolAdd(CamelContext camelContext, ThreadPoolExecutor threadPool, String id, String sourceId, String routeId, String threadPoolProfileId) {
        //nothing to do
    }

    @Override
    public void onThreadPoolRemove(CamelContext camelContext, ThreadPoolExecutor threadPool) {
        //nothing to do
    }

    public void setRepository(ConsoleRepository repository) {
        this.repository = repository;
    }

    public ConsolePerformanceCounter getCounter(String routeId) {
        return counters.get(routeId);
    }
}
