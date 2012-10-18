package com.ninja_squad.console.notifier;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.joda.time.DateTime;

public class Notification {

    private String routeId;
    private String destination;
    private String exchangeId;
    @JsonIgnore
    private Object body;
    private String timestamp;
    private String source;
    private int step;

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public void setDestination(String endpoint) {
        this.destination = endpoint;
    }

    public void setExchangeId(String exchangeId) {
        this.exchangeId = exchangeId;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public void setTimestamp(DateTime now) {
        this.timestamp = now.toString();
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "routeId='" + routeId + '\'' +
                ", exchangeId='" + exchangeId + '\'' +
                ", destination='" + destination + '\'' +
                ", source='" + source + '\'' +
                ", step='" + source + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
