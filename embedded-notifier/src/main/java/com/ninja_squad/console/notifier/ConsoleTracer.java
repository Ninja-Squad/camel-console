package com.ninja_squad.console.notifier;

import org.apache.camel.CamelContext;
import org.apache.camel.Processor;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.spi.InterceptStrategy;

public class ConsoleTracer implements InterceptStrategy {

    private ConsoleEventNotifier eventNotifier;

    public ConsoleTracer(ConsoleEventNotifier eventNotifier) {
        this.eventNotifier = eventNotifier;
    }

    @Override
    public Processor wrapProcessorInInterceptors(CamelContext context, ProcessorDefinition<?> definition, Processor target, Processor nextTarget) throws Exception {
        return new ConsoleTraceInterceptor(eventNotifier, definition, target);
    }

}
