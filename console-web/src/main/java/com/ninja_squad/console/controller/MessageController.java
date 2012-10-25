package com.ninja_squad.console.controller;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.ninja_squad.console.model.Message;
import com.ninja_squad.console.model.TimestampCount;
import com.ninja_squad.console.repository.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.resthub.web.controller.RepositoryBasedRestController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Controller
@RequestMapping(value = "/api/message")
@Slf4j
public class MessageController extends RepositoryBasedRestController<Message, String, MessageRepository> {

    @Inject
    @Named("messageRepository")
    @Override
    public void setRepository(MessageRepository repository) {
        this.repository = repository;
    }


    @RequestMapping(value = "/second", method = RequestMethod.GET)
    @ResponseBody
    public String getMessagesPerSecond() {
        List<TimestampCount> messagesPerSecond = repository.getMessagesPerSecond();
        return toJson(messagesPerSecond, DateTime.now());
    }

    protected String toJson(List<TimestampCount> messagesPerSecond, DateTime now) {
        if (messagesPerSecond.isEmpty()) return "";
        TimestampCount last = messagesPerSecond.get(0);
        List<TimestampCount> counts = Lists.newArrayList();
        for (TimestampCount timestampCount : messagesPerSecond) {
            //first missing points to 0
            if (timestampCount.getId() - last.getId() > 1000) {
                TimestampCount zero = new TimestampCount();
                zero.set_id(last.getId() + 1000);
                zero.setValue(0);
                counts.add(zero);
            }
            //last missing point to zero
            if (timestampCount.getId() - last.getId() > 2000) {
                TimestampCount zero = new TimestampCount();
                zero.set_id(timestampCount.getId() - 1000);
                zero.setValue(0);
                counts.add(zero);
            }
            last = timestampCount;
            counts.add(timestampCount);
        }
        //last point to zero
        TimestampCount after = new TimestampCount();
        after.set_id(last.getId() + 1000);
        after.setValue(0);
        counts.add(after);
        //current point to zero
        TimestampCount current = new TimestampCount();
        current.set_id(now.getMillis());
        current.setValue(0);
        counts.add(current);

        List<String> json = Lists.transform(counts, new Function<TimestampCount, String>() {
            @Override
            public String apply(TimestampCount input) {
                if (input == null) return "null";
                return input.toJson();
            }
        });
        log.debug(json.toString());
        return json.toString();
    }
}
