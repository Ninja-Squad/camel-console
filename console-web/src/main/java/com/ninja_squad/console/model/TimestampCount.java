package com.ninja_squad.console.model;

import lombok.*;

@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class TimestampCount {
    @Setter
    private long _id;
    @Setter
    private long value;

    public long getId() {
        return _id;
    }
}
