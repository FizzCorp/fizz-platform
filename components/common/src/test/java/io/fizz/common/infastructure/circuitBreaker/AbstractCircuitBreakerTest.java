package io.fizz.common.infastructure.circuitBreaker;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class AbstractCircuitBreakerTest {
    @Test
    @DisplayName("it should run circuit breaker with success")
    void circuitBreakerCreationTest() {
        final CircuitBreakerConfig config = config("CircuitBreakerTest1");
        AbstractCircuitBreaker<String> circuitBreaker = new TestCircuitBreaker(config, 0);

        assert (circuitBreaker.execute().equals("run"));
    }

    @Test
    @DisplayName("it should run circuit breaker with success")
    void circuitBreakerAsyncCreationTest() throws ExecutionException, InterruptedException {
        final CircuitBreakerConfig config = config("CircuitBreakerTest2");
        AbstractCircuitBreaker<String> circuitBreaker = new TestCircuitBreaker(config, 0);

        assert (circuitBreaker.enqueue().get().equals("run"));
    }

    @Test
    @DisplayName("it should timeout the command")
    void circuitBreakerTimeoutTest() {
        final CircuitBreakerConfig config = config("CircuitBreakerTest3");
        AbstractCircuitBreaker<String> circuitBreaker = new TestCircuitBreaker(config, 1500);

        assert (circuitBreaker.execute().equals("fallback"));
    }

    @Test
    @DisplayName("it should timeout the command")
    void circuitBreakerTimeoutCircuitOpenTest() throws InterruptedException {
        final String commandKey = "CircuitBreakerTest4";

        final CircuitBreakerConfig config = config(commandKey);
        TestCircuitBreaker circuitBreaker = new TestCircuitBreaker(config, 1500);
        assert (circuitBreaker.execute().equals("fallback"));

        assert (circuitBreaker.execute().equals("fallback"));

        assert (circuitBreaker.execute().equals("fallback"));

        assert (circuitBreaker.execute().equals("fallback"));

        Thread.sleep(5000);

        circuitBreaker.timeout(0);
        assert (circuitBreaker.execute().equals("run"));
    }

    @Test
    @DisplayName("it should timeout the command")
    void circuitBreakerErrorCircuitOpenTest() throws InterruptedException {
        final String commandKey = "CircuitBreakerTest5";

        final CircuitBreakerConfig config = config(commandKey);
        TestCircuitBreaker circuitBreaker = new TestCircuitBreaker(config, 0);
        assert (circuitBreaker.execute().equals("run"));

        circuitBreaker.markException(true);
        assert (circuitBreaker.execute().equals("fallback"));

        Thread.sleep(500);
        assert (circuitBreaker.execute().equals("fallback"));

        Thread.sleep(500);
        assert (circuitBreaker.execute().equals("fallback"));

        Thread.sleep(1000);
        assert (circuitBreaker.execute().equals("fallback"));

        Thread.sleep(2000);
        assert (circuitBreaker.execute().equals("fallback"));

        Thread.sleep(2000);
        assert (circuitBreaker.execute().equals("run"));
    }

    private CircuitBreakerConfig config(final String commandKey) {
        return new CircuitBreakerConfig()
                .commandKey(commandKey)
                .groupKey(AbstractCircuitBreakerTest.class.getName())
                .requestTimeout(1000)
                .circuitBreakerTime(4000)
                .circuitBreakerThreshold(1);
    }

    private static class TestCircuitBreaker extends AbstractCircuitBreaker<String> {
        private int timeout;

        private TestCircuitBreaker(final CircuitBreakerConfig aConfig, final int aTimeout) {
            super(aConfig);
            timeout = aTimeout;
        }

        private void timeout(final int aTimeout) {
            timeout = aTimeout;
        }

        private void markException(boolean aCreateException) {
            markException();
        }

        @Override
        protected String run() {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException ignored) { }
            return "run";
        }

        @Override
        protected String fallback() {
            return "fallback";
        }
    }
}
