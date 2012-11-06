package com.ninja_squad.console.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "notifications")
public class ExchangeStatistic extends com.ninja_squad.console.ExchangeStatistic {

    @Id
    @Getter
    @Setter
    private String id;

    @Getter
    @Setter
    private Boolean handled;
}
