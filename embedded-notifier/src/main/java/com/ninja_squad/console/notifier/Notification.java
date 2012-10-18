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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification)) return false;

        Notification that = (Notification) o;

        if (step != that.step) return false;
        if (body != null ? !body.equals(that.body) : that.body != null) return false;
        if (destination != null ? !destination.equals(that.destination) : that.destination != null) return false;
        if (exchangeId != null ? !exchangeId.equals(that.exchangeId) : that.exchangeId != null) return false;
        if (routeId != null ? !routeId.equals(that.routeId) : that.routeId != null) return false;
        if (source != null ? !source.equals(that.source) : that.source != null) return false;
        if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = routeId != null ? routeId.hashCode() : 0;
        result = 31 * result + (destination != null ? destination.hashCode() : 0);
        result = 31 * result + (exchangeId != null ? exchangeId.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + step;
        return result;
    }
}
