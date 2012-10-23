package com.ninja_squad.console.controller;

import com.ninja_squad.console.model.Route;
import com.ninja_squad.console.repository.RouteRepository;
import org.resthub.web.controller.RepositoryBasedRestController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;
import javax.inject.Named;

@Controller
@RequestMapping(value = "/api/route")
public class RouteController extends RepositoryBasedRestController<Route, String, RouteRepository> {

    @Inject
    @Named("routeRepository")
    @Override
    public void setRepository(RouteRepository repository) {
        this.repository = repository;
    }

}
