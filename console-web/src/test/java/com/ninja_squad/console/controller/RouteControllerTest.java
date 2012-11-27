package com.ninja_squad.console.controller;

import com.mongodb.Mongo;
import com.ninja_squad.console.model.Route;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.resthub.test.AbstractWebTest;
import org.resthub.web.Client;
import org.resthub.web.Http;
import org.resthub.web.Response;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.fest.assertions.api.Assertions.assertThat;

public class RouteControllerTest extends AbstractWebTest {

    private static MongodProcess mongod;
    private int mongoPort = 27016;

    @Configuration
    @Profile("mongodb-test")
    public class TestConfig extends AbstractMongoConfiguration {

        @Override
        protected String getDatabaseName() {
            return "test";
        }

        @Override
        public Mongo mongo() throws Exception {
            return new Mongo("localhost", mongoPort);
        }
    }

    @AfterClass
    public void tearDownDatabase() throws Exception {
        super.afterClass();
        if (mongod != null) { mongod.stop(); }
    }

    public RouteControllerTest() throws IOException {
        MongodConfig mongodConfig = new MongodConfig(Version.Main.V2_0, mongoPort, Network.localhostIsIPv6());
        MongodStarter runtime = MongodStarter.getDefaultInstance();
        MongodExecutable mongodExecutable = runtime.prepare(mongodConfig);
        mongod = mongodExecutable.start();
        this.activeProfiles = "mongodb-test, resthub-web-server";
        this.annotationBasedConfig = true;
        this.contextLocations = "com.ninja_squad.console";
    }

    protected String rootUrl() {
        return "http://localhost:" + port + "/route";
    }

    @Test
    public void shouldFindRouteByRouteId() throws Exception {
        Client httpClient = new Client();
        Route route = new Route("quoteWS");
        route = httpClient.url(rootUrl()).jsonPost(route).resource(route.getClass());
        assertThat(route).isNotNull();

        Response response = httpClient.url(rootUrl() + "/quoteWS").get();
        assertThat(response.getStatus()).isEqualTo(Http.OK);
    }
}
