package com.ninja_squad.console;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteStatistic {

    private long timestamp;
    private String exchangeId;
    private String routeId;
    private boolean failed;
    private int duration;

}
