package com.ninja_squad.console;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StepStatistic {

    @JsonProperty("r")
    private String routeId;

    @JsonProperty("d")
    private String destination;

    @JsonProperty("i")
    private String exchangeId;

    @JsonProperty("t")
    private long timestamp;

    @JsonProperty("s")
    private int step;

    @JsonProperty("c")
    private long duration;

    @JsonProperty("f")
    private boolean failed;

    @JsonProperty("eb")
    private Object errorBody;

    @JsonProperty("eh")
    private Object errorHeaders;

    @JsonProperty("e")
    private String exception;

    @JsonProperty("m")
    private String exceptionMessage;

}
