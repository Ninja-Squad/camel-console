package com.ninja_squad.console;

import lombok.*;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class Route {

    @Getter
    private String routeId;

    @Getter
    private String uri;

    @Getter
    private State state;

    @Getter
    @Setter
    private String definition;

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
