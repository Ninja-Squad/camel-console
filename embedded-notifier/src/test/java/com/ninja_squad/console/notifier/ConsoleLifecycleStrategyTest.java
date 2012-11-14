package com.ninja_squad.console.notifier;

import com.ninja_squad.console.InstanceState;
import com.ninja_squad.console.Route;
import com.ninja_squad.console.RouteState;
import com.ninja_squad.console.State;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.SortedMap;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ConsoleLifecycleStrategyTest {

    private CamelContext context;
    private ConsoleRepository repository;

    @Before
    public void setUpContext() {
        context = new DefaultCamelContext();
        ConsoleLifecycleStrategy lifecycleStrategy = new ConsoleLifecycleStrategy();
        repository = mock(ConsoleRepositoryJongo.class);
        doNothing().when(repository).save(any(InstanceState.class));
        lifecycleStrategy.setRepository(repository);
        context.addLifecycleStrategy(lifecycleStrategy);
    }

    @After
    public void tearDown() throws Exception {
        context.stop();
    }

    @Test
    public void shouldLogStartTime() throws Exception {
        //when starting context
        DateTime startTime = DateTime.now();
        context.start();

        //then the startup should be stored
        ArgumentCaptor<InstanceState> argument = ArgumentCaptor.forClass(InstanceState.class);
        verify(repository).save(argument.capture());
        InstanceState instanceState = argument.getValue();
        assertThat(instanceState.getName()).isEqualTo(context.getName());
        assertThat(instanceState.getState()).isEqualTo(State.Started);
        DateTime timestamp = new DateTime(instanceState.getTimestamp());
        assertThat(timestamp.isBeforeNow()).isTrue();
        assertThat(timestamp.isAfter(startTime)).isTrue();

    }

    @Test
    public void shouldLogStopTime() throws Exception {
        //given a context with a consoleLifecycleStrategy
        context.start();

        //when stopping context
        DateTime startTime = DateTime.now();
        context.stop();

        //then the startup should be stored
        ArgumentCaptor<InstanceState> argument = ArgumentCaptor.forClass(InstanceState.class);
        verify(repository, times(2)).save(argument.capture());
        InstanceState instanceState = argument.getValue();
        assertThat(instanceState.getName()).isEqualTo(context.getName());
        assertThat(instanceState.getState()).isEqualTo(State.Stopped);
        DateTime timestamp = new DateTime(instanceState.getTimestamp());
        assertThat(timestamp.isBeforeNow()).isTrue();
        assertThat(timestamp.isBefore(startTime)).isFalse();
    }

    @Test
    public void shouldLogRouteAddedAndStopped() throws Exception {
        //given a context with a consoleLifecycleStrategy
        context.start();

        //when adding a new route2
        DateTime startTime = DateTime.now();
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct://route1").routeId("route1").to("mock:result");
                from("direct://route2").routeId("route2").to("mock:result2");
            }
        });

        //should have tried to find it
        doReturn(null).when(repository).findRoute("route1");
        doReturn(null).when(repository).findRoute("route2");
        verify(repository, times(1)).findRoute("route1");
        verify(repository, times(1)).findRoute("route2");

        //then the startup should be stored
        ArgumentCaptor<Route> routeArgumentCaptor = ArgumentCaptor.forClass(Route.class);
        verify(repository, times(2)).save(routeArgumentCaptor.capture());
        // with route1
        Route route = routeArgumentCaptor.getAllValues().get(0);
        assertThat(route.getRouteId()).isEqualTo("route1");
        assertThat(route.getUri()).isEqualTo("direct://route1");
        assertThat(route.getDefinition()).isNotNull();
        assertThat(route.getDefinition()).isNotEmpty();
        SortedMap<String, String> steps = route.getSteps();
        assertThat(steps).hasSize(1);
        assertThat(steps.get(steps.keySet().iterator().next())).isEqualTo("mock:result");
        // then route2
        Route route2 = routeArgumentCaptor.getAllValues().get(1);
        assertThat(route2.getRouteId()).isEqualTo("route2");
        assertThat(route2.getUri()).isEqualTo("direct://route2");
        assertThat(route2.getDefinition()).isNotNull();
        assertThat(route2.getDefinition()).isNotEmpty();
        steps = route2.getSteps();
        assertThat(steps).hasSize(1);
        assertThat(steps.get(steps.keySet().iterator().next())).isEqualTo("mock:result2");
        //and a log of the state and time
        ArgumentCaptor<RouteState> routeStateArgumentCaptor = ArgumentCaptor.forClass(RouteState.class);
        verify(repository, times(2)).save(routeStateArgumentCaptor.capture());
        // for route1
        RouteState routeState = routeStateArgumentCaptor.getAllValues().get(0);
        assertThat(routeState.getRouteId()).isEqualTo("route1");
        assertThat(routeState.getState()).isEqualTo(State.Started);
        DateTime timestamp = new DateTime(routeState.getTimestamp());
        assertThat(timestamp.isBeforeNow()).isTrue();
        assertThat(timestamp.isAfter(startTime)).isTrue();
        // and route2
        RouteState routeState2 = routeStateArgumentCaptor.getAllValues().get(0);
        assertThat(routeState2.getRouteId()).isEqualTo("route1");
        assertThat(routeState2.getState()).isEqualTo(State.Started);
        DateTime timestamp2 = new DateTime(routeState2.getTimestamp());
        assertThat(timestamp2.isBeforeNow()).isTrue();
        assertThat(timestamp2.isAfter(startTime)).isTrue();
    }
}
