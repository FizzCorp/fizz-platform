package io.fizz.chat.infrastructure.translation;

import io.fizz.chat.application.channel.AbstractTranslationClient;
import io.fizz.chatcommon.domain.LanguageCode;
import io.fizz.common.infastructure.circuitBreaker.CircuitBreakerConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MockTranslationClient extends HystrixTranslationClient {
    private int timeout;
    private boolean markError;

    public MockTranslationClient(AbstractTranslationClient aClient, CircuitBreakerConfig aConfig) {
        super(aClient, aConfig);
    }

    @Override
    protected CompletableFuture<Map<LanguageCode, String>> run() {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException ignored) { }

        CompletableFuture<Map<LanguageCode, String>> future = new CompletableFuture<>();
        if (markError) {
            future.completeExceptionally(new Exception());
        } else {
            future.complete(new HashMap<LanguageCode, String>() {{
                put(LanguageCode.ENGLISH, "translated");
            }});
        }
        return future;
    }

    @Override
    protected void markException() {
        super.markException();
    }

    void markError(boolean aMarkError) {
        this.markError = aMarkError;
    }

    public void timeout(int aTimeout) {
        this.timeout = aTimeout;
    }
}
