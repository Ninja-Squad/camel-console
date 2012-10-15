package com.ninja_squad.console.notifier;

import org.apache.camel.Processor;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.processor.interceptor.TraceFormatter;
import org.apache.camel.processor.interceptor.TraceInterceptor;
import org.apache.camel.processor.interceptor.Tracer;

public class ConsoleTracer extends TraceInterceptor {

    public ConsoleTracer(ProcessorDefinition<?> node, Processor target, TraceFormatter formatter, Tracer tracer) {
        super(node, target, formatter, tracer);
    }
}
