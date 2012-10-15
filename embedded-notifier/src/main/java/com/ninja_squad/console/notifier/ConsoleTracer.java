package com.ninja_squad.console.notifier;

import org.apache.camel.CamelContext;
import org.apache.camel.Processor;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.processor.interceptor.TraceEventHandler;
import org.apache.camel.processor.interceptor.TraceInterceptor;
import org.apache.camel.processor.interceptor.Tracer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConsoleTracer extends Tracer {

    private final List<TraceEventHandler> traceHandlers = new CopyOnWriteArrayList<TraceEventHandler>();

    private ConsoleEventNotifier notifier;

    public ConsoleTracer(ConsoleEventNotifier notifier, ConsoleTraceHandler traceHandler) {
        this.notifier = notifier;
        notifier.setTraceHandler(traceHandler);
        traceHandlers.add(traceHandler);
    }

    @Override
    public Processor wrapProcessorInInterceptors(CamelContext context, ProcessorDefinition<?> definition, Processor target, Processor nextTarget) throws Exception {
        //RouteDefinitionHelper.forceAssignIds(context, definition);
        return new TraceInterceptor(definition, target, null, this);
    }

    @Override
    public List<TraceEventHandler> getTraceHandlers() {
        return traceHandlers;
    }
}
