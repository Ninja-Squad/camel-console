package com.ninja_squad.console;

import com.ninja_squad.console.subscriber.NotificationSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Slf4j
@Configuration
@EnableMongoRepositories(value = "com.ninja_squad.console.repository", repositoryImplementationPostfix = "CustomDefault")
@ImportResource({"classpath*:resthubContext.xml", "classpath*:applicationContext.xml"})
public class WebAppConfigurer {

    @Bean
    public NotificationSubscriber notificationSubscriber(){
        return new NotificationSubscriber();
    }

}