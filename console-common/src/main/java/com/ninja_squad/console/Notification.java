package com.ninja_squad.console;

import lombok.Data;

@Data
public class Notification {

    private String routeId;
    private String destination;
    private String exchangeId;
    private long timestamp;
    private int step;
    private long duration;
    private boolean failed;
    private Object errorBody;
    private Object errorHeaders;
    private String exception;
    private String exceptionMessage;

}
