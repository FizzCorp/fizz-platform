package io.fizz.chat.infrastructure.translation;

import io.fizz.chat.application.services.TranslationService;
import io.fizz.chatcommon.domain.LanguageCode;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Disabled
@ExtendWith(VertxExtension.class)
class BingTranslationServiceTest {
    private static final int TEST_TIMEOUT = 10;

    @Test
    void translationValidFromVTest(Vertx aVertx, VertxTestContext aContext) throws InterruptedException {
        final TranslationService service = new TranslationService(new BingTranslationClient(aVertx));

        service.translate("chat alliance", LanguageCode.ENGLISH, new LanguageCode[]{ LanguageCode.ENGLISH, LanguageCode.FRENCH })
        .handle((aTranslations, aError) -> {
            Assertions.assertEquals(aTranslations.size(), 2);
            Assertions.assertEquals(aTranslations.get(LanguageCode.ENGLISH).compareToIgnoreCase("chat alliance"), 0);
            Assertions.assertEquals(aTranslations.get(LanguageCode.FRENCH).compareToIgnoreCase("alliance de chat"), 0);

            aContext.completeNow();
            return CompletableFuture.completedFuture(null);
        });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    void translationNullFromTest(Vertx aVertx, VertxTestContext aContext) throws InterruptedException {
        final TranslationService service = new TranslationService(new BingTranslationClient(aVertx));

        service.translate("chat alliance", null, new LanguageCode[]{ LanguageCode.ENGLISH, LanguageCode.FRENCH })
                .handle((aTranslations, aError) -> {
                    Assertions.assertEquals(aTranslations.size(), 2);
                    Assertions.assertEquals(aTranslations.get(LanguageCode.ENGLISH).compareToIgnoreCase("cat alliance"), 0);
                    Assertions.assertEquals(aTranslations.get(LanguageCode.FRENCH).compareToIgnoreCase("chat alliance"), 0);

                    aContext.completeNow();
                    return CompletableFuture.completedFuture(null);
                });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }
}
