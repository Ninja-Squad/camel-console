package com.ninja_squad.console.jmx;

import com.google.common.collect.Sets;
import com.ninja_squad.console.Instance;
import com.ninja_squad.console.Route;
import com.ninja_squad.console.State;
import com.ninja_squad.console.jmx.exception.JmxException;
import org.junit.Test;

import javax.management.*;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

public class CamelJmxConnectorTest {

    // constructor

    @Test
    public void constructorShouldFailWithNullInstance() throws Exception {
        try {
            new CamelJmxConnector(null);
            failBecauseExceptionWasNotThrown(NullPointerException.class);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(NullPointerException.class).hasMessage("The instance should not be null");
        }
    }


    //connect()

    @Test
    public void connectShouldReturnStoppedIfServerThrowsException() throws Exception {
        //mocking connectToServer to throw a JmxException
        Instance instance = new Instance();
        CamelJmxConnector connector = spy(new CamelJmxConnector(instance));
        doThrow(JmxException.class).when(connector).connectToServer();

        //then State should be stopped when call to connect
        assertThat(connector.connect()).isEqualTo(State.Stopped);
        assertThat(instance.getState()).isEqualTo(State.Stopped);
    }

    @Test
    public void connectShouldReturnStartedIfServerIsAlright() throws Exception {
        //mocking connectToServer to return a connection
        Instance instance = new Instance();
        CamelJmxConnector connector = spy(new CamelJmxConnector(instance));
        doReturn(mock(MBeanServerConnection.class)).when(connector).connectToServer();
        doNothing().when(connector).listen();

        //then State should be Started when call to connect
        assertThat(connector.connect()).isEqualTo(State.Started);
        assertThat(instance.getState()).isEqualTo(State.Started);
    }

    // updateState()

    @Test
    public void updateStateShouldReturnIfStateDidNotChange() throws Exception {
        Instance instance = spy(new Instance());
        instance.setState(State.Stopped);
        CamelJmxConnector connector = new CamelJmxConnector(instance);
        connector.updateState(State.Stopped);
        verify(instance, times(1)).setState(any(State.class));
        assertThat(instance.getState()).isEqualTo(State.Stopped);
    }

    @Test
    public void updateStateShouldChangeStateIfStateChanged() throws Exception {
        Instance instance = spy(new Instance());
        instance.setState(State.Stopped);
        CamelJmxConnector connector = new CamelJmxConnector(instance);
        connector.updateState(State.Started);
        verify(instance, times(2)).setState(any(State.class));
        assertThat(instance.getState()).isEqualTo(State.Started);
    }


    //connectToServer()

    @Test
    public void connectToServerShouldThrowAnExceptionIfNoInstanceIsStarted() throws Exception {
        CamelJmxConnector connector = new CamelJmxConnector(new Instance());
        try {
            connector.connectToServer();
            failBecauseExceptionWasNotThrown(JmxException.class);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(JmxException.class).hasMessage("Cannot connect to " + new Instance().url());
        }
    }


    //getJmxServiceUrl()

    @Test
    public void getJmxServiceUrlShouldThrowAnExceptionIfUrlIsIncorrect() throws Exception {
        Instance instance = mock(Instance.class);
        when(instance.url()).thenReturn("-1");
        CamelJmxConnector connector = new CamelJmxConnector(instance);
        try {
            connector.getJmxServiceUrl();
            failBecauseExceptionWasNotThrown(JmxException.class);
        } catch (JmxException e) {
            assertThat(e).hasMessage("Malformed url : -1");
        }
    }

    @Test
    public void getJmxServiceUrlShouldReturnAnUrl() throws Exception {
        CamelJmxConnector connector = new CamelJmxConnector(new Instance());
        assertThat(connector.getJmxServiceUrl()).isEqualTo(new JMXServiceURL(new Instance().url()));
    }


    //getRoutes()

    @Test
    public void getRoutesShouldReturnEmptySetIfServerIsStopped() throws Exception {
        //mocking isServerStopped to return true
        CamelJmxConnector connector = spy(new CamelJmxConnector(new Instance()));
        doReturn(true).when(connector).isServerStopped();

        //then
        assertThat(connector.getRoutes()).isInstanceOf(Set.class).isEmpty();
    }

    @Test
    public void getRoutesShouldReturnEmptySetIfConnectToRoutesThrowsException() throws Exception {
        //mocking isServerStopped to return false and connectToRoutes an exception
        CamelJmxConnector connector = spy(new CamelJmxConnector(new Instance()));
        doReturn(false).when(connector).isServerStopped();
        doThrow(JmxException.class).when(connector).connectToRoutes();

        //then
        assertThat(connector.getRoutes()).isInstanceOf(Set.class).isEmpty();
    }

    @Test
    public void getRoutesShouldReturnASetWithRoutesIfConnectSuccess() throws Exception {
        //mocking connectToRoutes to return one route
        Instance instance = new Instance();
        CamelJmxConnector connector = spy(new CamelJmxConnector(instance));
        Route route = new Route("route1").state(State.Started).uri("direct://endpoint");
        Set<Route> routes = Sets.newHashSet(route);
        doReturn(routes).when(connector).connectToRoutes();
        //server is running
        doReturn(false).when(connector).isServerStopped();

        //then
        assertThat(connector.getRoutes()).isInstanceOf(Set.class).containsExactly(route);
    }


