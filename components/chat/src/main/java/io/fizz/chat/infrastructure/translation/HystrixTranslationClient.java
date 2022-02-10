package io.fizz.chat.infrastructure.translation;

import io.fizz.chat.application.channel.AbstractTranslationClient;
import io.fizz.chatcommon.domain.LanguageCode;
import io.fizz.common.infastructure.circuitBreaker.AbstractCircuitBreaker;
import io.fizz.common.infastructure.circuitBreaker.CircuitBreakerConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class HystrixTranslationClient
        extends AbstractCircuitBreaker<CompletableFuture<Map<LanguageCode,String>>>
        implements AbstractTranslationClient {

    private static final int REQUEST_TIMEOUT = 2000;
    private static final int CIRCUIT_BREAKER_TIME = 300_000;
    private static final int CIRCUIT_BREAKER_THRESHOLD = 1;
    private static final String COMMEND_KEY = "HystrixTranslationClient";
    private static final String GROUP_KEY = "HystrixTranslationClient";

    private String text;
    private LanguageCode from;
    private LanguageCode[] to;

    final private AbstractTranslationClient client;

    public HystrixTranslationClient(final AbstractTranslationClient aClient) {
        this(aClient, new CircuitBreakerConfig()
                .commandKey(COMMEND_KEY)
                .groupKey(GROUP_KEY)
                .requestTimeout(REQUEST_TIMEOUT)
                .circuitBreakerTime(CIRCUIT_BREAKER_TIME)
                .circuitBreakerThreshold(CIRCUIT_BREAKER_THRESHOLD));
    }

    public HystrixTranslationClient(final AbstractTranslationClient aClient,
                                    final CircuitBreakerConfig aConfig) {
        super(aConfig);
        this.client = aClient;
    }

    @Override
    public CompletableFuture<Map<LanguageCode, String>> translate(String aText, LanguageCode aFrom, LanguageCode[] aTo) {
        text = aText;
        from = aFrom;
        to = aTo;
        return this.execute();
    }

    @Override
    protected CompletableFuture<Map<LanguageCode, String>> run() {
        return client.translate(text, from, to)
                .handle((res, error) -> {
                    if (Objects.nonNull(error)) {
                        markException();
                    }
                    return Objects.isNull(res) ? new HashMap<>() : res;
                });
    }

    @Override
    protected CompletableFuture<Map<LanguageCode, String>> fallback() {
        return CompletableFuture.completedFuture(new HashMap<LanguageCode, String>() {{ put(from, text); }});
    }
}
