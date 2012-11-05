package com.ninja_squad.console.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExchangeStatistic {

    private String exchangeId;
    private String routeId;
    private boolean failed;
    private int duration;

}
