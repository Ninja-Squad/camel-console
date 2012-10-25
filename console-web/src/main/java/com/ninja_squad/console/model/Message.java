package com.ninja_squad.console.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "notifications")
public class Message extends com.ninja_squad.console.Message {

    @Id
    @Getter
    @Setter
    private String id;

}
