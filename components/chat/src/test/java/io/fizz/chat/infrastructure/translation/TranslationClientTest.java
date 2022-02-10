package io.fizz.chat.infrastructure.translation;

import io.fizz.chatcommon.domain.LanguageCode;
import io.fizz.common.infastructure.circuitBreaker.CircuitBreakerConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class TranslationClientTest {
    @Test
    @DisplayName("it should open circuit breaker on timeout")
    void TranslateTimeoutTest() throws InterruptedException {
        final CircuitBreakerConfig config = config("ContentModerationCircuitBreakerTest1");
        MockTranslationClient client = new MockTranslationClient(null, config);

        client.translate("text", LanguageCode.ENGLISH, new LanguageCode[]{LanguageCode.ENGLISH})
        .handle((s, error) -> {
            assert (Objects.isNull(error));
            assert (s.get(LanguageCode.ENGLISH).equals("translated"));
            return CompletableFuture.completedFuture(null);
        });

        client.timeout(1500);
        client.translate("text", LanguageCode.ENGLISH, new LanguageCode[]{LanguageCode.ENGLISH})
        .handle((s, error) -> {
            assert (Objects.isNull(error));
            assert (s.size() == 0);
            client.markException();
            return CompletableFuture.completedFuture(null);
        });

        client.translate("text", LanguageCode.ENGLISH, new LanguageCode[]{LanguageCode.ENGLISH})
        .handle((s, error) -> {
            assert (Objects.isNull(error));
            assert (s.size() == 0);
            client.markException();
            return CompletableFuture.completedFuture(null);
        });

        client.timeout(0);
        Thread.sleep(5000);
        client.translate("text", LanguageCode.ENGLISH, new LanguageCode[]{LanguageCode.ENGLISH})
        .handle((s, error) -> {
            assert (Objects.isNull(error));
            assert (s.get(LanguageCode.ENGLISH).equals("translated"));
            return CompletableFuture.completedFuture(null);
        });
    }

    @Test
    @DisplayName("it should open circuit breaker on error response")
    void translateErrorTest() throws InterruptedException {
        final CircuitBreakerConfig config = config("ContentModerationCircuitBreakerTest1");
        MockTranslationClient client = new MockTranslationClient(null, config);

        client.translate("text", LanguageCode.ENGLISH, new LanguageCode[]{LanguageCode.ENGLISH})
        .handle((s, error) -> {
            assert (Objects.isNull(error));
            assert (s.get(LanguageCode.ENGLISH).equals("translated"));
            return CompletableFuture.completedFuture(null);
        });

        client.markError(true);
        client.translate("text", LanguageCode.ENGLISH, new LanguageCode[]{LanguageCode.ENGLISH})
        .handle((s, error) -> {
            assert (Objects.isNull(error));
            assert (s.size() == 0);
            client.markException();
            return CompletableFuture.completedFuture(null);
        });

        client.markError(false);
        client.translate("text", LanguageCode.ENGLISH, new LanguageCode[]{LanguageCode.ENGLISH})
        .handle((s, error) -> {
            assert (Objects.isNull(error));
            assert (s.size() == 0);
            return CompletableFuture.completedFuture(null);
        });

        Thread.sleep(3000);
        client.translate("text", LanguageCode.ENGLISH, new LanguageCode[]{LanguageCode.ENGLISH})
        .handle((s, error) -> {
            assert (Objects.isNull(error));
            assert (s.size() == 0);
            return CompletableFuture.completedFuture(null);
        });

        Thread.sleep(2000);
        client.translate("text", LanguageCode.ENGLISH, new LanguageCode[]{LanguageCode.ENGLISH})
        .handle((s, error) -> {
            assert (Objects.isNull(error));
            assert (s.get(LanguageCode.ENGLISH).equals("translated"));
            return CompletableFuture.completedFuture(null);
        });
    }

    private CircuitBreakerConfig config(final String commandKey) {
        return new CircuitBreakerConfig()
                .commandKey(commandKey)
                .groupKey(HystrixTranslationClient.class.getName())
                .requestTimeout(1000)
                .circuitBreakerTime(4000)
                .circuitBreakerThreshold(1);
    }
}
