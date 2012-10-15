package com.ninja_squad.console.notifier;

import org.apache.camel.CamelContext;
import org.apache.camel.Processor;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.processor.interceptor.TraceInterceptor;
import org.apache.camel.processor.interceptor.Tracer;

public class ConsoleTracer extends Tracer {

    public ConsoleTracer(ConsoleTraceHandler traceHandler) {
        super.getTraceHandlers().clear();
        super.getTraceHandlers().add(traceHandler);
    }

    @Override
    public Processor wrapProcessorInInterceptors(CamelContext context, ProcessorDefinition<?> definition, Processor target, Processor nextTarget) throws Exception {
        //RouteDefinitionHelper.forceAssignIds(context, definition);
        return new TraceInterceptor(definition, target, null, this);
    }

}
