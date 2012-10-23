package com.ninja_squad.console.repository;

import com.ninja_squad.console.model.Route;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RouteRepository  extends MongoRepository<Route, String> {

}
