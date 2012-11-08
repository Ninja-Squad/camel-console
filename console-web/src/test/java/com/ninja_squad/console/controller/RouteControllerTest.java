package com.ninja_squad.console.controller;

import com.ninja_squad.console.model.Route;
import org.resthub.test.AbstractWebTest;
import org.resthub.web.Client;
import org.resthub.web.Http;
import org.resthub.web.Response;
import org.springframework.context.annotation.ComponentScan;
import org.testng.annotations.Test;

import static org.fest.assertions.api.Assertions.assertThat;

@ComponentScan("com.ninja_squad.console")
public class RouteControllerTest extends AbstractWebTest {

    public RouteControllerTest() {
        this.activeProfiles = "resthub-mongodb, resthub-web-server";
    }

    protected String rootUrl() {
        return "http://localhost:" + port + "/api/route";
    }

    @Test
    public void shouldFindRouteByRouteId() throws Exception {
        // TODO unit test does not work
        Client httpClient = new Client();
        Route route = new Route("quoteWS");
        route = httpClient.url(rootUrl()).jsonPost(route).resource(route.getClass());
        assertThat(route).isNotNull();

        Response response = httpClient.url(rootUrl() + "/quoteWS").get();
        assertThat(response.getStatus()).isEqualTo(Http.OK);
    }
}
