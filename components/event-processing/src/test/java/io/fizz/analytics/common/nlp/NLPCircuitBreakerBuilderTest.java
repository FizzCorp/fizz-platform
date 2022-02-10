package io.fizz.analytics.common.nlp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class NLPCircuitBreakerBuilderTest {
    @Test
    @DisplayName("it should execute a command successfully")
    void circuitBreakerBuilderCreateTest() throws ExecutionException, InterruptedException {
        final MockTextAnalysisService service = new MockTextAnalysisService();
        NLPCircuitBreaker breaker = new  NLPCircuitBreakerBuilder(service).build();

        assert (Objects.nonNull(breaker.text("text").enqueue().get()));
    }
}
