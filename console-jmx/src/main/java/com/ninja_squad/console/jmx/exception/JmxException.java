package com.ninja_squad.console.jmx.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JmxException extends RuntimeException {

    public JmxException(String message, Exception e) {
        super(message, e);
        log.error(message, e);
    }

    public JmxException(String message) {
        super(message);
        log.error(message);
    }
}