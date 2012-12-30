package com.ninja_squad.console.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document
public class StepStatistic {

    @Field("r")
    private String routeId;

    @Field("d")
    private String destination;

    @Field("i")
    private String exchangeId;

    @Field("t")
    private long timestamp;

    @Field("s")
    private int step;

    @Field("c")
    private long duration;

    @Field("f")
    private boolean failed;

    @Field("eb")
    private Object errorBody;

    @Field("eh")
    private Object errorHeaders;

    @Field("e")
    private String exception;

    @Field("m")
    private String exceptionMessage;

}