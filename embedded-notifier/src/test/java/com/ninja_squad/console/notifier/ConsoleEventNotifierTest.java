package com.ninja_squad.console.notifier;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.management.event.DefaultEventFactory;
import org.apache.camel.management.event.ExchangeCompletedEvent;
import org.apache.camel.model.ProcessorDefinition;
import org.junit.Test;

import java.util.EventObject;
import java.util.List;

import static org.mockito.Mockito.*;

public class ConsoleEventNotifierTest {

    @Test
    public void notifyShouldHandleExchangeCompletedEvent() throws Exception {
        //given an ExchangeSentEvent
        CamelContext camelContext = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(camelContext);
        EventObject event = new DefaultEventFactory().createExchangeCompletedEvent(exchange);

        //when the EventNotifier receives it
        ConsoleEventNotifier notifier = spy(new ConsoleEventNotifier());
        notifier.setRepository(mock(NotifierRepository.class));
        notifier.notify(event);

        //then notifyExchangeSentEvent should have been called
        verify(notifier).notifyExchangeCompletedEvent((ExchangeCompletedEvent) event);
    }

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
        ConsoleEventNotifier notifier = spy(new ConsoleEventNotifier());
        notifier.setRepository(mock(NotifierRepository.class));
        notifier.traceExchange(node, processor, null, exchange);

        //then notifyExchangeSentEvent should have been called
        verify(notifier).addNotification(any(String.class), any(Notification.class));
    }
}
