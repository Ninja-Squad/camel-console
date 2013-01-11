package com.ninja_squad.console.model;

import com.google.common.collect.Sets;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Collection;

@Data
@Document(collection = "exchange_statistics")
public class ExchangeStatistic {

    @Id
    private String id;

    @Indexed
    private Boolean handled;

    @Field("i")
    private String exchangeId;

    @Field("t")
    private long timestamp;

    @Field("d")
    private long duration;

    @Field("f")
    private boolean failed;

    @Field("e")
    private String exception;

    @Field("em")
    private String exceptionMessage;

    @Field("s")
    private Collection<StepStatistic> steps = Sets.newHashSet();
}
