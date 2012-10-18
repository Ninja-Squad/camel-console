package com.ninja_squad.console.notifier;

import com.google.common.collect.Sets;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.management.event.DefaultEventFactory;
import org.apache.camel.management.event.ExchangeCompletedEvent;
import org.apache.camel.management.event.ExchangeFailedEvent;
import org.junit.Test;

import java.util.Collection;
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
        doNothing().when(notifier).notifyExchangeCompletedEvent(any(ExchangeCompletedEvent.class));
        notifier.setTraceHandler(mock(ConsoleTraceHandler.class));
        notifier.notify(event);

        //then notifyExchangeSentEvent should have been called
        verify(notifier).notifyExchangeCompletedEvent((ExchangeCompletedEvent) event);
    }

    @Test
    public void notifyExchangeCompletedEventShouldSaveThePreviousNotifications() throws Exception {
        //given an ExchangeSentEvent
        CamelContext camelContext = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setExchangeId("1");
        ExchangeCompletedEvent event = (ExchangeCompletedEvent) new DefaultEventFactory().createExchangeCompletedEvent(exchange);

        //when the EventNotifier receives it
        ConsoleEventNotifier notifier = spy(new ConsoleEventNotifier());
        ConsoleTraceHandler traceHandler = mock(ConsoleTraceHandler.class);
        Notification notif1 = new Notification();
        notif1.setRouteId("1");
        notif1.setExchangeId("1");
        notif1.setStep(1);
        Notification notif2 = new Notification();
        notif2.setRouteId("1");
        notif2.setExchangeId("1");
        notif2.setStep(1);
        Collection<Notification> notifications = Sets.newHashSet(notif1, notif2);
        doReturn(notifications).when(traceHandler).getNotifications("1");
        notifier.setTraceHandler(traceHandler);
        NotifierRepository repository = mock(NotifierRepository.class);
        notifier.setRepository(repository);
        notifier.notifyExchangeCompletedEvent(event);

        //then notifyExchangeSentEvent should have been called
        Message message = new Message();
        message.setExchangeId("1");
        message.setNotifications(notifications);
        verify(repository).save(message);
        verify(traceHandler).removeNotifications("1");
    }

    @Test
        public void notifyExchangeFailedEventShouldSaveThePreviousNotifications() throws Exception {
            //given an ExchangeSentEvent
            CamelContext camelContext = new DefaultCamelContext();
            Exchange exchange = new DefaultExchange(camelContext);
            exchange.setExchangeId("1");
            ExchangeFailedEvent event = (ExchangeFailedEvent) new DefaultEventFactory().createExchangeFailedEvent(exchange);

            //when the EventNotifier receives it
            ConsoleEventNotifier notifier = spy(new ConsoleEventNotifier());
            ConsoleTraceHandler traceHandler = mock(ConsoleTraceHandler.class);
            Notification notif1 = new Notification();
            notif1.setRouteId("1");
            notif1.setExchangeId("1");
            notif1.setStep(1);
            Notification notif2 = new Notification();
            notif2.setRouteId("1");
            notif2.setExchangeId("1");
            notif2.setStep(1);
            Collection<Notification> notifications = Sets.newHashSet(notif1, notif2);
            doReturn(notifications).when(traceHandler).getNotifications("1");
            notifier.setTraceHandler(traceHandler);
            NotifierRepository repository = mock(NotifierRepository.class);
            notifier.setRepository(repository);
            notifier.notifyExchangeFailedEvent(event);

            //then notifyExchangeSentEvent should have been called
            Message message = new Message();
            message.setExchangeId("1");
            message.setNotifications(notifications);
            verify(repository).save(message);
            verify(traceHandler).removeNotifications("1");
        }


}
