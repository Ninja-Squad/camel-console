package com.ninja_squad.console;

import com.google.common.collect.Sets;
import lombok.Data;

import java.util.Collection;

@Data
public class ExchangeStatistic {

    private String exchangeId;
    private long timestamp;
    private long duration;
    private boolean failed;
    private String exception;
    private String exceptionMessage;
    private Collection<StepStatistic> steps = Sets.newHashSet();

}
