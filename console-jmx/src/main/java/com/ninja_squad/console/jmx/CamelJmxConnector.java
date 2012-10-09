package com.ninja_squad.console.jmx;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import com.ninja_squad.console.Instance;
import com.ninja_squad.console.Route;
import com.ninja_squad.console.State;
import com.ninja_squad.console.jmx.exception.JmxException;
import com.ninja_squad.core.retry.Retryer;
import com.ninja_squad.core.retry.RetryerBuilder;
import com.ninja_squad.core.retry.WaitStrategies;
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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CamelJmxConnector {

    protected final static String CAMEL_PACKAGE = "org.apache.camel:";
    protected final static String CAMEL_ROUTE = CAMEL_PACKAGE + "type=routes,*";
    protected final static String CAMEL_TRACER = CAMEL_PACKAGE + "type=tracer,*";
    public static final String ROUTE_ID = "RouteId";
    public static final String ENDPOINT_URI = "EndpointUri";
    public static final String STATE = "State";

    @Getter
    @Setter
    private MBeanServerConnection serverConnection;

    @NonNull
    private Instance instance;

    @Setter
    private CamelJmxNotificationListener notificationListener = new CamelJmxNotificationListener();

    private Retryer retryer;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Jmx connector to a Camel instance
     *
     * @param instance should not be null
     */
    public CamelJmxConnector(Instance instance) {
        Preconditions.checkNotNull(instance, "The instance should not be null");
        this.instance = instance;
        retryer = RetryerBuilder.<State>newBuilder()
                .withWaitStrategy(WaitStrategies.fixedWait(500L, TimeUnit.MILLISECONDS))
                .retryIfResult(Predicates.<State>equalTo(State.Stopped))
                .build();
    }

    /**
     * Connect to the instance and give the current state.
     *
     * @return the current {@link State} of the instance
     */
    public State connect() {
        try {
            setServerConnection(connectToServer());
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
        if (State.Stopped.equals(state)) {
            retry();
        }
    }

    /**
     * Retry to connect if the instance is stopped.
     */
    @SuppressWarnings("unchecked")
    protected void retry() {
        Retryer.RetryerCallable callable = retryer.wrap(new Callable<State>() {
            int nb;

            @Override
            public State call() throws Exception {
                log.debug("Retry - " + nb++);
                return connect();
            }
        });
        executorService.submit(callable);
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
        State state = connect();
        return State.Stopped.equals(state);
    }

    /**
     * Try to establish a connection to the instance and return it.
     * A {@link JmxException} is raised if a connection problem occurs.
     *
     * @return a {@link MBeanServerConnection} to the instance
     * @throws JmxException
     */
    protected MBeanServerConnection connectToServer() throws JmxException {
        JMXServiceURL jmxServiceURL = getJmxServiceUrl();
        MBeanServerConnection server;
        try {
            JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxServiceURL);
            server = jmxConnector.getMBeanServerConnection();
        } catch (IOException e) {
            throw new JmxException("Cannot connect to " + instance.url(), e);
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
        Set<ObjectName> objectNames = getObjectNames(CAMEL_ROUTE);
        //convert them in routes
        Collection<Route> routes = Collections2.transform(objectNames, new Function<ObjectName, Route>() {
            @Override
            public Route apply(ObjectName objectName) {
                return extractRouteFromObjectName(objectName);
            }
        });
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
            return new Route(routeId).uri(in).state(State.valueOf(state));
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
        // TODO should poll at some interval to retry
        if (isServerStopped()) return;

        //get the only tracer
        Set<ObjectName> objectNames = getObjectNames(CAMEL_TRACER);
        if (objectNames.size() != 1) {
            throw new JmxException("There should be only one tracer - check your jmx config");
        }

        ObjectName tracer = objectNames.iterator().next();
        forceTraceNotification(tracer);

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
     * Return the notifications received
     *
     * @return a {@link List} of {@link CamelJmxNotification}
     */
    public List<CamelJmxNotification> getNotifications() {
        return notificationListener.getNotifications();
    }

}
