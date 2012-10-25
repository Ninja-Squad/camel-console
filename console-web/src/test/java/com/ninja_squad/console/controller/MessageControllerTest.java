package com.ninja_squad.console.controller;

import com.google.common.collect.Lists;
import com.ninja_squad.console.model.TimestampCount;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class MessageControllerTest {

    @Test
    public void shouldAddNullValueForMissingPoints() throws Exception {
        // given a time series with missing value
        MessageController controller = new MessageController();
        TimestampCount count1 = new TimestampCount();
        count1.set_id(1351153523000L);
        count1.setValue(1);TimestampCount count2 = new TimestampCount();
        count2.set_id(1351153525000L);
        count2.setValue(1);TimestampCount count3 = new TimestampCount();
        count3.set_id(1351153529000L);
        count3.setValue(1);
        List<TimestampCount> counts = Lists.newArrayList(count1, count2, count3);

        // when toJson
        DateTime now = new DateTime(2012, 10, 24, 00, 00, 00);
        String json = controller.toJson(counts, now);

        // then the result should have null values to fill the missing datas
        assertThat(json).isEqualTo("[[1351153523000,1], [1351153524000,0], [1351153525000,1], [1351153526000,0], " +
                "[1351153528000,0], [1351153529000,1], [1351153530000,0], [1351029600000,0]]");
    }
}
