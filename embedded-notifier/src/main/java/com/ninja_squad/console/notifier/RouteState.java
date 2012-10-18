package com.ninja_squad.console.notifier;

import com.ninja_squad.console.State;

public class RouteState {
    private String routeId;

    private State state;

    private String timestamp;

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "RouteState{" +
                "routeId='" + routeId + '\'' +
                ", state=" + state +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
