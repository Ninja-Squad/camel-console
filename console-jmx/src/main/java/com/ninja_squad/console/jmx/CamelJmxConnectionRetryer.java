package com.ninja_squad.console.jmx;

import com.google.common.base.Predicates;
import com.ninja_squad.console.State;
import com.ninja_squad.core.retry.Retryer;
import com.ninja_squad.core.retry.RetryerBuilder;
import com.ninja_squad.core.retry.WaitStrategies;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CamelJmxConnectionRetryer {

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    // every 500ms
    private Retryer.RetryerCallable wrapper;

    public CamelJmxConnectionRetryer(final CamelJmxConnector connector) {

        Callable<State> callable = new Callable<State>() {
            @Override
            public State call() throws Exception {
                return connector.connect();
            }
        };

        wrapper = RetryerBuilder.<State>newBuilder()
                .withWaitStrategy(WaitStrategies.fixedWait(500L, TimeUnit.MILLISECONDS))
                .retryIfResult(Predicates.<State>equalTo(State.Stopped))
                .build().wrap(callable);
    }

    public void start() {
        executorService.submit(wrapper);
    }
}
