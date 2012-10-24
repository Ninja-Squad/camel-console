package com.ninja_squad.console.repository;

import com.ninja_squad.console.model.TimestampCount;

import java.util.List;

public interface MessageRepositoryCustom {

    /**
     * Return a Collection of timestamp with the number of messages.
     */
    public List<TimestampCount> getMessagesPerSecond();

}
