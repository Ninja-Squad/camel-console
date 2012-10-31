package com.ninja_squad.console.repository;

import com.ninja_squad.console.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {

    List<Message> findByHandledIsFalseOrderByTimestampAsc();

}
