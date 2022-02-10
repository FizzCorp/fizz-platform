package io.fizz.analytics.common.nlp;

import io.fizz.common.infastructure.circuitBreaker.CircuitBreakerConfig;

public class NLPCircuitBreakerBuilder {
    private static final int REQUEST_TIMEOUT = 2000;
    private static final int CIRCUIT_BREAKER_TIME = 300_000;
    private static final int CIRCUIT_BREAKER_THRESHOLD = 1;

    private final AbstractTextAnalysisService nlpService;

    public NLPCircuitBreakerBuilder(final AbstractTextAnalysisService aNLPService) {
        nlpService = aNLPService;
    }

    public NLPCircuitBreaker build() {
        CircuitBreakerConfig config = new CircuitBreakerConfig()
                .commandKey(getClass().getName())
                .groupKey(getClass().getName())
                .requestTimeout(REQUEST_TIMEOUT)
                .circuitBreakerTime(CIRCUIT_BREAKER_TIME)
                .circuitBreakerThreshold(CIRCUIT_BREAKER_THRESHOLD);

        return new NLPCircuitBreaker(config, nlpService);
    }
}
