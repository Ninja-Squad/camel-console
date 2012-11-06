package com.ninja_squad.console;

import lombok.Data;

@Data
public class RouteState {

    private String routeId;
    private State state;
    private long timestamp;

}
