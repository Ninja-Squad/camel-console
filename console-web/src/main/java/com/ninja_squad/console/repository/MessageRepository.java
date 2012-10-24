package com.ninja_squad.console.repository;

import com.ninja_squad.console.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRepository extends MongoRepository<Message, String>, MessageRepositoryCustom {

}
