package io.fizz.chat.application.provider;

import io.fizz.chat.application.domain.CleanSpeakProviderConfig;
import io.fizz.common.domain.ApplicationId;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Disabled
@ExtendWith(VertxExtension.class)
class CleanSpeakProviderTest {
    private static final int TEST_TIMEOUT = 10;

    private static Vertx vertx;
    private static WebClient client;

    @BeforeAll
    static void setUp(VertxTestContext aContext) throws Exception {
        vertx = Vertx.vertx();
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
    void baselineTest(final VertxTestContext aContext) throws InterruptedException {
        final CleanSpeakProviderConfig config = new CleanSpeakProviderConfig(
                "https://fizz-cleanspeak-api.inversoft.io",
                "_2cTiI99b8YRGQEKykzVhGaPhEu4TziXeUfki3Nphzk",
                "d72fa454-9686-4f9f-8f52-e133ce2109e6"
        );

        final CleanSpeakProvider provider = new CleanSpeakProvider(config);

        ApplicationId appId = null;
        try {
            appId = new ApplicationId("751326fc-305b-4aef-950a-074c9a21d461");
        } catch (Exception e) {
            Assertions.fail();
        }

        provider.filter(appId, client, "motherfucker420")
        .handle((aText, aError) -> {
            Assertions.assertNull(aError);
            Assertions.assertEquals(aText, "***************");

            aContext.completeNow();

            return CompletableFuture.completedFuture(null);
        });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }
}
