package io.fizz.common.infastructure.circuitBreaker;

import com.netflix.hystrix.*;
import rx.Observable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractCircuitBreaker<R> {
    private boolean exceptionOccurred;
    private final CircuitBreakerConfig config;

    public AbstractCircuitBreaker (final CircuitBreakerConfig aConfig) {
        if (Objects.isNull(aConfig)) {
            throw new IllegalArgumentException("invalid circuit breaker config.");
        }
        if (Objects.isNull(aConfig.commandKey())) {
            throw new IllegalArgumentException("invalid command key.");
        }
        if (Objects.isNull(aConfig.groupKey())) {
            throw new IllegalArgumentException("invalid group key.");
        }
        config = aConfig;
    }

    protected abstract R run();
    protected abstract R fallback();

    protected void markException() {
        exceptionOccurred = true;
    }

    public R execute() {
        return buildHystrixCircuitBreaker().execute();
    }

    public CompletableFuture<R> enqueue() {
        return buildHystrixCircuitBreaker().enqueue();
    }

    private HystrixCircuitBreaker<R> buildHystrixCircuitBreaker() {
        HystrixCommand.Setter hystrixConfig = HystrixCommand
                .Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey(config.groupKey()))
                .andCommandKey(HystrixCommandKey.Factory.asKey(config.commandKey()));


        HystrixCommandProperties.Setter properties = HystrixCommandProperties.Setter();
        properties.withExecutionTimeoutInMilliseconds(config.requestTimeout());
        properties.withCircuitBreakerSleepWindowInMilliseconds(config.circuitBreakerTime());
        properties.withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD);
        properties.withCircuitBreakerEnabled(true);
        properties.withCircuitBreakerRequestVolumeThreshold(config.circuitBreakerThreshold());

        HystrixThreadPoolProperties.Setter threadProperties = HystrixThreadPoolProperties
                .Setter().withCoreSize(config.threadPoolSize());

        hystrixConfig
        .andCommandPropertiesDefaults(properties)
        .andThreadPoolPropertiesDefaults(threadProperties);

        return new HystrixCircuitBreaker<R>(hystrixConfig, new HystrixCircuitBreakerListener<R>() {
            @Override
            public R run() {
                if (consumeException()) {
                    throw new RuntimeException();
                }
                return AbstractCircuitBreaker.this.run();
            }

            @Override
            public R fallback() {
                return AbstractCircuitBreaker.this.fallback();
            }
        });
    }

    private boolean consumeException() {
        boolean ex = exceptionOccurred;
        exceptionOccurred = false;
        return ex;
    }

    private interface HystrixCircuitBreakerListener<R> {
        R run();
        R fallback();
    }

    private static class HystrixCircuitBreaker<R> extends HystrixCommand<R> {
        private final HystrixCircuitBreakerListener<R> listener;

        HystrixCircuitBreaker(final Setter aSetter, final HystrixCircuitBreakerListener<R> aListener) {
            super(aSetter);
            listener = aListener;
        }

        public <T> CompletableFuture<T> fromSingleObservable(Observable<T> observable) {
            CompletableFuture<T> future = new CompletableFuture<>();
            observable.single().subscribe(future::complete, future::completeExceptionally);
            return future;
        }

        public CompletableFuture<R> enqueue() {
            final Observable<R> observable = super.observe();
            return fromSingleObservable(observable);
        }

        @Override
        public R execute() {
            return super.execute();
        }

        @Override
        protected R run() {
            return listener.run();
        }

        @Override
        protected R getFallback() {
            return listener.fallback();
        }
    }
}
