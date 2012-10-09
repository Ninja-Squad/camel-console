package com.ninja_squad.console.jmx;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.ninja_squad.console.Instance;
import com.ninja_squad.console.Route;
import com.ninja_squad.console.State;
import com.ninja_squad.console.jmx.exception.JmxException;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.api.management.mbean.ManagedTracerMBean;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;

@Slf4j
public class CamelJmxConnector {

    protected final static String CAMEL_PACKAGE = "org.apache.camel:";
    protected final static String CAMEL_ROUTE = CAMEL_PACKAGE + "type=routes,name=";
    protected final static String CAMEL_ROUTE_ALL = CAMEL_PACKAGE + "type=routes,*";
    protected final static String CAMEL_TRACER = CAMEL_PACKAGE + "type=tracer,*";
    public static final String ROUTE_ID = "RouteId";
    public static final String ENDPOINT_URI = "EndpointUri";
    public static final String STATE = "State";
    public static final String EXCHANGES_COMPLETED = "ExchangesCompleted";
    public static final String EXCHANGES_FAILED = "ExchangesFailed";
    public static final String EXCHANGES_TOTAL = "ExchangesTotal";

    @NonNull
    private Instance instance;

    @Getter
    private MBeanServerConnection serverConnection;

    private CamelJmxConnectionRetryer retryer = new CamelJmxConnectionRetryer(this);

    @Getter
    private EventBus notificationBus = new EventBus();

    @Setter
    private CamelJmxNotificationListener notificationListener = new CamelJmxNotificationListener(notificationBus);

    private List<CamelJmxNotification> notifications = new ArrayList<CamelJmxNotification>();

    private CamelJMXConnectionListener connectionListener = new CamelJMXConnectionListener();

    /**
     * Jmx connector to a Camel instance
     *
     * @param instance should not be null
     */
    public CamelJmxConnector(Instance instance) {
        Preconditions.checkNotNull(instance, "The instance should not be null");
        this.instance = instance;
        // start watching notifications
        notificationBus.register(new NotificationHandler());
        retryer.start();
    }

    /**
     * Connect to the instance and give the current state.
     *
     * @return the current {@link State} of the instance
     */
    public State connect() {
        try {
            serverConnection = connectToServer();
            updateState(State.Started);
        } catch (JmxException e) {
            updateState(State.Stopped);
        }
        log.debug("Connect - " + instance.getState());
        return instance.getState();
    }

    /**
     * Update the instance's state and eventually begin a retry strategy.
     *
     * @param state the actual instance's state
     */
    protected void updateState(State state) {
        if (instance.getState().equals(state)) {
            //same state, nothing to do
            return;
        }
        log.debug("New state - " + state);
        instance.setState(state);
    }

    /**
     * Get the routes of the instance, with their current state.
     * An empty set is returned if the instance cannot be reach or if an exception occurred.
     *
     * @return the instance's set of {@link Route}
     */
    public Set<Route> getRoutes() {
        //if instance is stopped, return an empty set
        if (isServerStopped()) return new HashSet<Route>();
        //else get the routes
        try {
            return connectToRoutes();
        } catch (JmxException e) {
            return new HashSet<Route>();
        }
    }

    /**
     * Check if the instance is stopped
     *
     * @return true if the server is stopped
     */
    protected boolean isServerStopped() {
        return State.Stopped.equals(instance.getState());
    }

