package com.ninja_squad.console.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "routes")
public class Route  extends com.ninja_squad.console.Route {

    @Id
    @Getter
    @Setter
    private String id;

}
