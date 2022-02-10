package io.fizz.gateway.http.controllers;

import com.google.gson.Gson;
import io.fizz.gateway.Constants;
import io.fizz.gateway.http.MockApplication;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.AuthApi;
import io.swagger.client.model.CreateAdminRequest;
import io.swagger.client.model.LanguageCode;
import io.swagger.client.model.SessionAuthRequest;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class BaseControllerTest {
    protected static Vertx vertx;
    protected static String sessionToken1;
    protected static String sessionToken2;
    protected static String sessionToken3;
    protected static String adminToken;
    protected static final String APP_ID = "appA";
    protected static final String USER_ID_1 = "user1";
    protected static final String USER_ID_2 = "user2";
    protected static final String USER_ID_3 = "user3";
    protected static final String USER_ADMIN = "admin";

    static private Future<String> deployVertex() {
        Promise<String> promise = Promise.promise();

        vertx.deployVerticle(MockApplication.class.getName(), promise);

        return promise.future();
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
    public static void shutdown() {
        vertx.close();
    }

    @BeforeEach
    void init() throws ApiException {
        if (Objects.nonNull(sessionToken1)) {
            return;
        }
        final AuthApi api = new AuthApi(new ApiClient());

        sessionToken1 = createSessionToken(USER_ID_1, APP_ID, LanguageCode.EN);
        sessionToken2 = createSessionToken(USER_ID_2, APP_ID, LanguageCode.ES);
        sessionToken3 = createSessionToken(USER_ID_3, APP_ID, LanguageCode.FR);
        adminToken = createSessionToken(USER_ADMIN, APP_ID, LanguageCode.EN);

        final CreateAdminRequest createAdminRequest = new CreateAdminRequest();
        createAdminRequest.setAppId(APP_ID);
        createAdminRequest.setUserId(USER_ADMIN);
        api.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(createAdminRequest), "secret")
        );
        api.createAdministrator(createAdminRequest);
    }

    protected String createSessionToken(final String aUserId,
                                        final String aAppId,
                                        final LanguageCode aLocale) throws ApiException {
        final AuthApi api = new AuthApi(new ApiClient());

        final SessionAuthRequest request = new SessionAuthRequest();
        request.setUserId(aUserId);
        request.setAppId(aAppId);
        request.setLocale(aLocale);
        api.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(request), "secret")
        );

        return api.createSession(request).getToken();
    }
}
