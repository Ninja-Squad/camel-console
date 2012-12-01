package com.ninja_squad.console.model;

import lombok.Getter;
import lombok.ToString;

/**
 * Objects used for map reducing statistics,
 * as mongo will return a composed object with a key (_id) and a value (here the statistic aggregated).
 */
@ToString
public class MapReducedStatistic {

    Object _id;

    @Getter
    Statistic value;

}
