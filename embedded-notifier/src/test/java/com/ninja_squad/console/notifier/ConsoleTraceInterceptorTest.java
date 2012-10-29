package com.ninja_squad.console.notifier;

import org.apache.camel.AsyncCallback;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.model.ProcessorDefinition;
import org.junit.Test;

import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

public class ConsoleTraceInterceptorTest {

    @Test
    public void notifyShouldHandleCreateANotificationAndUpdateStats() throws Exception {
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
        ConsoleTraceInterceptor consoleTraceInterceptor = spy(new ConsoleTraceInterceptor(mock(ConsoleEventNotifier.class), node, processor));
        consoleTraceInterceptor.process(exchange, new AsyncCallback() {
            @Override
            public void done(boolean doneSync) {

            }
        });

        //then notifyExchangeSentEvent should have been called
        verify(consoleTraceInterceptor).recordTime(any(Exchange.class), anyLong());
    }
}
