package com.ninja_squad.console.controller.converter;

import com.ninja_squad.console.model.TimeUnit;
import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyEditorSupport;

@Slf4j
public class TimeUnitEnumConverter extends PropertyEditorSupport {

    @Override
    public void setAsText(final String text) throws IllegalArgumentException {
        TimeUnit unit = TimeUnit.valueOf((text.trim().toUpperCase()));
        log.debug("converting " + text + " to " + unit);
        setValue(unit);
    }
}
