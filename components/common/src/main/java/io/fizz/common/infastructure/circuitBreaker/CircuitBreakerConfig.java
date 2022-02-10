package io.fizz.common.infastructure.circuitBreaker;

public class CircuitBreakerConfig {
    private static final int DEFAULT_REQUEST_TIMEOUT = 1000;
    private static final int DEFAULT_CIRCUIT_BREAKER_TIME = 60_000;
    private static final int DEFAULT_CIRCUIT_BREAKER_THRESHOLD = 1;
    private static final int DEFAULT_THREAD_POOL_SIZE = 10;

    private String groupKey;
    private String commandKey;
    private int requestTimeout = DEFAULT_REQUEST_TIMEOUT;
    private int circuitBreakerTime = DEFAULT_CIRCUIT_BREAKER_TIME;
    private int circuitBreakerThreshold = DEFAULT_CIRCUIT_BREAKER_THRESHOLD;
    private int threadPoolSize = DEFAULT_THREAD_POOL_SIZE;

    public String groupKey() {
        return groupKey;
    }

    public CircuitBreakerConfig groupKey(String groupKey) {
        this.groupKey = groupKey;
        return this;
    }

    public String commandKey() {
        return commandKey;
    }

    public CircuitBreakerConfig commandKey(String commandKey) {
        this.commandKey = commandKey;
        return this;
    }

    public int requestTimeout() {
        return requestTimeout;
    }

    public CircuitBreakerConfig requestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
        return this;
    }

    public int circuitBreakerTime() {
        return circuitBreakerTime;
    }

    public CircuitBreakerConfig circuitBreakerTime(int circuitBreakerTime) {
        this.circuitBreakerTime = circuitBreakerTime;
        return this;
    }

    public int circuitBreakerThreshold() {
        return circuitBreakerThreshold;
    }

    public CircuitBreakerConfig circuitBreakerThreshold(int circuitBreakerThreshold) {
        this.circuitBreakerThreshold = circuitBreakerThreshold;
        return this;
    }

    public int threadPoolSize() {
        return threadPoolSize;
    }

    public CircuitBreakerConfig threadPoolSize(int aThreadPoolSize) {
        this.threadPoolSize = aThreadPoolSize;
        return this;
    }
}
