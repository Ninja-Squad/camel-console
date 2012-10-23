package com.ninja_squad.console.notifier;

import com.ninja_squad.console.InstanceState;
import com.ninja_squad.console.Message;
import com.ninja_squad.console.Route;
import com.ninja_squad.console.RouteState;

public interface ConsoleRepository {
    /**
     * Save a new {@link com.ninja_squad.console.Message} when exchange is completed (failed or not)
     *
     * @param message
     */
    void save(Message message);

    /**
     * Save a new {@link com.ninja_squad.console.InstanceState} of the application
     *
     * @param state
     */
    void save(InstanceState state);

    /**
     * Save a new {@link Route}
     *
     * @param route
     */
    void save(Route route);

    /**
     * Find a {@link Route} by its id
     *
     * @param routeId
     * @return the {@link Route}
     */
    Route findRoute(String routeId);

    /**
     * Save a new {@link com.ninja_squad.console.RouteState}
     *
     * @param routeState
     */
    void save(RouteState routeState);

    /**
     * Find the last {@link RouteState} by the route's id
     *
     * @param routeId
     * @return
     */
    RouteState lastRouteState(String routeId);

    /**
     * Update the route with fresh statistics
     *
     * @param routeId
     * @param exchangesCompleted
     * @param exchangesFailed
     * @param exchangesTotal
     */
    void updateRoute(String routeId, long exchangesCompleted, long exchangesFailed, long exchangesTotal);
}
