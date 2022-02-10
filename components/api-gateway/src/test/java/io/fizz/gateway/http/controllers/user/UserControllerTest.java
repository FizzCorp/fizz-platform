package io.fizz.gateway.http.controllers.user;

import com.google.gson.Gson;
import io.fizz.gateway.Constants;
import io.fizz.gateway.http.MockApplication;
import io.fizz.gateway.http.controllers.TestUtils;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.AuthApi;
import io.swagger.client.api.UserApi;
import io.swagger.client.model.*;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
public class UserControllerTest {

    private static Vertx vertx;
    private static String sessionTokenUser1;
    private static String sessionTokenUserA;
    private static String sessionTokenUserB;
    private static final String APP_ID = "appA";
    private static final String USER_ID_1 = "user1";
    private static final String USER_A = "userA";
    private static final String USER_B = "user_offline";

    static private Future<String> deployVertex() {
        Promise<String> deployed = Promise.promise();
        vertx.deployVerticle(MockApplication.class.getName(), deployed);

        return deployed.future();
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
        if (Objects.nonNull(sessionTokenUser1)) {
            return;
        }
        final AuthApi api = new AuthApi(new ApiClient());
        final SessionAuthRequest request = new SessionAuthRequest();
        request.setAppId(APP_ID);
        request.setLocale(LanguageCode.EN);

        request.setUserId(USER_ID_1);
        api.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(request), "secret")
        );
        sessionTokenUser1 = api.createSession(request).getToken();

        request.setUserId(USER_A);
        api.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(request), "secret")
        );
        sessionTokenUserA = api.createSession(request).getToken();

        request.setUserId(USER_B);
        api.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(request), "secret")
        );
        sessionTokenUserB = api.createSession(request).getToken();
    }

    @Test
    @DisplayName("it should fetch user profiles")
    void fetchUserProfileTest() throws ApiException {
        final UserApi client = new UserApi(new ApiClient());
        client.getApiClient().setApiKey(sessionTokenUser1);

        User userA = client.queryUser(USER_A);
        Assertions.assertNotNull(userA);
        Assertions.assertEquals("nickA", userA.getNick());
        Assertions.assertEquals("statusMessageA", userA.getStatusMessage());
        Assertions.assertEquals("http://www.google.com", userA.getProfileUrl());
        Assertions.assertEquals(true, userA.isIsOnline());

        User userB = client.queryUser(USER_B);
        Assertions.assertNotNull(userB);
        Assertions.assertEquals("nickB", userB.getNick());
        Assertions.assertEquals("statusMessageB", userB.getStatusMessage());
        Assertions.assertEquals("http://www.gmail.com", userB.getProfileUrl());
        Assertions.assertEquals(false, userB.isIsOnline());
    }

    @Test
    @DisplayName("it should update user nick")
    void updateUserNickTest() throws ApiException {
        final UserApi client = new UserApi(new ApiClient());
        client.getApiClient().setApiKey(sessionTokenUserA);

        User userA = client.queryUser(USER_A);
        Assertions.assertNotNull(userA);
        Assertions.assertEquals("nickA", userA.getNick());

        UpdateUserRequest nickRequest = new UpdateUserRequest();
        nickRequest.setNick("Updated Nick A");
        client.updateUser(USER_A, nickRequest);

        userA = client.queryUser(USER_A);
        Assertions.assertNotNull(userA);
        Assertions.assertEquals("Updated Nick A", userA.getNick());
    }

    @Test
    @DisplayName("it should update user nick")
    void updateUserStatusTest() throws ApiException {
        final UserApi client = new UserApi(new ApiClient());
        client.getApiClient().setApiKey(sessionTokenUserA);

        User userA = client.queryUser(USER_A);
        Assertions.assertNotNull(userA);
        Assertions.assertEquals("statusMessageA", userA.getStatusMessage());

        UpdateUserRequest statusRequest = new UpdateUserRequest();
        statusRequest.setStatusMessage("Updated Status Message");
        client.updateUser(USER_A, statusRequest);

        userA = client.queryUser(USER_A);
        Assertions.assertNotNull(userA);
        Assertions.assertEquals("Updated Status Message", userA.getStatusMessage());
    }

    @Test
    @DisplayName("it should update user profile url")
    void updateUserProfileUrlTest() throws ApiException {
        final UserApi client = new UserApi(new ApiClient());
        client.getApiClient().setApiKey(sessionTokenUserA);

        User userA = client.queryUser(USER_A);
        Assertions.assertNotNull(userA);
        Assertions.assertEquals("http://www.google.com", userA.getProfileUrl());

        UpdateUserRequest profileUrlRequest = new UpdateUserRequest();
        profileUrlRequest.setProfileUrl("http://www.fizz.com");
        client.updateUser(USER_A, profileUrlRequest);

        userA = client.queryUser(USER_A);
        Assertions.assertNotNull(userA);
        Assertions.assertEquals("http://www.fizz.com", userA.getProfileUrl());
    }

    @Test
    @DisplayName("it should not allow a user to update other user's nick")
    void invalidUpdateUserNickTest() throws Exception {
        final UserApi client = new UserApi(new ApiClient());
        client.getApiClient().setApiKey(sessionTokenUserA);

        User userB = client.queryUser(USER_B);
        Assertions.assertNotNull(userB);
        Assertions.assertEquals("nickB", userB.getNick());

        UpdateUserRequest nickRequest = new UpdateUserRequest();
        nickRequest.setNick("Updated Nick B");
        validateErrorResponse(() -> client.updateUser(USER_B, nickRequest), 403);

        userB = client.queryUser(USER_B);
        Assertions.assertNotNull(userB);
        Assertions.assertEquals("nickB", userB.getNick());
    }

    @Test
    @DisplayName("it should not allow a user to update other user's status")
    void invalidUpdateUserStatusTest() throws Exception {
        final UserApi client = new UserApi(new ApiClient());
        client.getApiClient().setApiKey(sessionTokenUserA);

        User userB = client.queryUser(USER_B);
        Assertions.assertNotNull(userB);
        Assertions.assertEquals("statusMessageB", userB.getStatusMessage());

        UpdateUserRequest statusRequest = new UpdateUserRequest();
        statusRequest.setStatusMessage("Updated Status Message B");
        validateErrorResponse(() -> client.updateUser(USER_B, statusRequest), 403);

        userB = client.queryUser(USER_B);
        Assertions.assertNotNull(userB);
        Assertions.assertEquals("statusMessageB", userB.getStatusMessage());
    }

    @Test
    @DisplayName("it should not allow a user to update other user's profile url")
    void invalidUpdateUserProfileUrlTest() throws Exception {
        final UserApi client = new UserApi(new ApiClient());
        client.getApiClient().setApiKey(sessionTokenUserA);

        User userB = client.queryUser(USER_B);
        Assertions.assertNotNull(userB);
        Assertions.assertEquals("http://www.gmail.com", userB.getProfileUrl());

        UpdateUserRequest profileUrlRequest = new UpdateUserRequest();
        profileUrlRequest.setProfileUrl("http://www.fizz.com");
        validateErrorResponse(() -> client.updateUser(USER_B, profileUrlRequest), 403);

        userB = client.queryUser(USER_B);
        Assertions.assertNotNull(userB);
        Assertions.assertEquals("http://www.gmail.com", userB.getProfileUrl());
    }

    @Test
    @DisplayName("it should update user token")
    void updateUserTokenTest() throws ApiException {
        final UserApi client = new UserApi(new ApiClient());
        client.getApiClient().setApiKey(sessionTokenUserA);

        User userA = client.queryUser(USER_A);
        Assertions.assertNotNull(userA);
        Assertions.assertNull(userA.getTokens());

        SetUserDeviceRequest request = new SetUserDeviceRequest();
        request.setPlatform(SetUserDeviceRequest.PlatformEnum.FCM);
        request.setToken("test");
        client.setDeviceToken(USER_A, request);

        userA = client.queryUser(USER_A);
        Assertions.assertNotNull(userA);
        Assertions.assertEquals(1, userA.getTokens().size());
        Assertions.assertEquals("test", userA.getTokens().get("fcm"));

        client.clearDeviceToken(USER_A, SetUserDeviceRequest.PlatformEnum.FCM.getValue());
        userA = client.queryUser(USER_A);
        Assertions.assertNotNull(userA);
        Assertions.assertNull(userA.getTokens());
    }

    @Test
    @DisplayName("it should allow a user to update another user's device token")
    void updateAnotherUserTokenTest() throws ApiException {
        final UserApi client = new UserApi(new ApiClient());
        client.getApiClient().setApiKey(sessionTokenUserA);

        User userA = client.queryUser(USER_A);
        Assertions.assertNotNull(userA);
        Assertions.assertNull(userA.getTokens());

        SetUserDeviceRequest request = new SetUserDeviceRequest();
        request.setPlatform(SetUserDeviceRequest.PlatformEnum.FCM);
        request.setToken("test");
        Assertions.assertThrows(ApiException.class, () -> client.setDeviceToken("userB", request));
    }

    @Test
    @DisplayName("it should not allow a user to update profile with invalid url")
    void invalidUpdateUserProfileUrlTest2() throws Exception {
        final UserApi client = new UserApi(new ApiClient());
        client.getApiClient().setApiKey(sessionTokenUserB);

        User userB = client.queryUser(USER_B);
        Assertions.assertNotNull(userB);
        Assertions.assertEquals("http://www.gmail.com", userB.getProfileUrl());

        UpdateUserRequest profileUrlRequest = new UpdateUserRequest();
        profileUrlRequest.setProfileUrl("broken invalid-url.com");
        validateErrorResponse(() -> client.updateUser(USER_B, profileUrlRequest), 400);

        userB = client.queryUser(USER_B);
        Assertions.assertNotNull(userB);
        Assertions.assertEquals("http://www.gmail.com", userB.getProfileUrl());
    }

    @Test
    @DisplayName("it should subscribe user profile")
    void subscribeProfileTest() throws ApiException {
        final UserApi client = new UserApi(new ApiClient());
        client.getApiClient().setApiKey(sessionTokenUser1);
        client.addUserSubscriber(USER_A);
        client.addUserSubscriber(USER_B);
    }

    @Test
    @DisplayName("it should unsubscribe user profile")
    void unsubscribeProfileTest() throws ApiException {
        final UserApi client = new UserApi(new ApiClient());
        client.getApiClient().setApiKey(sessionTokenUser1);
        client.removeUserSubscriber(USER_A);
        client.removeUserSubscriber(USER_B);
    }

    @FunctionalInterface
    public interface Executor {
        void get() throws ApiException;
    }

    private void validateErrorResponse(Executor aExecutor, int aCode) throws Exception {
        try {
            aExecutor.get();
        }
        catch (ApiException ex) {
            Assertions.assertEquals(ex.getCode(), aCode);
            return;
        }
        throw new ApiException("no exception thrown");
    }
}
