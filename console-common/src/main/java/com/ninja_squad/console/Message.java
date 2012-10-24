package com.ninja_squad.console;

import com.google.common.collect.Sets;
import lombok.Data;

import java.util.Collection;

@Data
public class Message {

    private String exchangeId;
    private String timestamp;
    private Collection<Notification> notifications = Sets.newHashSet();

}
