package com.ninja_squad.console;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class Instance {

    private String host = "localhost";

    private int port = 1099;

    private String context = "jmxrmi/camel";

    @Getter
    @Setter
    private Map<String, Route> routes = new HashMap<String, Route>();

    @Getter
    @Setter
    public State state = State.Unknown;

    /**
     * Build a complete url for jmx
     *
     * @return a String of the jmx url
     */
    public String url() {
        return "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/" + context;
    }

    //Builder

    /**
     * Set the port of the install
     *
     * @param port
     * @return the current instance
     */
    public Instance port(int port) {
        this.port = port;
        return this;
    }

    /**
     * Set the host of the install
     *
     * @param host
     * @return the current instance
     */
    public Instance host(String host) {
        this.host = host;
        return this;
    }

    /**
     * Set the jmx context of the install
     *
     * @param context
     * @return the current instance
     */
    public Instance context(String context) {
        this.context = context;
        return this;
    }
}

