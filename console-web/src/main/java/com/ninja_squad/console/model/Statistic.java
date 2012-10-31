package com.ninja_squad.console.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Document(collection = "statistics")
public class Statistic {

    private long range;
    private TimeUnit timeUnit;
    private int failed;
    private int completed;
    private int min;
    private int max;
    private int average;

    public synchronized void addFailed() {
        failed++;
    }

    public synchronized void addCompleted(int duration) {
        computeAverage(duration);
        updateMin(duration);
        updateMax(duration);
        completed++;
    }

    public synchronized void computeAverage(int duration) {
        average = (average * completed + duration) / (completed + 1);
    }

    public synchronized void updateMin(int duration) {
        min = duration < min ? duration : min;
    }

    public synchronized void updateMax(int duration) {
        max = duration > max ? duration : max;
    }
}
