package com.ninja_squad.console.notifier;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.ninja_squad.console.InstanceState;
import com.ninja_squad.console.RouteState;
import com.ninja_squad.console.State;
import org.apache.camel.*;
import org.apache.camel.impl.EventDrivenConsumerRoute;
import org.apache.camel.management.InstrumentationProcessor;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spi.LifecycleStrategy;
import org.apache.camel.spi.RouteContext;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;

public class ConsoleLifecycleStrategy implements LifecycleStrategy {

    private Logger log = LoggerFactory.getLogger(getClass());

    private ConsoleRepository repository;

    public ConsoleLifecycleStrategy() {
        String property = null;
        String host = null;
        try {
            java.util.Properties properties = new java.util.Properties();
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

    /**
     * Store a notification of the application state and time
     *
     * @param context of the Camel app
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
     * @param context of the Camel app
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
     * @param routes being added to the app
     */
    @Override
    public void onRoutesAdd(Collection<Route> routes) {
        for (Route routeCamel : routes) {
            //adding a performance counter on the route
            if (routeCamel instanceof EventDrivenConsumerRoute) {
                EventDrivenConsumerRoute edcr = (EventDrivenConsumerRoute) routeCamel;
                Processor processor = edcr.getProcessor();
                if (processor instanceof InstrumentationProcessor) {
                    InstrumentationProcessor ip = (InstrumentationProcessor) processor;
                    ConsolePerformanceCounter counter = new ConsolePerformanceCounter(routeCamel.getId(), repository);
                    ip.setCounter(counter);
                    log.debug("Adding a counter" + counter.toString() + " to " + routeCamel.getId());
                }
            }

            //saving route in database
            log.debug("Route added " + routeCamel.getId());
            com.ninja_squad.console.Route route = repository.findRoute(routeCamel.getId());
            if (route == null) {
                route = new com.ninja_squad.console.Route(routeCamel.getId())
                        .state(State.Started)
                        .uri(routeCamel.getEndpoint().getEndpointUri());
                ObjectMapper mapper = new ObjectMapper();
                AnnotationIntrospector introspector = new JaxbAnnotationIntrospector(TypeFactory.defaultInstance());
                // make serializer use JAXB annotations (only)
                mapper.setAnnotationIntrospector(introspector);
                String definition = null;
                RouteDefinition routeDefinition = routeCamel.getRouteContext().getRoute();
                try {
                    definition = mapper.writeValueAsString(routeDefinition);
                } catch (IOException e) {
                    log.error("Error while marshalling route definition", e);
                }
                route.setDefinition(definition);
                for (ProcessorDefinition<?> stepDefinition : routeDefinition.getOutputs()) {
                    if (stepDefinition.getId() == null) {
                        stepDefinition.setId(stepDefinition.getClass().getSimpleName());
                    }
                    route.getSteps().put(stepDefinition.getId(), stepDefinition.getLabel());
                }
                repository.save(route);
            }

            //saving state in database
            RouteState routeState = repository.lastRouteState(routeCamel.getId());
            if (routeState == null || routeState.getState().equals(State.Stopped)) {
                routeState = new RouteState();
                routeState.setRouteId(routeCamel.getId());
                routeState.setState(State.Started);
                routeState.setTimestamp(DateTime.now().getMillis());
                repository.save(routeState);
            }
        }
    }

    @Override
    public void onRoutesRemove(Collection<Route> routes) {
        for (Route routeCamel : routes) {
            log.debug("Route stopped : " + routeCamel.getId());
            // saving state in database
            RouteState routeState = repository.lastRouteState(routeCamel.getId());
            if (routeState == null || routeState.getState().equals(State.Started)) {
                routeState = new RouteState();
                routeState.setRouteId(routeCamel.getId());
                routeState.setState(State.Stopped);
                routeState.setTimestamp(DateTime.now().getMillis());
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

}
