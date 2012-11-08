package com.ninja_squad.console.notifier;

import com.google.common.collect.Sets;
import com.ninja_squad.console.ExchangeStatistic;
import com.ninja_squad.console.StepStatistic;
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
        ConsoleEventNotifier notifier = spy(new ConsoleEventNotifier());
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
        ConsoleEventNotifier notifier = spy(new ConsoleEventNotifier());
        StepStatistic stepStatistic1 = new StepStatistic();
        stepStatistic1.setRouteId("1");
        stepStatistic1.setExchangeId("1");
        stepStatistic1.setStep(0);
        StepStatistic stepStatistic2 = new StepStatistic();
        stepStatistic2.setRouteId("1");
        stepStatistic2.setExchangeId("1");
        stepStatistic2.setStep(1);
        Collection<StepStatistic> stepStatistics = Sets.newHashSet(stepStatistic1, stepStatistic2);
        ConsoleRepository repository = mock(ConsoleRepositoryJongo.class);
        doReturn(stepStatistics).when(notifier).getStepStatistics("1");
        notifier.setRepository(repository);
        notifier.notifyExchangeCompletedEvent(event);

        //then save and removeStepStatistics should have been called
        ArgumentCaptor<ExchangeStatistic> captor = ArgumentCaptor.forClass(ExchangeStatistic.class);
        verify(repository).save(captor.capture());
        ExchangeStatistic exchangeStatistic = captor.getValue();
        assertThat(exchangeStatistic.getDuration()).isEqualTo(0);
        assertThat(exchangeStatistic.getExchangeId()).isEqualTo("1");
        assertThat(exchangeStatistic.getSteps()).isEqualTo(stepStatistics);
        assertThat(exchangeStatistic.getTimestamp()).isNotEqualTo(0);
        assertThat(exchangeStatistic.isFailed()).isFalse();

        verify(notifier).removeStepStatistics("1");
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
        StepStatistic stepStatistic1 = new StepStatistic();
        stepStatistic1.setRouteId("1");
        stepStatistic1.setExchangeId("1");
        stepStatistic1.setStep(1);
        StepStatistic stepStatistic2 = new StepStatistic();
        stepStatistic2.setRouteId("1");
        stepStatistic2.setExchangeId("1");
        stepStatistic2.setStep(1);
        Collection<StepStatistic> notifications = Sets.newHashSet(stepStatistic1, stepStatistic2);
        doReturn(notifications).when(notifier).getStepStatistics("1");
        ConsoleRepository repository = mock(ConsoleRepositoryJongo.class);
        notifier.setRepository(repository);
        notifier.notifyExchangeFailedEvent(event);

        //then save and removeStepStatistics should have been called
        ArgumentCaptor<ExchangeStatistic> captor = ArgumentCaptor.forClass(ExchangeStatistic.class);
        verify(repository).save(captor.capture());
        ExchangeStatistic exchangeStatistic = captor.getValue();
        assertThat(exchangeStatistic.getDuration()).isEqualTo(0);
        assertThat(exchangeStatistic.getExchangeId()).isEqualTo("1");
        assertThat(exchangeStatistic.getSteps()).isEqualTo(notifications);
        assertThat(exchangeStatistic.getTimestamp()).isNotEqualTo(0);
        assertThat(exchangeStatistic.isFailed()).isTrue();
        verify(notifier).removeStepStatistics("1");
    }
}
