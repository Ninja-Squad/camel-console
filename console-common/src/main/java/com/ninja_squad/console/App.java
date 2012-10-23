package com.ninja_squad.console;

import java.util.HashSet;
import java.util.Set;

public class App {

    private String name;

    private String version;

    private Set<Instance> instances = new HashSet<Instance>();

    public App() {
    }

    public App(String name) {
        this.name = name;
    }

    /**
     * Add a instance to the current app
     *
     * @param instance
     * @return the current instance
     */
    public App addInstance(Instance instance) {
        instances.add(instance);
        return this;
    }
}