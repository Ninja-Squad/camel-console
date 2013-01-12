package com.ninja_squad.console;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Sets;
import lombok.Data;

import java.util.Collection;

@Data
public class ExchangeStatistic {

    @JsonProperty("i")
    private String exchangeId;

    @JsonProperty("t")
    private long timestamp;

    @JsonProperty("d")
    private long duration;

    @JsonProperty("f")
    private boolean failed;

    @JsonProperty("e")
    private String exception;

    @JsonProperty("em")
    private String exceptionMessage;

    @JsonProperty("s")
    private Collection<StepStatistic> steps = Sets.newHashSet();

}
