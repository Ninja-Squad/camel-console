package com.ninja_squad.console;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Route {

    @Getter
    private String routeId;

    @Getter
    private String uri;

    @Getter
    private State state;

    @Getter
    private Instance instance;

    //Builder

    public Route(String routeId) {
        this.routeId = routeId;
    }

    /**
     * Set the uri of the route
     *
     * @param uri
     * @return the current instance
     */
    public Route uri(String uri) {
        this.uri = uri;
        return this;
    }

    /**
     * Set the uri of the route
     *
     * @param state
     * @return the current instance
     */
    public Route state(State state) {
        this.state = state;
        return this;
    }

}
