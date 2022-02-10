package io.fizz.gateway.http.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.fizz.common.ConfigService;
import io.fizz.gateway.Constants;
import io.fizz.gateway.http.MockApplication;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.LoggerApi;
import io.swagger.client.model.SubmitLogRequest;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
public class LogControllerTest {

    private static int TEST_TIMEOUT = Constants.TEST_TIMEOUT;
    private static Vertx vertx;
    private static int port;

    private static final String APP_ID = "appA";

    static private Future<String> deployVertex() {
        Future<String> future = Future.future();
        vertx.deployVerticle(MockApplication.class.getName(), future);
        return future;
    }

    @BeforeAll
    static void setUp(VertxTestContext aContext) throws InterruptedException {
        vertx = Vertx.vertx();
        port = ConfigService.instance().getNumber("http.port").intValue();

        deployVertex()
                .setHandler(ar -> {
                    if (ar.succeeded()) {
                        aContext.completeNow();
                    } else {
                        aContext.failNow(ar.cause());
                    }
                });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @AfterAll
    static void tearDown(VertxTestContext aContext) {
        vertx.close(res -> aContext.completeNow());
    }

    @Test
    @DisplayName("it should publish and fetch logs from logger service")
    void validPublishAndFetchLogsTest() throws ApiException {

        final LoggerApi client = new LoggerApi(new ApiClient());

        for (int logId = 1; logId <= 55; logId++) {
            final SubmitLogRequest logItem = new SubmitLogRequest()
                    .logItem("Test log " + logId);

            String payload = new Gson().toJson(logItem);
            String signature = TestUtils.createSignature(payload, "secret");

            client.getApiClient().addDefaultHeader("Authorization", "HMAC-SHA256 " + signature);
            client.submitLog(APP_ID, "logChannel", logItem);
        }

        JsonObject payloadCall1 = new JsonObject();
        payloadCall1.addProperty("nonce", "call1");
        String signatureCall1 = TestUtils.createSignature(payloadCall1.toString(), "secret");
        client.getApiClient().addDefaultHeader("Authorization", "HMAC-SHA256 " + signatureCall1);

        List<String> logItemRes1 = client.fetchLogs(APP_ID, "logChannel", "call1", 3);
        Assertions.assertEquals(3, logItemRes1.size());
        Assertions.assertEquals("Test log 53", logItemRes1.get(0));
        Assertions.assertEquals("Test log 54", logItemRes1.get(1));
        Assertions.assertEquals("Test log 55", logItemRes1.get(2));

        JsonObject payloadCall2 = new JsonObject();
        payloadCall2.addProperty("nonce", "call2");
        String signatureCall2 = TestUtils.createSignature(payloadCall2.toString(), "secret");
        client.getApiClient().addDefaultHeader("Authorization", "HMAC-SHA256 " + signatureCall2);

        List<String> logItemRes2 = client.fetchLogs(APP_ID, "logChannel", "call2", null);
        Assertions.assertEquals(50, logItemRes2.size());
    }

    @Test
    @DisplayName("it should throw exception for invalid count")
    void invalidLogCountTest() throws Exception {

        final LoggerApi client = new LoggerApi(new ApiClient());

        JsonObject payload = new JsonObject();
        payload.addProperty("nonce", "call1");
        String signature = TestUtils.createSignature(payload.toString(), "secret");
        client.getApiClient().addDefaultHeader("Authorization", "HMAC-SHA256 " + signature);

        validateErrorResponse(() -> client.fetchLogs(APP_ID, "logChannel", "call1", 0), 400);
        validateErrorResponse(() -> client.fetchLogs(APP_ID, "logChannel", "call1", 51), 400);
    }

    void validateErrorResponse(ChatControllerTest.Executor aExecutor, int aCode) throws Exception {
        try {
            aExecutor.get();
        }
        catch (ApiException ex) {
            Assertions.assertEquals(ex.getCode(), aCode);
            return;
        }

        throw new Exception("no exception thrown");
    }
}
