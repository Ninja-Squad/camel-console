package com.ninja_squad.console.notifier;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.model.ProcessorDefinition;
import org.junit.Test;

import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class ConsoleTraceHandlerTest {

    @Test
    public void notifyShouldHandleTrace() throws Exception {
        //given an ExchangeSentEvent
        CamelContext camelContext = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(camelContext);
        ProcessorDefinition node = new ProcessorDefinition() {
            @Override
            public List<ProcessorDefinition<?>> getOutputs() {
                return null;
            }

            @Override
            public boolean isOutputSupported() {
                return false;
            }
        };
        Processor processor = new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {

            }
        };

        //when the EventNotifier receives it
        ConsoleTraceHandler handler = spy(new ConsoleTraceHandler());
        handler.traceExchange(node, processor, null, exchange);

        //then notifyExchangeSentEvent should have been called
        verify(handler).addNotification(any(String.class), any(Notification.class));
    }
}
