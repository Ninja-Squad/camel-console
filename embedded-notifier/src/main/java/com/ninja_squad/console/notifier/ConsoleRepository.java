package com.ninja_squad.console.notifier;

import com.ninja_squad.console.*;

public interface ConsoleRepository {
    /**
     * Save a new {@link com.ninja_squad.console.ExchangeStatistic} when exchange is completed (failed or not)
     *
     * @param exchangeStatistic to save
     */
    void save(ExchangeStatistic exchangeStatistic);

    /**
     * Save a new {@link com.ninja_squad.console.InstanceState} of the application
     *
     * @param state to save
     */
    void save(InstanceState state);

    /**
     * Save a new {@link Route}
     *
     * @param route to save
     */
    void save(Route route);

    /**
     * Find a {@link Route} by its id
     *
     * @param routeId to find
     * @return the {@link Route}
     */
    Route findRoute(String routeId);

    /**
     * Save a new {@link com.ninja_squad.console.RouteState}
     *
     * @param routeState to save
     */
    void save(RouteState routeState);

    /**
     * Find the last {@link RouteState} by the route's id
     *
     * @param routeId to find
     * @return the last {@link RouteState} for this id
     */
    RouteState lastRouteState(String routeId);

    /**
     * Save a new statistic on an exchange
     *
     * @param routeStatistic to save
     */
    void save(RouteStatistic routeStatistic);

}
