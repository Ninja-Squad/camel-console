package com.ninja_squad.console;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories("com.ninja_squad.console.repository")
@ImportResource({"classpath*:resthubContext.xml", "classpath*:applicationContext.xml"})
public class WebAppConfigurer {

}