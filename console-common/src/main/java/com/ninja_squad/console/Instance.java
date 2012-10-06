package com.ninja_squad.console;

import lombok.Getter;
import lombok.Setter;

public class Instance {

    public String host = "localhost";

    public int port = 1099;

    public String context = "jmxrmi/camel";

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

