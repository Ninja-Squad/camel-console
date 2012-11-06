package com.ninja_squad.console.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "route_statistics")
public class RouteStatistic extends com.ninja_squad.console.RouteStatistic {

    @Id
    @Getter
    @Setter
    private String id;

    @Getter
    @Setter
    private Boolean handled;

}
