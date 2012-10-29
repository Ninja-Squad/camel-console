package com.ninja_squad.console.notifier;

import com.google.common.collect.Sets;
import com.ninja_squad.console.Message;
import com.ninja_squad.console.Notification;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.management.event.DefaultEventFactory;
import org.apache.camel.management.event.ExchangeCompletedEvent;
import org.apache.camel.management.event.ExchangeFailedEvent;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collection;
import java.util.EventObject;
import java.util.Properties;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ConsoleEventNotifierTest {

    @Test
    public void notifyShouldHandleExchangeCompletedEvent() throws Exception {
        //given an ExchangeSentEvent
        CamelContext camelContext = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(camelContext);
        EventObject event = new DefaultEventFactory().createExchangeCompletedEvent(exchange);

        //when the EventNotifier receives it
        ConsoleEventNotifier notifier = spy(new ConsoleEventNotifier(mock(Properties.class)));
        doNothing().when(notifier).notifyExchangeCompletedEvent(any(ExchangeCompletedEvent.class));
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
        ConsoleEventNotifier notifier = spy(new ConsoleEventNotifier(mock(Properties.class)));
        Notification notif1 = new Notification();
        notif1.setRouteId("1");
        notif1.setExchangeId("1");
        notif1.setStep(0);
        Notification notif2 = new Notification();
        notif2.setRouteId("1");
        notif2.setExchangeId("1");
        notif2.setStep(1);
        Collection<Notification> notifications = Sets.newHashSet(notif1, notif2);
        ConsoleRepository repository = mock(ConsoleRepositoryJongo.class);
        doReturn(notifications).when(notifier).getNotifications("1");
        notifier.setRepository(repository);
        notifier.notifyExchangeCompletedEvent(event);

        //then save and removeNotifications should have been called
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(repository).save(captor.capture());
        Message message = captor.getValue();
        assertThat(message.getDuration()).isEqualTo(0);
        assertThat(message.getExchangeId()).isEqualTo("1");
        assertThat(message.getNotifications()).isEqualTo(notifications);
        assertThat(message.getTimestamp()).isNotEqualTo(0);
        assertThat(message.isFailed()).isFalse();

        verify(notifier).removeNotifications("1");
    }

    @Test
    public void notifyExchangeFailedEventShouldSaveThePreviousNotifications() throws Exception {
        //given an ExchangeSentEvent
        CamelContext camelContext = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setExchangeId("1");
        ExchangeFailedEvent event = (ExchangeFailedEvent) new DefaultEventFactory().createExchangeFailedEvent(exchange);

        //when the EventNotifier receives it
        ConsoleEventNotifier notifier = spy(new ConsoleEventNotifier(mock(Properties.class)));
        Notification notif1 = new Notification();
        notif1.setRouteId("1");
        notif1.setExchangeId("1");
        notif1.setStep(1);
        Notification notif2 = new Notification();
        notif2.setRouteId("1");
        notif2.setExchangeId("1");
        notif2.setStep(1);
        Collection<Notification> notifications = Sets.newHashSet(notif1, notif2);
        doReturn(notifications).when(notifier).getNotifications("1");
        ConsoleRepository repository = mock(ConsoleRepositoryJongo.class);
        notifier.setRepository(repository);
        notifier.notifyExchangeFailedEvent(event);

        //then save and removeNotifications should have been called
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(repository).save(captor.capture());
        Message message = captor.getValue();
        assertThat(message.getDuration()).isEqualTo(0);
        assertThat(message.getExchangeId()).isEqualTo("1");
        assertThat(message.getNotifications()).isEqualTo(notifications);
        assertThat(message.getTimestamp()).isNotEqualTo(0);
        assertThat(message.isFailed()).isTrue();
        verify(notifier).removeNotifications("1");
    }
}
