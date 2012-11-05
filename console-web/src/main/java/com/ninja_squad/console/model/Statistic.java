package com.ninja_squad.console.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@ToString
@EqualsAndHashCode(exclude = "id")
@Document(collection = "statistics")
public class Statistic {

    @Id
    private String id;

    @Getter
    private long range;
    private TimeUnit timeUnit;
    private int failed;
    private int completed;
    private int min;
    private int max;
    private int average;

    public Statistic(long range, TimeUnit timeUnit, int failed, int completed, int min, int max, int average) {
        this.average = average;
        this.completed = completed;
        this.failed = failed;
        this.max = max;
        this.min = min;
        this.range = range;
        this.timeUnit = timeUnit;
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

    public String toJson() {
        return "[" + range + "," + failed + "," + completed + "," + min + "," + max + "," + average + "]";
    }
}
