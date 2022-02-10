package io.fizz.chat.moderation.infrastructure.service;

import io.fizz.chat.application.provider.AbstractProvider;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.infastructure.circuitBreaker.AbstractCircuitBreaker;
import io.fizz.common.infastructure.circuitBreaker.CircuitBreakerConfig;
import io.vertx.ext.web.client.WebClient;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class HystrixContentModerationClient
        extends AbstractCircuitBreaker<CompletableFuture<String>> implements AbstractProvider {
    private static final int REQUEST_TIMEOUT = 2000;
    private static final int CIRCUIT_BREAKER_TIME = 300_000;
    private static final int CIRCUIT_BREAKER_THRESHOLD = 1;
    private static final String COMMAND_KEY = "HystrixContentModerationClient";
    private static final String GROUP_KEY = "HystrixContentModerationClient";

    private final AbstractProvider provider;

    private ApplicationId appId;
    private WebClient webClient;
    private String text;

    public HystrixContentModerationClient(final AbstractProvider aProvider) {
        this(aProvider,
                new CircuitBreakerConfig()
                .commandKey(COMMAND_KEY)
                .groupKey(GROUP_KEY)
                .requestTimeout(REQUEST_TIMEOUT)
                .circuitBreakerTime(CIRCUIT_BREAKER_TIME)
                .circuitBreakerThreshold(CIRCUIT_BREAKER_THRESHOLD));

    }

    public HystrixContentModerationClient(final AbstractProvider aProvider,
                                          final CircuitBreakerConfig aConfig) {
        super(aConfig);
        this.provider = aProvider;
    }

    @Override
    public CompletableFuture<String> filter(final ApplicationId aAppId,
                                            final WebClient aClient,
                                            final String aText) {
        text = aText;
        appId = aAppId;
        webClient = aClient;

        return this.execute();
    }

    @Override
    protected CompletableFuture<String> run() {
        if (Objects.isNull(text)) {
            throw new IllegalArgumentException("invalid text.");
        }

        return provider.filter(appId, webClient, text)
                .handle((res, error) -> {
                    if (Objects.nonNull(error)) {
                        markException();
                    }
                    return Objects.isNull(res) ? text : res;
                });
    }

    @Override
    protected CompletableFuture<String> fallback() {
        return CompletableFuture.completedFuture(text);
    }
}
