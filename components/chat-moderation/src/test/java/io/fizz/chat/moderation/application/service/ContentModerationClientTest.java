package io.fizz.chat.moderation.application.service;

import io.fizz.chat.application.provider.AbstractProvider;
import io.fizz.chat.application.provider.MockAbstractProvider;
import io.fizz.chat.moderation.infrastructure.service.HystrixContentModerationClient;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.infastructure.circuitBreaker.CircuitBreakerConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ContentModerationClientTest {
    @Test
    @DisplayName("it should open circuit breaker on timeout")
    void circuitBreakerTimeoutTest() throws InterruptedException {

        ApplicationId appId = null;
        try {
            appId = new ApplicationId("appA");
        } catch (Exception e) {
            Assertions.fail();
        }

        final CircuitBreakerConfig config = config("ContentModerationCircuitBreakerTest1");
        final MockAbstractProvider provider = new MockAbstractProvider();
        final MockContentModerationClient client = new MockContentModerationClient(provider, config);

        client.filter(appId, null, "text")
        .handle((s, error) -> {
            assert (Objects.isNull(error));
            assert (s.equals("success"));
            return CompletableFuture.completedFuture(null);
        });

        client.timeout(1500);
        client.filter(appId, null, "text")
        .handle((s, error) -> {
            assert (Objects.isNull(error));
            assert (s.equals("text"));
            client.markException();
            return CompletableFuture.completedFuture(null);
        });

        client.filter(appId, null, "text")
        .handle((s, error) -> {
            assert (Objects.isNull(error));
            assert (s.equals("text"));
            client.markException();
            return CompletableFuture.completedFuture(null);
        });

        client.timeout(0);
        Thread.sleep(2000);
        client.filter(appId, null, "text")
        .handle((s, error) -> {
            assert (Objects.isNull(error));
            assert (s.equals("success"));
            return CompletableFuture.completedFuture(null);
        });
    }

    @Test
    @DisplayName("it should open circuit breaker on error response")
    void circuitBreakerErrorTest() throws InterruptedException {

        ApplicationId appId = null;
        try {
            appId = new ApplicationId("appA");
        } catch (Exception e) {
            Assertions.fail();
        }

        final CircuitBreakerConfig config = config("ContentModerationCircuitBreakerTest2");
        final MockAbstractProvider provider = new MockAbstractProvider();
        final MockContentModerationClient client = new MockContentModerationClient(provider, config);

        client.filter(appId, null, "text")
        .handle((s, error) -> {
            assert (Objects.isNull(error));
            assert (s.equals("success"));
            return CompletableFuture.completedFuture(null);
        });

        provider.markError(true);
        client.filter(appId, null, "text")
        .handle((s, error) -> {
            assert (Objects.isNull(error));
            assert (s.equals("text"));
            client.markException();
            return CompletableFuture.completedFuture(null);
        });

        provider.markError(false);
        client.filter(appId, null, "text")
        .handle((s, error) -> {
            assert (Objects.isNull(error));
            assert (s.equals("text"));
            return CompletableFuture.completedFuture(null);
        });

        Thread.sleep(3000);
        client.filter(appId, null, "text")
        .handle((s, error) -> {
            assert (Objects.isNull(error));
            assert (s.equals("text"));
            return CompletableFuture.completedFuture(null);
        });

        Thread.sleep(2000);
        client.filter(appId, null, "text")
        .handle((s, error) -> {
            assert (Objects.isNull(error));
            assert (s.equals("success"));
            return CompletableFuture.completedFuture(null);
        });
    }

    private CircuitBreakerConfig config(final String commandKey) {
        return new CircuitBreakerConfig()
                .commandKey(commandKey)
                .groupKey(ContentModerationClientTest.class.getName())
                .requestTimeout(1000)
                .circuitBreakerTime(4000)
                .circuitBreakerThreshold(1);
    }

    private class MockContentModerationClient extends HystrixContentModerationClient {
        private int timeout;

        public MockContentModerationClient(AbstractProvider aProvider) {
            super(aProvider);
        }

        public MockContentModerationClient(AbstractProvider aProvider, CircuitBreakerConfig aConfig) {
            super(aProvider, aConfig);
        }

        @Override
        protected CompletableFuture<String> run() {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException ignored) { }
            return super.run();
        }

        public void timeout(int aTimeout) {
            this.timeout = aTimeout;
        }

        @Override
        protected void markException() {
            super.markException();
        }
    }
}
