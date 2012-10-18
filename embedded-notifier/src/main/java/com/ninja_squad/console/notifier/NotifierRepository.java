package com.ninja_squad.console.notifier;

import com.mongodb.Mongo;
import org.jongo.Jongo;
import org.jongo.MongoCollection;

import java.net.UnknownHostException;
import java.util.Collection;

public class NotifierRepository {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NotifierRepository.class);

    private MongoCollection mongoCollection;

    public NotifierRepository() {
        Mongo mongo = null;
        try {
            mongo = new Mongo("127.0.0.1", 27017);
        } catch (UnknownHostException e) {
            log.error("No Mongo running");
        }
        Jongo jongo = new Jongo(mongo.getDB("console"));
        mongoCollection = jongo.getCollection("notifications");
    }

    public void save(Collection<Notification> notifications) {
        Message message = new Message();
        message.setNotifications(notifications);
        mongoCollection.save(message);
    }
}
