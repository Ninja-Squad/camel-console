package com.ninja_squad.console.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@ToString
@EqualsAndHashCode(exclude = "id")
@Document(collection = "statistics")
@CompoundIndexes({
    @CompoundIndex(name = "agg_stats", def = "{'elementId': 1, 'timeUnit': 1, 'range': 1}")
})
public class Statistic {

    public static final String ALL = "overall";

    @Id
    @Getter
    private String id;

    @Getter
    @Setter
    private String elementId;

    @Getter
    @Setter
    private long range;

    @Getter
    private TimeUnit timeUnit;

    @Getter
    private int failed;

    @Getter
    private int completed;

    @Getter
    private int min;

    @Getter
    private int max;

    @Getter
    private int average;

    public Statistic(String elementId, long range, TimeUnit timeUnit, int failed, int completed, int min, int max, int average) {
        this.average = average;
        this.completed = completed;
        this.failed = failed;
        this.max = max;
        this.min = min;
        this.range = range;
        this.timeUnit = timeUnit;
        this.elementId = elementId;
    }

    public synchronized void addFailed() {
        failed++;
    }

    public synchronized void addCompleted(int duration) {
        average = (average * completed + duration) / (completed + 1);
        min = duration < min ? duration : min;
        max = duration > max ? duration : max;
        completed++;
    }
}
