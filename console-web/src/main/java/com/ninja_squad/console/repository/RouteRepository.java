package com.ninja_squad.console.repository;

import com.ninja_squad.console.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RouteRepository extends MongoRepository<Message, String> {

}