    /**
     * Try to establish a connection to the instance and return it.
     * A {@link CamelJMXConnectionListener} is added to listen to the connection's state.
     * A {@link JmxException} is raised if a connection problem occurs.
     *
     * @return a {@link MBeanServerConnection} to the instance
     * @throws JmxException
     */
    protected MBeanServerConnection connectToServer() throws JmxException {
        JMXServiceURL jmxServiceURL = getJmxServiceUrl();
        MBeanServerConnection server;
        try {
            Map<String, Object> properties = Maps.newHashMap();
            properties.put("jmx.remote.x.client.connection.check.period", 500L);
            JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxServiceURL, properties);
            // start watching connection state
            jmxConnector.addConnectionNotificationListener(connectionListener, null, retryer);
            server = jmxConnector.getMBeanServerConnection();
        } catch (IOException e) {
            throw new JmxException("Cannot connect to " + instance.url());
        }
        return server;
    }

    /**
     * Return a {@link JMXServiceURL} from a {@link String}
     *
     * @return the instance's {@link JMXServiceURL}
     * @throws JmxException
     */
    protected JMXServiceURL getJmxServiceUrl() throws JmxException {
        try {
            return new JMXServiceURL(instance.url());
        } catch (MalformedURLException e) {
            throw new JmxException("Malformed url : " + instance.url(), e);
        }
    }

    /**
     * Try to connect to the server to get all the routes of the instance
     *
     * @return the instance's set of {@link Route}
     * @throws JmxException
     */
    protected Set<Route> connectToRoutes() throws JmxException {
        //get objectNames
        Set<ObjectName> objectNames = getObjectNames(CAMEL_ROUTE_ALL);
        //convert them in routes
        Collection<Route> routes = Collections2.transform(objectNames, new Function<ObjectName, Route>() {
            @Override
            public Route apply(ObjectName objectName) {
                return extractRouteFromObjectName(objectName);
            }
        });

        instance.getRoutes().clear();
        for (Route route : routes) {
            instance.getRoutes().put(route.getUri(), route);
        }

        //return the routes as a set
        return Sets.newHashSet(routes);
    }

    /**
     * Return a {@link Route} from an {@link ObjectName}
     *
     * @param objectName to convert to a {@link Route}
     * @return the {@link Route} associated to the {@link ObjectName}
     * @throws JmxException
     */
    protected Route extractRouteFromObjectName(ObjectName objectName) throws JmxException {
        String keyProperty = objectName.getCanonicalKeyPropertyListString();
        ObjectName objectInfoName = getInfoName(keyProperty);
        try {
            String routeId = (String) getServerConnection().getAttribute(objectInfoName, ROUTE_ID);
            String in = (String) getServerConnection().getAttribute(objectInfoName, ENDPOINT_URI);
            String state = (String) getServerConnection().getAttribute(objectInfoName, STATE);
            Route route = new Route(routeId).uri(in).state(State.valueOf(state));
            route.setCanonicalName(objectName.getCanonicalName());

            long exchangesCompleted = (Long) getServerConnection().getAttribute(objectInfoName, EXCHANGES_COMPLETED);
            route.setExchangesCompleted(exchangesCompleted);

            long exchangesFailed = (Long) getServerConnection().getAttribute(objectInfoName, EXCHANGES_FAILED);
            route.setExchangesFailed(exchangesFailed);

            long exchangesTotal = (Long) getServerConnection().getAttribute(objectInfoName, EXCHANGES_TOTAL);
            route.setExchangesTotal(exchangesTotal);

            return route;
        } catch (Exception e) {
            throw new JmxException("Couldn't find objects with name : " + CAMEL_PACKAGE + keyProperty, e);
        }
    }

    /**
     * Get the objects exposed on JMX by the instance
     *
     * @return a list og {@link ObjectName}
     * @throws JmxException
     */
    protected Set<ObjectName> getObjectNames(String key) throws JmxException {
        //build the query
        ObjectName objectName;
        try {
            objectName = ObjectName.getInstance(key);
        } catch (MalformedObjectNameException e) {
            throw new JmxException("Incorrect object name : " + key, e);
        }
        //ask the server
        Set<ObjectName> objectNames;
        try {
            objectNames = getServerConnection().queryNames(objectName, null);
        } catch (IOException e) {
            throw new JmxException("Couldn't find objects with name : " + key, e);
        }
        return objectNames;
    }

    /**
     * Return an {@link ObjectName} from a {@link String}
     *
     * @param keyProperty to get
     * @return the {@link ObjectName} associated to the property
     * @throws JmxException
     */
    private ObjectName getInfoName(String keyProperty) throws JmxException {
        try {
            return ObjectName.getInstance(CAMEL_PACKAGE + keyProperty);
        } catch (MalformedObjectNameException e) {
            throw new JmxException("Incorrect object name : " + CAMEL_PACKAGE + keyProperty, e);
        }
    }

    /**
     * Start listening on a specific route, specified by its id.
     * All messages going through this route will be received and stored.
     */
    public void listen() {
        ObjectName tracer = getTracer();

        //adding the listener
        try {
            getServerConnection().addNotificationListener(tracer, notificationListener, null, null);
            log.debug("Listener added");
        } catch (InstanceNotFoundException e) {
            updateState(State.Stopped);
            throw new JmxException("Instance cannot be found", e);
        } catch (IOException e) {
            updateState(State.Stopped);
            throw new JmxException("Couldn't connect to the instance", e);
        }

    }

    /**
     * Give the instance's tracer
     *
     * @return the tracer
     */
    private ObjectName getTracer() {
        //get the only tracer
        Set<ObjectName> objectNames = getObjectNames(CAMEL_TRACER);
        if (objectNames.size() != 1) {
            throw new JmxException("There should be only one tracer - check your jmx config");
        }

        ObjectName tracer = objectNames.iterator().next();
        forceTraceNotification(tracer);
        return tracer;
    }

    /**
     * Ensure the instance has enable the jmx notifications on the tracer
     *
     * @param tracer to listen
     */
    protected void forceTraceNotification(ObjectName tracer) {
        //calling setJmxTraceNotifications to be sure it is enable
        ManagedTracerMBean tracerCamel = JMX.newMBeanProxy(serverConnection, tracer, ManagedTracerMBean.class);
        tracerCamel.setJmxTraceNotifications(true);
    }

    /**
     * Subscriber to the {@link EventBus} receiving the notifications.
     * Each notification will be stored and will trigger an update of the route's stats.
     */
    private class NotificationHandler {
        @Subscribe
        public void handleNotification(CamelJmxNotification notification) {
            getRoutes();
            storeNotification(notification);
        }
    }

    /**
     * Store the notification
     *
     * @param notification to store
     */
    public void storeNotification(CamelJmxNotification notification) {
        notifications.add(notification);
    }

    private ObjectName getRoute(String routeId) {
        //get the only route
        Set<ObjectName> objectNames = getObjectNames(routeId);
        if (objectNames.size() != 1) {
            throw new JmxException("There should be only one route with this id : " + routeId + " and not " + objectNames.size());
        }

        return objectNames.iterator().next();
    }

    /**
     * Return the notifications received
     *
     * @return a {@link List} of {@link CamelJmxNotification}
     */
    public List<CamelJmxNotification> getNotifications() {
        return notifications;
    }

}
