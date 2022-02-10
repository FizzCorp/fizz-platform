package io.fizz.gateway.http.controllers;

import com.google.gson.Gson;
import io.fizz.gateway.Constants;
import io.fizz.gateway.http.MockApplication;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.AuthApi;
import io.swagger.client.api.ModerationApi;
import io.swagger.client.model.FilterTextsResponse;
import io.swagger.client.model.LanguageCode;
import io.swagger.client.model.SessionAuthRequest;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
public class ContentModerationControllerTest {

    private static Vertx vertx;
    private static String sessionToken;
    private static final String APP_ID = "appA";
    private static final String USER_ID_1 = "user1";

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

        request.setUserId(USER_ID_1);
        request.setAppId(APP_ID);
        request.setLocale(LanguageCode.EN);
        api.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(request), "secret")
        );

        sessionToken = api.createSession(request).getToken();
    }

    @Test
    @DisplayName("it should moderate text messages")
    void validModerateTextMessagesTest() throws ApiException {

        final ModerationApi client = new ModerationApi(new ApiClient());

        List<String> requestTexts = Arrays.asList(
                "Hi, This is sample Filter 1. See you around",
                "this is sample Filter 6",
                "this is the Filter 3. Bye");

        List<String> expectedResult = Arrays.asList(
                "Hi, This is sample ***. See you around",
                "this is sample Filter 6",
                "this is the ***. Bye");

        client.getApiClient().setApiKey(sessionToken);
        FilterTextsResponse actualResult = client.filterTexts(requestTexts);
        Assertions.assertEquals(requestTexts.size(), actualResult.size());
        for (int ti = 0; ti < requestTexts.size(); ti++) {
            Assertions.assertEquals(expectedResult.get(ti), actualResult.get(ti));
        }

        List<String> listWithEmptyText = Arrays.asList(
                "Text 1",
                "",
                "Text 3");

        client.getApiClient().setApiKey(sessionToken);
        List<String> emptyTextResult = client.filterTexts(listWithEmptyText);
        Assertions.assertEquals(listWithEmptyText.size(), emptyTextResult.size());
        for (int ti = 0; ti < listWithEmptyText.size(); ti++) {
            Assertions.assertEquals(listWithEmptyText.get(ti), emptyTextResult.get(ti));
        }
    }

    @Test
    @DisplayName("it should throw exception for invalid text array size")
    void invalidTextArraySizeTest() throws Exception {

        final ModerationApi client = new ModerationApi(new ApiClient());
        List<String> oversizeList = Arrays.asList(
                "Text 1",
                "Text 2",
                "Text 3",
                "Text 4",
                "Text 5",
                "Text 6");

        client.getApiClient().setApiKey(sessionToken);
        validateErrorResponse(() -> client.filterTexts(oversizeList), 400);
    }

    @Test
    @DisplayName("it should throw exception for invalid text")
    void invalidTextTest() throws Exception {

        final ModerationApi client = new ModerationApi(new ApiClient());
        List<String> listWithNullText = Arrays.asList(
                "Text 1",
                null,
                "Text 3");

        client.getApiClient().setApiKey(sessionToken);
        validateErrorResponse(() -> client.filterTexts(listWithNullText), 400);
    }

    @Test
    @DisplayName("it should throw exception for invalid text length")
    void invalidMaximumLengthTextTest() throws Exception {

        final ModerationApi client = new ModerationApi(new ApiClient());

        final byte[] largeText = new byte[2000];
        new Random().nextBytes(largeText);
        List<String> listWithLongText = Collections.singletonList(
                new String(largeText, StandardCharsets.UTF_8));

        client.getApiClient().setApiKey(sessionToken);
        validateErrorResponse(() -> client.filterTexts(listWithLongText), 400);
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
