package io.fizz.analytics.common.nlp;

import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalysisResults;
import io.fizz.analytics.domain.TextAnalysisResult;
import io.fizz.common.infastructure.circuitBreaker.CircuitBreakerConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class NLPCircuitBreakerTest {
    @Test
    @DisplayName("it should open circuit breaker on timeout")
    void circuitBreakerTimeoutTest() throws InterruptedException, ExecutionException {
        final CircuitBreakerConfig config = config("circuitBreakerTimeoutTest1");
        final MockTextAnalysisService service = new MockTextAnalysisService();
        final MockNLPCircuitBreaker breaker = new MockNLPCircuitBreaker(config, service);

        assert (Objects.nonNull(breaker.text("text").enqueue().get()));

        breaker.timeout(1500);
        assert (Objects.isNull(breaker.text("text").enqueue().get()));

        assert (Objects.isNull(breaker.text("text").enqueue().get()));

        breaker.timeout(0);
        Thread.sleep(2000);
        assert (Objects.isNull(breaker.text("text").enqueue().get()));
    }

    private CircuitBreakerConfig config(final String commandKey) {
        return new CircuitBreakerConfig()
                .commandKey(commandKey)
                .groupKey(NLPCircuitBreaker.class.getName())
                .requestTimeout(1000)
                .circuitBreakerTime(4000)
                .circuitBreakerThreshold(1);
    }

    private class MockNLPCircuitBreaker extends NLPCircuitBreaker {
        private int timeout;

        public MockNLPCircuitBreaker(CircuitBreakerConfig aConfig, AbstractTextAnalysisService aNLPService) {
            super(aConfig, aNLPService);
        }

        @Override
        protected TextAnalysisResult run() {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException ignored) { }
            return super.run();
        }

        public void timeout(int aTimeout) {
            this.timeout = aTimeout;
        }
    }
}