    //connectToRoutes()

    @Test
    public void connectToRoutesShouldReturnASetWithRoutesFromObjectNames() throws Exception {
        //mocking getObjectNames to return a list
        Instance instance = new Instance();
        CamelJmxConnector connector = spy(new CamelJmxConnector(instance));
        ObjectName objectName = new ObjectName(CamelJmxConnector.CAMEL_ROUTE);
        Set<ObjectName> objectNames = Sets.newHashSet(objectName);
        doReturn(objectNames).when(connector).getObjectNames(anyString());
        // and extractRouteFromObjectName to return a route
        Route route1 = new Route("route1").uri("direct://endpoint");
        doReturn(route1).when(connector).extractRouteFromObjectName(objectName);

        //then
        assertThat(connector.connectToRoutes()).isInstanceOf(Set.class).containsExactly(route1);
        assertThat(instance.getRoutes().keySet()).isInstanceOf(Set.class).containsExactly(route1.getUri());
    }

    @Test
    public void connectToRoutesShouldThrowAnExceptionIfItCantConnect() throws Exception {
        //mocking getObjectNames to return a list
        CamelJmxConnector connector = spy(new CamelJmxConnector(new Instance()));
        doThrow(JmxException.class).when(connector).getObjectNames(anyString());

        //then should throw an exception
        try {
            connector.connectToRoutes();
            failBecauseExceptionWasNotThrown(JmxException.class);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(JmxException.class);
        }
    }


    //extractRouteFromObjectName()

    @Test
    public void extractRouteFromObjectNameShouldReturnARouteFromAnObject() throws Exception {
        //mocking serverConnection
        MBeanServerConnection serverConnection = mock(MBeanServerConnection.class);
        //mocking Jmx getAttribute
        when(serverConnection.getAttribute(any(ObjectName.class), eq(CamelJmxConnector.ROUTE_ID))).thenReturn("route1");
        when(serverConnection.getAttribute(any(ObjectName.class), eq(CamelJmxConnector.ENDPOINT_URI))).thenReturn("uri");
        when(serverConnection.getAttribute(any(ObjectName.class), eq(CamelJmxConnector.STATE))).thenReturn("Started");
        when(serverConnection.getAttribute(any(ObjectName.class), eq(CamelJmxConnector.EXCHANGES_COMPLETED))).thenReturn(0L);
        when(serverConnection.getAttribute(any(ObjectName.class), eq(CamelJmxConnector.EXCHANGES_FAILED))).thenReturn(0L);
        when(serverConnection.getAttribute(any(ObjectName.class), eq(CamelJmxConnector.EXCHANGES_TOTAL))).thenReturn(0L);

        CamelJmxConnector connector = spy(new CamelJmxConnector(new Instance()));
        doReturn(serverConnection).when(connector).getServerConnection();

        //then should return the route route1
        ObjectName objectName = new ObjectName(CamelJmxConnector.CAMEL_ROUTE);
        Route route1 = new Route("route1").state(State.Started).uri("uri");
        route1.setCanonicalName("org.apache.camel:name=,type=routes");
        assertThat(connector.extractRouteFromObjectName(objectName)).isEqualTo(route1);
    }


    @Test
    @SuppressWarnings("unchecked")
    public void extractRouteFromObjectNameShouldThrowAnExceptionIfNoConnection() throws Exception {
        //mocking serverConnection
        MBeanServerConnection serverConnection = mock(MBeanServerConnection.class);
        //mocking Jmx getAttribute to throw exception
        when(serverConnection.getAttribute(any(ObjectName.class), any(String.class))).thenThrow(AttributeNotFoundException.class);

        CamelJmxConnector connector = spy(new CamelJmxConnector(new Instance()));
        doReturn(serverConnection).when(connector).getServerConnection();

        //then should throw an exception
        ObjectName objectName = new ObjectName(CamelJmxConnector.CAMEL_ROUTE_ALL);
        try {
            connector.extractRouteFromObjectName(objectName);
            failBecauseExceptionWasNotThrown(JmxException.class);
        } catch (JmxException e) {
            assertThat(e).hasMessage("Couldn't find objects with name : org.apache.camel:type=routes");
        }
    }


    //getObjectNames()

    @Test
    public void getObjectNamesShouldReturnAList() throws Exception {
        //mocking serverConnection
        MBeanServerConnection serverConnection = mock(MBeanServerConnection.class);
        //mocking queryNames
        ObjectName objectNameRoute1 = new ObjectName(CamelJmxConnector.CAMEL_ROUTE);
        Set<ObjectName> objectNames = Sets.newHashSet(objectNameRoute1);
        when(serverConnection.queryNames(any(ObjectName.class), any(QueryExp.class))).thenReturn(objectNames);

        CamelJmxConnector connector = spy(new CamelJmxConnector(new Instance()));
        doReturn(serverConnection).when(connector).getServerConnection();

        //then should return these 2 objectNames
        assertThat(connector.getObjectNames(CamelJmxConnector.CAMEL_ROUTE)).containsExactly(objectNameRoute1);
    }

