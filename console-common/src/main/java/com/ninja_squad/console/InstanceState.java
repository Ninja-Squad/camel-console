package com.ninja_squad.console;

public class InstanceState {

    private String name;

    private State state;

    private String timestamp;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        return "InstanceState{" +
                "name='" + name + '\'' +
                ", state=" + state +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
