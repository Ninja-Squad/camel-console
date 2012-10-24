package com.ninja_squad.console.controller;

import com.ninja_squad.console.model.Message;
import com.ninja_squad.console.repository.MessageRepository;
import org.resthub.web.controller.RepositoryBasedRestController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;
import javax.inject.Named;

@Controller
@RequestMapping(value = "/api/route")
public class RouteController extends RepositoryBasedRestController<Message, String, MessageRepository> {

    @Inject
    @Named("routeRepository")
    @Override
    public void setRepository(MessageRepository repository) {
        this.repository = repository;
    }

}
