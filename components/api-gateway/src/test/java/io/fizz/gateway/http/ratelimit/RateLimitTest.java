package io.fizz.gateway.http.ratelimit;

import com.google.gson.Gson;
import io.fizz.common.Utils;
import io.fizz.gateway.Constants;
import io.fizz.gateway.http.MockApplication;
import io.fizz.gateway.http.controllers.TestUtils;
import io.fizz.gateway.http.verticles.MockHttpVerticle;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.AuthApi;
import io.swagger.client.api.ChannelApi;
import io.swagger.client.model.*;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@ExtendWith(VertxExtension.class)
public class RateLimitTest {
    private static Vertx vertx;
    private static String sessionToken;
    private static final String APP_ID = "appA";
    private static final String USER_ID = "user1";

    static private Future<String> deployVertex() {
        Future<String> future = Future.future();
        vertx.deployVerticle(MockApplication.class.getName(), future);
        return future;
    }

    @BeforeAll
    static void setUp(VertxTestContext aContext) throws InterruptedException {
        vertx = Vertx.vertx();

        deployVertex()
                .setHandler(ar -> {
                    if (ar.succeeded()) {
                        aContext.completeNow();
                    } else {
                        aContext.failNow(ar.cause());
                    }
                });

        Assertions.assertTrue(aContext.awaitCompletion(Constants.TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @AfterAll
    static void tearDown(VertxTestContext aContext) {
        vertx.close(res -> aContext.completeNow());
    }

    @BeforeEach
    void init() throws ApiException {
        if (Objects.nonNull(sessionToken)) {
            return;
        }
        final AuthApi api = new AuthApi(new ApiClient());
        final SessionAuthRequest request = new SessionAuthRequest();

        request.setUserId(USER_ID);
        request.setAppId(APP_ID);
        request.setLocale(LanguageCode.EN);
        api.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(request), "secret")
        );

        sessionToken = api.createSession(request).getToken();
    }

    @Disabled
    @Test
    @DisplayName("it should not allow user to publish messages over rate limit")
    void channelRateLimitPublishMessageTest() throws Exception {
        final String channelId = UUID.randomUUID().toString();
        final ChannelApi api = new ChannelApi(new ApiClient());
        api.getApiClient().setApiKey(sessionToken);

        ChannelMessageModel message = buildMessage(USER_ID,"Message", "data", true);
        api.publishChannelMessage(channelId, message);
        // sleep till next second starts
        long remainingMillis = Utils.now()%1000;
        Thread.sleep(1000+remainingMillis);

        for (int ii = 0; ii<MockHttpVerticle.CHANNEL_RATE_LIMIT_MAX_MOCK; ii++) {
            message = buildMessage(USER_ID,"Message "+(ii+1), "data", true);
            api.publishChannelMessage(channelId, message);
        }

        final ChannelMessageModel message1 = buildMessage(USER_ID,"Message", "data", true);
        validateErrorResponse(() -> api.publishChannelMessage(channelId, message1), 429);

        // wait a second for key too be updated [second interval]
        Thread.sleep(1000);

        // should be able to publish again
        api.publishChannelMessage(channelId, message1);
    }

    @Disabled
    @Test
    @DisplayName("it should not allow user to update messages over rate limit")
    void channelRateLimitUpdateMessageTest() throws Exception {
        final String channelId = UUID.randomUUID().toString();
        final ChannelApi api = new ChannelApi(new ApiClient());
        api.getApiClient().setApiKey(sessionToken);

        ChannelMessageModel message = buildMessage(USER_ID,"Message", "data", true);
        api.publishChannelMessage(channelId, message);
        List<ChannelMessage> messages = api.queryChannelMessages(channelId, 1, null, null);

        // sleep till next second starts
        long remainingMillis = Utils.now()%1000;
        Thread.sleep(1000+remainingMillis);

        for (int ii=0; ii<MockHttpVerticle.CHANNEL_RATE_LIMIT_MAX_MOCK; ii++) {
            final UpdateChannelMessageRequest updatedMessageRequest = buildUpdateMessageRequest(USER_ID,
                    "message Updated " +(ii+1), "data " + (ii+1));
            api.updateChannelMessage(channelId, messages.get(0).getId().toString(), updatedMessageRequest);
        }

        final UpdateChannelMessageRequest updatedMessageRequest = buildUpdateMessageRequest(USER_ID,
                "message Updated", "data");
        validateErrorResponse(() -> api.updateChannelMessage(channelId, messages.get(0).getId().toString(), updatedMessageRequest), 429);

        // wait a second for key too be updated [second interval]
        Thread.sleep(1000);

        // should be able to update again
        api.updateChannelMessage(channelId, messages.get(0).getId().toString(), updatedMessageRequest);
    }

    @Disabled
    @Test
    @DisplayName("it should not allow user to delete messages over rate limit")
    void channelRateLimitDeleteMessageTest() throws Exception {
        final String channelId = UUID.randomUUID().toString();
        final ChannelApi api = new ChannelApi(new ApiClient());
        api.getApiClient().setApiKey(sessionToken);

        for (int ii=0; ii<MockHttpVerticle.CHANNEL_RATE_LIMIT_MAX_MOCK; ii++) {
            ChannelMessageModel message = buildMessage(USER_ID,"Message "+(ii+1), "data", true);
            api.publishChannelMessage(channelId, message);
        }
        Thread.sleep(1000);
        ChannelMessageModel message = buildMessage(USER_ID,"Message 6", "data", true);
        api.publishChannelMessage(channelId, message);

        List<ChannelMessage> messages = api.queryChannelMessages(channelId, MockHttpVerticle.CHANNEL_RATE_LIMIT_MAX_MOCK+1, null, null);

        // sleep till next second starts
        long remainingMillis = Utils.now()%1000;
        Thread.sleep(1000+remainingMillis);

        for (int ii=0; ii<MockHttpVerticle.CHANNEL_RATE_LIMIT_MAX_MOCK; ii++) {
            api.deleteChannelMessage(channelId, messages.get(ii).getId().toString());
        }
        validateErrorResponse(() -> api.deleteChannelMessage(channelId, messages.get(MockHttpVerticle.CHANNEL_RATE_LIMIT_MAX_MOCK).getId().toString()), 429);

        // wait a second for key too be updated [second interval]
        Thread.sleep(1000);

        // should be able to delete again
        api.deleteChannelMessage(channelId, messages.get(MockHttpVerticle.CHANNEL_RATE_LIMIT_MAX_MOCK).getId().toString());
    }

    private ChannelMessageModel buildMessage(String aNick, String aBody, String aData, boolean aPersist) {
        return new ChannelMessageModel()
                .nick(aNick)
                .body(aBody)
                .data(aData)
                .persist(aPersist);
    }

    private UpdateChannelMessageRequest buildUpdateMessageRequest(String aNick, String aBody, String aData) {
        return new UpdateChannelMessageRequest()
                .nick(aNick)
                .body(aBody)
                .data(aData);
    }

    @FunctionalInterface
    public interface Executor {
        void get() throws ApiException;
    }

    void validateErrorResponse(Executor aExecutor, int aCode) throws Exception {
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