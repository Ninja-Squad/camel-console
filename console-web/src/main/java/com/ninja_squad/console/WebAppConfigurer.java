package com.ninja_squad.console;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(value = "com.ninja_squad.console.repository", repositoryImplementationPostfix = "CustomDefault")
@ImportResource({"classpath*:resthubContext.xml", "classpath*:applicationContext.xml"})
public class WebAppConfigurer {

}