    @Test
    public void getObjectNamesShouldThrowAnExceptionIfIncorrectName() throws Exception {
        CamelJmxConnector connector = new CamelJmxConnector(new Instance());

        //then should throw JmxException
        try {
            connector.getObjectNames("-1");
            failBecauseExceptionWasNotThrown(JmxException.class);
        } catch (JmxException e) {
            assertThat(e).hasMessage("Incorrect object name : -1");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getObjectNamesShouldThrowAnExceptionIfCannotConnect() throws Exception {
        //mocking serverConnection
        MBeanServerConnection serverConnection = mock(MBeanServerConnection.class);
        //mocking queryNames to throw an IOException
        when(serverConnection.queryNames(any(ObjectName.class), any(QueryExp.class))).thenThrow(IOException.class);

        CamelJmxConnector connector = spy(new CamelJmxConnector(new Instance()));
        doReturn(serverConnection).when(connector).getServerConnection();

        //then should throw JmxException
        try {
            connector.getObjectNames(CamelJmxConnector.CAMEL_ROUTE);
            failBecauseExceptionWasNotThrown(JmxException.class);
        } catch (JmxException e) {
            assertThat(e).hasMessage("Couldn't find objects with name : " + CamelJmxConnector.CAMEL_ROUTE);
        }
    }


    //listen()

    @Test
    public void listenShouldThrowAnExceptionIfMultipleTracers() throws Exception {
        //mocking queryNames to return two tracers
        HashSet<ObjectName> objectNames = Sets.newHashSet(
                ObjectName.getInstance(CamelJmxConnector.CAMEL_ROUTE),
                ObjectName.getInstance(CamelJmxConnector.CAMEL_TRACER)
        );
        CamelJmxConnector connector = spy(new CamelJmxConnector(new Instance()));
        doReturn(objectNames).when(connector).getObjectNames(anyString());
        //mocking a valid connection
        doReturn(false).when(connector).isServerStopped();

        //then throw JmxException
        try {
            connector.listen();
            failBecauseExceptionWasNotThrown(JmxException.class);
        } catch (JmxException e) {
            assertThat(e).hasMessage("There should be only one tracer - check your jmx config");
        }
    }

    @Test
    public void listenShouldThrowAnExceptionIfInstanceNotFound() throws Exception {
        Instance instance = new Instance();
        CamelJmxConnector connector = spy(new CamelJmxConnector(instance));
        //mocking a valid connection
        doReturn(false).when(connector).isServerStopped();
        //mocking a valid objectName set
        HashSet<ObjectName> objectNames = Sets.newHashSet(ObjectName.getInstance(CamelJmxConnector.CAMEL_TRACER));
        doReturn(objectNames).when(connector).getObjectNames(anyString());
        //mocking forceTraceNotification
        doNothing().when(connector).forceTraceNotification(any(ObjectName.class));
        //mocking serverConnection
        MBeanServerConnection serverConnection = mock(MBeanServerConnection.class);
        doReturn(serverConnection).when(connector).getServerConnection();
        //mocking addNotificationListener to throw exception
        doThrow(InstanceNotFoundException.class).when(serverConnection).addNotificationListener(any(ObjectName.class), any(NotificationListener.class), any(NotificationFilter.class), anyObject());

        //then throw JmxException
        try {
            connector.listen();
            failBecauseExceptionWasNotThrown(JmxException.class);
        } catch (JmxException e) {
            assertThat(e).hasMessage("Instance cannot be found");
            assertThat(instance.getState()).isEqualTo(State.Stopped);
        }
    }

    @Test
    public void listenShouldThrowAnExceptionIfIOException() throws Exception {
        Instance instance = new Instance();
        CamelJmxConnector connector = spy(new CamelJmxConnector(instance));
        //mocking a valid connection
        doReturn(false).when(connector).isServerStopped();
        //mocking a valid objectName set
        HashSet<ObjectName> objectNames = Sets.newHashSet(ObjectName.getInstance(CamelJmxConnector.CAMEL_TRACER));
        doReturn(objectNames).when(connector).getObjectNames(anyString());
        //mocking forceTraceNotification
        doNothing().when(connector).forceTraceNotification(any(ObjectName.class));
        //mocking serverConnection
        MBeanServerConnection serverConnection = mock(MBeanServerConnection.class);
        doReturn(serverConnection).when(connector).getServerConnection();
        //mocking addNotificationListener to throw exception
        doThrow(IOException.class).when(serverConnection).addNotificationListener(any(ObjectName.class), any(NotificationListener.class), any(NotificationFilter.class), anyObject());

        //then throw JmxException
        try {
            connector.listen();
            failBecauseExceptionWasNotThrown(JmxException.class);
        } catch (JmxException e) {
            assertThat(e).hasMessage("Couldn't connect to the instance");
            assertThat(instance.getState()).isEqualTo(State.Stopped);
        }
    }

}
