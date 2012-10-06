package com.ninja_squad.console.jmx;

import lombok.Data;
import org.joda.time.DateTime;

import java.util.Map;

@Data
public class CamelJmxNotification {

    private String exchangeId;

    private String source;

    private String destination;

    private Object body;

    private Map<String, String> headers;

    private Map<String, String> properties;

    private DateTime timestamp;

    private String breadcrumbId;

}
