package io.fizz.chat.application.impl;

import io.fizz.chat.application.domain.AzureProviderConfig;
import io.fizz.chat.application.provider.AzureProvider;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
public class ProviderFactoryTest {

    private static final int TEST_TIMEOUT = 10;

    private static AzureProviderConfig azureProviderConfig;
    private static ProviderFactory providerFactory;

    @BeforeAll
    static void setUp() {
        azureProviderConfig = new AzureProviderConfig("http://test.azure.com/moderate", "azureSecret");
        providerFactory = new ProviderFactory();
    }

    @Test
    @DisplayName("it should create provider object with valid azure provider config")
    void testProviderFactoryAzureConfig(VertxTestContext aContext) throws InterruptedException {
        try {
            providerFactory.create(azureProviderConfig).thenApply(provider -> {
                Assertions.assertNotNull(provider);
                Assertions.assertTrue(provider instanceof AzureProvider);

                aContext.completeNow();
                return null;
            });
        } catch (Exception ex) {
            aContext.failNow(ex);
        }

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("it should not create provider object with null provider config")
    void testProviderFactoryNullConfig() {
        try {
            providerFactory.create(null);
            Assertions.fail("Provider should not be created!");
        } catch (Exception ex) {
            Assertions.assertEquals("invalid_provider_config", ex.getMessage());
        }
    }
}
