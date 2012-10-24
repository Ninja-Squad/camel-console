package com.ninja_squad.console.controller;

import com.ninja_squad.console.model.Message;
import com.ninja_squad.console.model.TimestampCount;
import com.ninja_squad.console.repository.MessageRepository;
import org.resthub.web.controller.RepositoryBasedRestController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Controller
@RequestMapping(value = "/api/message")
public class MessageController extends RepositoryBasedRestController<Message, String, MessageRepository> {

    @Inject
    @Named("messageRepository")
    @Override
    public void setRepository(MessageRepository repository) {
        this.repository = repository;
    }


    @RequestMapping(value = "/second", method = RequestMethod.GET, produces = "application/json")
    public List<TimestampCount> getMessagesPerSecond() {
        return repository.getMessagesPerSecond();
    }
}
