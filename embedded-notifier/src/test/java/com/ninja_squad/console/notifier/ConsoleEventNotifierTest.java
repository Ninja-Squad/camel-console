package com.ninja_squad.console.notifier;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.management.event.DefaultEventFactory;
import org.apache.camel.management.event.ExchangeCompletedEvent;
import org.junit.Test;

import java.util.EventObject;

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
        notifier.setTraceHandler(mock(ConsoleTraceHandler.class));
        notifier.notify(event);

        //then notifyExchangeSentEvent should have been called
        verify(notifier).notifyExchangeCompletedEvent((ExchangeCompletedEvent) event);
    }

}
