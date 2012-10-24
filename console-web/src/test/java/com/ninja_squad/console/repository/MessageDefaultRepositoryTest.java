package com.ninja_squad.console.repository;

import com.mongodb.Mongo;
import com.ninja_squad.console.model.Message;
import com.ninja_squad.console.model.TimestampCount;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.inject.Inject;
import java.net.UnknownHostException;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class MessageDefaultRepositoryTest {

    static int port = 27016;
    private MongodProcess mongod;

    @Inject
    private MongoTemplate mongoTemplate;

    @Inject
    private MessageDefaultRepository repository;

    @Configuration
    static class ContextConfiguration {

        @Bean
        public MongoTemplate mongoTemplate() throws UnknownHostException {
            Mongo mongo = new Mongo("localhost", port);
            return new MongoTemplate(mongo, "test");
        }

        @Bean
        public MessageDefaultRepository messageDefaultRepository() {
            return new MessageDefaultRepository();
        }

    }

    @Before
    public void setUp() throws Exception {
        MongodConfig mongodConfig = new MongodConfig(Version.Main.V2_0, port, Network.localhostIsIPv6());
        MongodStarter runtime = MongodStarter.getDefaultInstance();
        MongodExecutable mongodExecutable = runtime.prepare(mongodConfig);
        mongod = mongodExecutable.start();
    }

    @After
    public void tearDown() throws Exception {
        if (mongod != null) mongod.stop();
    }

    @Test
    public void shouldMapReduceNotifications() throws Exception {
        // given 5 messages over 3 seconds
        DateTime timestamp = new DateTime(2012, 10, 23, 16, 00, 01, 03);
        saveMessage(timestamp);
        timestamp = new DateTime(2012, 10, 23, 16, 00, 02, 01);
        saveMessage(timestamp);
        timestamp = new DateTime(2012, 10, 23, 16, 00, 02, 90);
        saveMessage(timestamp);
        timestamp = new DateTime(2012, 10, 23, 16, 00, 03, 03);
        saveMessage(timestamp);
        timestamp = new DateTime(2012, 10, 23, 16, 00, 03, 98);
        saveMessage(timestamp);

        // when mapreducing them
        List<TimestampCount> count = repository.getNotificationBySecond();

        // then
        assertThat(count).hasSize(3);
        DateTime time = new DateTime(2012, 10, 23, 16, 00, 01);
        assertThat(count.get(0)).isEqualTo(getTimestampCount(time, 1));
        assertThat(count.get(1)).isEqualTo(getTimestampCount(time.plusSeconds(1), 2));
        assertThat(count.get(2)).isEqualTo(getTimestampCount(time.plusSeconds(2), 2));
    }

    private TimestampCount getTimestampCount(DateTime dateTime, long count) {
        TimestampCount timestampCount = new TimestampCount();
        long millis = dateTime.getMillis();
        timestampCount.set_id(millis);
        timestampCount.setValue(count);
        return timestampCount;
    }

    private void saveMessage(DateTime timestamp) {
        Message message = new Message();
        message.setTimestamp(String.valueOf(timestamp.getMillis()));
        mongoTemplate.save(message);
    }
}
