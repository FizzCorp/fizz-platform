package io.fizz.chat.application.provider;

import io.fizz.chat.application.domain.AbstractProviderConfig;
import io.fizz.chat.application.domain.AzureProviderConfig;
import io.fizz.common.domain.ApplicationId;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

@Disabled
@ExtendWith(VertxExtension.class)
public class AzureProviderTest {
    private static final int TEST_TIMEOUT = 10;

    private static Vertx vertx;
    private static ApplicationId appId;
    private static WebClient client;
    private static AbstractProvider provider;

    @BeforeAll
    static void setUp(VertxTestContext aContext) throws Exception {
        vertx = Vertx.vertx();
        String baseUrl = "https://eastus.api.cognitive.microsoft.com/contentmoderator";
        String secret = "e8e6bc05541643ef98da02f2b7dac045";
        AbstractProviderConfig providerConfig = new AzureProviderConfig(baseUrl, secret);

        appId = new ApplicationId("appA");
        provider = new AzureProvider(providerConfig);
        client = WebClient.create(vertx);

        aContext.completeNow();
    }

    @AfterAll
    static void tearDown(VertxTestContext aContext) {
        vertx.close(res -> {
            client.close();
            aContext.completeNow();
        });
    }

    @Test
    void testAzureServiceAbusive(VertxTestContext aContext) throws InterruptedException {

        provider.filter(appId, client, "it is a holy shit @$$")
                .thenApply(result -> {
                    Assertions.assertEquals("it is a holy *** ***", result);
                    aContext.completeNow();
                   return null;
                });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    void testAzureServicePII(VertxTestContext aContext) throws InterruptedException {
        provider.filter(appId, client, "here is my email zeeshan.safder@fizz.io")
                .thenApply(result -> {
                    Assertions.assertEquals("here is my email ***", result);
                    aContext.completeNow();
                    return null;
                });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    void testAzureServiceAbusivePII(VertxTestContext aContext) throws InterruptedException {
        provider.filter(appId, client,"it is a holy shit. here is my email zeeshan.safder@fizz.io")
                .thenApply(result -> {
                    Assertions.assertEquals("it is a holy ***. here is my email ***", result);
                    aContext.completeNow();
                    return null;
                });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    void testAzureServiceNormal(VertxTestContext aContext) throws InterruptedException {
        provider.filter(appId, client,"hello")
                .thenApply(result -> {
                    Assertions.assertEquals("hello", result);
                    aContext.completeNow();
                    return null;
                });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }
}
