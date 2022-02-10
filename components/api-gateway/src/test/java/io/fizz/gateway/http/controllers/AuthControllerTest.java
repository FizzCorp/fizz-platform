package io.fizz.gateway.http.controllers;

import com.google.gson.Gson;
import io.fizz.chatcommon.infrastructure.WebUtils;
import io.fizz.common.ConfigService;
import io.fizz.gateway.Constants;
import io.fizz.gateway.http.MockApplication;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.AuthApi;
import io.swagger.client.api.ChannelApi;
import io.swagger.client.api.GroupApi;
import io.swagger.client.model.*;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
class AuthControllerTest {
    private static final int SESSION_CHANNELS_SIZE_MAX = ConfigService.instance().getNumber("session.channels.size.max").intValue();
    private static final int TEST_TIMEOUT = Constants.TEST_TIMEOUT;
    static Vertx vertx;
    static int port;
    private static final String APP_ID = "appA";
    private static final String USER_ID = "userA";
    protected static final String USER_ADMIN = "admin";

    @BeforeAll
    static void setUp(VertxTestContext aContext) throws InterruptedException {
        vertx = Vertx.vertx();
        port = ConfigService.instance().getNumber("http.port").intValue();

        vertx.deployVerticle(new MockApplication(), res -> aContext.completeNow());

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @AfterAll
    static void tearDown(VertxTestContext aContext) {
        vertx.close(res -> aContext.completeNow());
    }

    @Test
    @DisplayName("it should start a proper session")
    void createSessionTest() throws Throwable {
        final AuthApi client = new AuthApi(new ApiClient());
        final SessionAuthRequest request = new SessionAuthRequest();

        request.setAppId(APP_ID);
        request.setUserId("userA");

        TestUtils.signV2(
                client.getApiClient(),
                APP_ID,
                "POST",
                "/v1/sessions",
                new Gson().toJson(request),
                "secret"
        );

        final SessionAuthReply reply = client.createSession(request);
        Assertions.assertNotEquals(reply.getToken(), null);
        Assertions.assertFalse(reply.getToken().isEmpty());

        Assertions.assertNotEquals(reply.getSubscriberId(), null);
        Assertions.assertFalse(reply.getSubscriberId().isEmpty());

        Assertions.assertTrue(reply.getNowTs().longValue() > 0);
    }

    @Test
    @DisplayName("it should send error for invalid application id")
    void invalidApplicationIdTest() {
        final AuthApi client = new AuthApi(new ApiClient());
        final SessionAuthRequest req = new SessionAuthRequest();
        final Gson serde = new Gson();

        req.setUserId("userA");

        try {
            req.setAppId(null);
            client.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(serde.toJson(req), "secret")
            );
            client.createSession(req);
        } catch (ApiException ex) {
            Assertions.assertEquals(ex.getCode(), HttpStatus.SC_UNAUTHORIZED);
            Assertions.assertEquals(reason(ex.getResponseBody()), "invalid_app_id");
        }

        try {
            req.setAppId("");
            client.getApiClient().addDefaultHeader(
                    "Authorization",
                    "HMAC-SHA256 " + TestUtils.createSignature(serde.toJson(req), "secret")
            );
            client.createSession(req);
        } catch (ApiException ex) {
            Assertions.assertEquals(ex.getCode(), HttpStatus.SC_UNAUTHORIZED);
            Assertions.assertEquals(reason(ex.getResponseBody()), "unknown_app");
        }

        try {
            req.setAppId(generateString(65));
            client.getApiClient().addDefaultHeader(
                    "Authorization",
                    "HMAC-SHA256 " + TestUtils.createSignature(serde.toJson(req), "secret")
            );
            client.createSession(req);
        } catch (ApiException ex) {
            Assertions.assertEquals(ex.getCode(), HttpStatus.SC_UNAUTHORIZED);
            Assertions.assertEquals(reason(ex.getResponseBody()), "unknown_app");
        }
    }

    @Test
    @DisplayName("it should send error for invalid user id")
    void invalidUserIdTest() {
        final AuthApi client = new AuthApi(new ApiClient());
        final SessionAuthRequest req = new SessionAuthRequest();
        final Gson serde = new Gson();

        req.setAppId(APP_ID);

        try {
            req.setUserId(null);
            client.getApiClient().addDefaultHeader(
                    "Authorization",
                    "HMAC-SHA256 " + TestUtils.createSignature(serde.toJson(req), "secret")
            );
            client.createSession(req);
        } catch (ApiException ex) {
            Assertions.assertEquals(ex.getCode(), HttpStatus.SC_BAD_REQUEST);
            Assertions.assertEquals(reason(ex.getResponseBody()), "invalid_user_id");
        }

        try {
            req.setUserId("");
            client.getApiClient().addDefaultHeader(
                    "Authorization",
                    "HMAC-SHA256 " + TestUtils.createSignature(serde.toJson(req), "secret")
            );
            client.createSession(req);
        } catch (ApiException ex) {
            Assertions.assertEquals(ex.getCode(), HttpStatus.SC_BAD_REQUEST);
            Assertions.assertEquals(reason(ex.getResponseBody()), "invalid_user_id");
        }

        try {
            req.setUserId(generateString(65));
            client.getApiClient().addDefaultHeader(
                    "Authorization",
                    "HMAC-SHA256 " + TestUtils.createSignature(serde.toJson(req), "secret")
            );
            client.createSession(req);
        } catch (ApiException ex) {
            Assertions.assertEquals(ex.getCode(), HttpStatus.SC_BAD_REQUEST);
            Assertions.assertEquals(reason(ex.getResponseBody()), "invalid_user_id");
        }
    }

    @Test
    @DisplayName("it should start a proper session with empty channels")
    void createSessionWithEmptyChannelsListTest() throws Exception {
        String sessionToken = createSession(new AllowedChannels());
        Assertions.assertNotEquals(sessionToken, null);
        Assertions.assertFalse(sessionToken.isEmpty());

        final ChannelApi api = new ChannelApi(new ApiClient());
        final ChannelMessageModel message1 = buildMessage("userA","Message 1", "data1", true);
        api.getApiClient().setApiKey(sessionToken);

        validateErrorResponse(() -> api.publishChannelMessage("global", message1), HttpStatus.SC_UNAUTHORIZED);
        validateErrorResponse(() -> api.addChannelSubscriber("global"), HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("it should allow to specify channels within limit while creating session")
    void createSessionWithInvalidChannelsListSizeTest() throws Exception {
        AllowedChannels channels = new AllowedChannels();
        for (int fId=0; fId < SESSION_CHANNELS_SIZE_MAX+1; fId++) {
            ChannelFilter filter = new ChannelFilter();
            filter.setStartsWith("global-"+fId);
            channels.add(filter);
        }

        validateErrorResponse(() -> createSession(channels), WebUtils.STATUS_BAD_REQUEST);
    }

    @Test
    @DisplayName("it should start a proper session with channel filters and update later on")
    void createSessionWithChannelsTest() throws Exception {
        final AuthApi client = new AuthApi(new ApiClient());
        final SessionAuthRequest request = new SessionAuthRequest();

        String userId = "userA";
        request.setAppId(APP_ID);
        request.setUserId(userId);

        AllowedChannels channels = new AllowedChannels();

        ChannelFilter startsWithFilter = new ChannelFilter();
        startsWithFilter.setStartsWith("global-");

        channels.add(startsWithFilter);
        request.setChannels(channels);

        client.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(request), "secret")
        );

        final SessionAuthReply reply = client.createSession(request);
        Assertions.assertNotEquals(reply.getToken(), null);
        Assertions.assertFalse(reply.getToken().isEmpty());

        Assertions.assertNotEquals(reply.getSubscriberId(), null);
        Assertions.assertFalse(reply.getSubscriberId().isEmpty());

        Assertions.assertTrue(reply.getNowTs().longValue() > 0);

        final String sessionToken = reply.getToken();
        final ChannelApi api = new ChannelApi(new ApiClient());
        final ChannelMessageModel message1 = buildMessage("userA","Message 1", "data1", true);

        api.getApiClient().setApiKey(sessionToken);
        api.publishChannelMessage("global-", message1);
        api.publishChannelMessage("global-1", message1);
        api.publishChannelMessage("global-2", message1);

        validateErrorResponse(() -> api.publishChannelMessage("global", message1), HttpStatus.SC_UNAUTHORIZED);
        validateErrorResponse(() -> api.publishChannelMessage("team-spartan", message1), HttpStatus.SC_UNAUTHORIZED);
        validateErrorResponse(() -> api.publishChannelMessage("team-united", message1), HttpStatus.SC_UNAUTHORIZED);

        final UpdateSessionAuthRequest updateRequest = new UpdateSessionAuthRequest();
        updateRequest.setAppId(APP_ID);
        updateRequest.setToken(sessionToken);

        AllowedChannels updatedChannels = new AllowedChannels();

        ChannelFilter updatedStartsWithFilter = new ChannelFilter();
        updatedStartsWithFilter.setStartsWith("local-");

        ChannelFilter matchesFilter = new ChannelFilter();
        matchesFilter.setMatches("team-spartan");

        updatedChannels.add(updatedStartsWithFilter);
        updatedChannels.add(matchesFilter);
        updateRequest.setChannels(updatedChannels);

        client.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(updateRequest), "secret")
        );

        client.updateSession(userId, updateRequest);

        api.publishChannelMessage("local-", message1);
        api.publishChannelMessage("local-1", message1);
        api.publishChannelMessage("local-2", message1);
        api.publishChannelMessage("team-spartan", message1);

        validateErrorResponse(() -> api.publishChannelMessage("global-", message1), HttpStatus.SC_UNAUTHORIZED);
        validateErrorResponse(() -> api.publishChannelMessage("global-1", message1), HttpStatus.SC_UNAUTHORIZED);
        validateErrorResponse(() -> api.publishChannelMessage("global-2", message1), HttpStatus.SC_UNAUTHORIZED);
        validateErrorResponse(() -> api.publishChannelMessage("local", message1), HttpStatus.SC_UNAUTHORIZED);
        validateErrorResponse(() -> api.publishChannelMessage("team-united", message1), HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("it should allow to user to perform operations in permitted channel")
    void messageCRUDWithFilteredChannelsTest() throws Exception {
        final String sessionToken = createSession(null);
        final ChannelApi chatApi = new ChannelApi(new ApiClient());
        chatApi.getApiClient().setApiKey(sessionToken);

        String channel = "TestChannel";

        // Should Publish Message
        ChannelMessageModel message = buildMessage("userA","Message 1", "data1", true);
        chatApi.publishChannelMessage(channel, message);

        // Should be able to fetch messages
        List<ChannelMessage> messages = chatApi.queryChannelMessages(channel, 10, null, null);

        // Should be able to update own message
        UpdateChannelMessageRequest updatedMessageRequest = buildUpdateMessageRequest("userA","message1-updated", "data1-updated");
        chatApi.updateChannelMessage(channel, messages.get(0).getId().toString(), updatedMessageRequest);

        messages = chatApi.queryChannelMessages(channel, 10, null, null);
        Assertions.assertEquals("message1-updated", messages.get(0).getBody());

        // Should be able to delete own message
        chatApi.deleteChannelMessage(channel, messages.get(0).getId().toString());
        messages = chatApi.queryChannelMessages(channel, 10, null, null);
        Assertions.assertEquals(0, messages.size());

        // Update session with allowed channels
        String allowedChannel = "TestChannelUpdate";
        updateSession(sessionToken, createMatchChannelFilter(allowedChannel));

        message = buildMessage("userA","Message 2", "data2", true);

        // Should be able to publish in allowed channel
        chatApi.publishChannelMessage(allowedChannel, message);

        ChannelMessageModel finalMessage = message;
        // Shouldn't be able to publish in restricted channel
        validateErrorResponse(() -> chatApi.publishChannelMessage(channel, finalMessage), HttpStatus.SC_UNAUTHORIZED);

        // Should be able to fetch messages from allowed channel
        messages = chatApi.queryChannelMessages(allowedChannel, 10, null, null);
        Assertions.assertEquals(1, messages.size());

        // Shouldn't be able to fetch messages from restricted channel
        validateErrorResponse(() -> chatApi.queryChannelMessages(channel, 10, null, null), HttpStatus.SC_UNAUTHORIZED);

        // Should be able to update message in allowed channel
        updatedMessageRequest = buildUpdateMessageRequest("userA","message1-updated", "data1-updated");
        chatApi.updateChannelMessage(allowedChannel, messages.get(0).getId().toString(), updatedMessageRequest);
        messages = chatApi.queryChannelMessages(allowedChannel, 10, null, null);
        Assertions.assertEquals("message1-updated", messages.get(0).getBody());

        // Shouldn't be able to update message in restricted channel
        List<ChannelMessage> finalMessages = messages;
        UpdateChannelMessageRequest finalUpdatedMessageRequest = updatedMessageRequest;
        validateErrorResponse(() -> chatApi.updateChannelMessage(channel, finalMessages.get(0).getId().toString(), finalUpdatedMessageRequest), HttpStatus.SC_UNAUTHORIZED);

        // Should be able to delete own message in allowed channel
        chatApi.deleteChannelMessage(allowedChannel, messages.get(0).getId().toString());
        messages = chatApi.queryChannelMessages(allowedChannel, 10, null, null);
        Assertions.assertEquals(0, messages.size());

        chatApi.publishChannelMessage(allowedChannel, message);
        messages = chatApi.queryChannelMessages(allowedChannel, 10, null, null);

        // Should be able to delete own message in restricted channel
        List<ChannelMessage> finalMessages1 = messages;
        validateErrorResponse(() -> chatApi.deleteChannelMessage(channel, finalMessages1.get(0).getId().toString()), HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("it should allow to user to perform operations in permitted channel")
    void pubSubWithFilteredChannelsTest() throws Exception {
        final String sessionToken = createSession(null);
        final ChannelApi chatApi = new ChannelApi(new ApiClient());
        chatApi.getApiClient().setApiKey(sessionToken);

        String channel = "TestChannel";

        // Should subscribe channel
        chatApi.addChannelSubscriber(channel);

        // Should unsubscribe channel
        chatApi.removeChannelSubscriber(channel);

        // Update session with allowed channels
        String allowedChannel = "TestChannelUpdate";
        updateSession(sessionToken, createMatchChannelFilter(allowedChannel));

        // Should subscribe allowed channel
        chatApi.addChannelSubscriber(allowedChannel);

        // Should unsubscribe allowed channel
        chatApi.removeChannelSubscriber(allowedChannel);

        // Shouldn't subscribe restricted channel
        validateErrorResponse(() -> chatApi.addChannelSubscriber(channel), HttpStatus.SC_UNAUTHORIZED);

        // Should unsubscribe restricted channel
        chatApi.removeChannelSubscriber(channel);

        // Update session with allowed channels
        String allowedChannelWithSuffix = "global-channel-";
        updateSession(sessionToken, createStartsWithChannelFilter(allowedChannelWithSuffix));

        // Should subscribe allowed channel
        chatApi.addChannelSubscriber(allowedChannelWithSuffix);
        chatApi.addChannelSubscriber(allowedChannelWithSuffix + "1");
        chatApi.addChannelSubscriber(allowedChannelWithSuffix + "2");

        // Should unsubscribe allowed channel
        chatApi.removeChannelSubscriber(allowedChannelWithSuffix);
        chatApi.removeChannelSubscriber(allowedChannelWithSuffix + "1");
        chatApi.removeChannelSubscriber(allowedChannelWithSuffix + "2");

        // Shouldn't subscribe restricted channel
        validateErrorResponse(() -> chatApi.addChannelSubscriber("a" + allowedChannelWithSuffix), HttpStatus.SC_UNAUTHORIZED);
        validateErrorResponse(() -> chatApi.addChannelSubscriber("1" + allowedChannelWithSuffix), HttpStatus.SC_UNAUTHORIZED);
        validateErrorResponse(() -> chatApi.addChannelSubscriber("1" + allowedChannelWithSuffix + "abc"), HttpStatus.SC_UNAUTHORIZED);
        validateErrorResponse(() -> chatApi.addChannelSubscriber(channel), HttpStatus.SC_UNAUTHORIZED);

        // Should unsubscribe restricted channel
        chatApi.removeChannelSubscriber(channel);
    }

    @Test
    @DisplayName("it should apply app mute")
    void applyAppMuteTest() throws Exception {
        final String USER_ID = "ApplyMuteUserId";

        final AuthApi api = new AuthApi(new ApiClient());
        final ApplyAppMuteUserRequest appMuteRequest = new ApplyAppMuteUserRequest();
        appMuteRequest.appId(APP_ID);
        appMuteRequest.setUserId(USER_ID);
        api.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(appMuteRequest), "secret")
        );
        api.muteApplicationUser(appMuteRequest);
    }

    @Test
    @DisplayName("it should not apply app mute with missing appId")
    void applyAppMuteMissingAppIdTest() throws Exception {
        final AuthApi api = new AuthApi(new ApiClient());
        final ApplyAppMuteUserRequest appMuteRequest = new ApplyAppMuteUserRequest();
        appMuteRequest.setAppId(null);
        appMuteRequest.setUserId(USER_ID);
        api.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(appMuteRequest), "secret")
        );

        validateErrorResponse(() -> api.muteApplicationUser(appMuteRequest), 401);
    }

    @Test
    @DisplayName("it should not apply app mute with missing userId")
    void applyAppMuteMissingUserIdTest() throws Exception {
        final AuthApi api = new AuthApi(new ApiClient());
        final ApplyAppMuteUserRequest appMuteRequest = new ApplyAppMuteUserRequest();
        appMuteRequest.setAppId(APP_ID);
        api.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(appMuteRequest), "secret")
        );

        validateErrorResponse(() -> api.muteApplicationUser(appMuteRequest), HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    @DisplayName("it should not apply app mute with invalid secret")
    void applyAppMuteInvalidSecretTest() throws Exception {
        final AuthApi api = new AuthApi(new ApiClient());
        final ApplyAppMuteUserRequest appMuteRequest = new ApplyAppMuteUserRequest();
        appMuteRequest.setAppId(APP_ID);
        appMuteRequest.setUserId(USER_ID);
        api.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(appMuteRequest), "wrong_secret")
        );

        validateErrorResponse(() -> api.muteApplicationUser(appMuteRequest), HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("it should remove app mute role")
    void removeAppMuteTest() throws Exception {
        final AuthApi api = new AuthApi(new ApiClient());
        final ApplyAppMuteUserRequest appMuteRequest = new ApplyAppMuteUserRequest();
        appMuteRequest.setAppId(APP_ID);
        appMuteRequest.setUserId(USER_ID);
        api.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(appMuteRequest), "secret")
        );
        // Apply app level mute
        api.muteApplicationUser(appMuteRequest);

        final RemoveAppMuteUserRequest removeAppMuteRequest = new RemoveAppMuteUserRequest();
        removeAppMuteRequest.setAppId(APP_ID);
        removeAppMuteRequest.setUserId(USER_ID);
        api.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(removeAppMuteRequest), "secret")
        );
        // Remove app level mute
        api.unmuteApplicationUser(removeAppMuteRequest);

        // Verify app level mute is not applied
        validateErrorResponse(() -> api.unmuteApplicationUser(removeAppMuteRequest), HttpStatus.SC_CONFLICT);
    }

    @Test
    @DisplayName("App muted user should not be able to publish/update/delete channel message")
    void appMutedPublishUpdateDeleteChannelMessageTest() throws Exception {
        final String USER_ID = "ChannelTestUserId";

        // creating user session
        final AuthApi authApi = new AuthApi(new ApiClient());
        final SessionAuthRequest request = new SessionAuthRequest();
        request.setUserId(USER_ID);
        request.setAppId(APP_ID); 
        request.setLocale(LanguageCode.EN);
        authApi.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(request), "secret")
        );
        String sessionToken = authApi.createSession(request).getToken();

        // Publishing in channel
        final ChannelApi channelApi = new ChannelApi(new ApiClient());
        final String channel = UUID.randomUUID().toString();
        ChannelMessageModel message1 = buildMessage("userA","message1", "data1", true);
        UpdateChannelMessageRequest updateMessageRequest = new UpdateChannelMessageRequest();
        updateMessageRequest.setBody("updated message");

        // publish message before banning to test edit, delete etc
        channelApi.getApiClient().setApiKey(sessionToken);
        channelApi.publishChannelMessage(channel, message1);

        // Apply app level mute
        final ApplyAppMuteUserRequest appMuteRequest = new ApplyAppMuteUserRequest();
        appMuteRequest.setAppId(APP_ID);
        appMuteRequest.setUserId(USER_ID);
        authApi.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(appMuteRequest), "secret")
        );
        authApi.muteApplicationUser(appMuteRequest);

        // validate ops after applying app mute
        List<ChannelMessage> messages = channelApi.queryChannelMessages(channel, 5, null, null);
        Assertions.assertEquals(messages.size(), 1);
        String messageId = messages.get(0).getId().toString();

        channelApi.addChannelSubscriber(channel);
        validateErrorResponse(() -> channelApi.publishChannelMessage(channel, message1), 403);
        validateErrorResponse(() -> channelApi.updateChannelMessage(channel, messageId, updateMessageRequest), 403);
        validateErrorResponse(() -> channelApi.deleteChannelMessage(channel, messageId), 403);
        channelApi.removeChannelSubscriber(channel);

        // Remove app level mute
        final RemoveAppMuteUserRequest removeAppMuteRequest = new RemoveAppMuteUserRequest();
        removeAppMuteRequest.setAppId(APP_ID);
        removeAppMuteRequest.setUserId(USER_ID);
        authApi.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(removeAppMuteRequest), "secret")
        );
        authApi.unmuteApplicationUser(removeAppMuteRequest);

        // validate ops after removing app mute
        channelApi.publishChannelMessage(channel, message1);
        channelApi.updateChannelMessage(channel, messageId, updateMessageRequest);
        channelApi.deleteChannelMessage(channel, messageId);
    }

    @Test
    @DisplayName("App muted user should not be able to publish/update/delete channel message")
    void appMutedPublishGroupMessageTest() throws Exception {
        final String USER_ID = "GroupTestUserId";

        // creating user session
        final AuthApi authApi = new AuthApi(new ApiClient());
        SessionAuthRequest request = new SessionAuthRequest();
        request.setUserId(USER_ID);
        request.setAppId(APP_ID);
        request.setLocale(LanguageCode.EN);
        authApi.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(request), "secret")
        );
        String userSession = authApi.createSession(request).getToken();

        // Creating admin
        final CreateAdminRequest createAdminRequest = new CreateAdminRequest();
        createAdminRequest.setAppId(APP_ID);
        createAdminRequest.setUserId(USER_ADMIN);
        authApi.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(createAdminRequest), "secret")
        );
        authApi.createAdministrator(createAdminRequest);

        // Creating admin session
        request = new SessionAuthRequest();
        request.setUserId(USER_ADMIN);
        request.setAppId(APP_ID);
        request.setLocale(LanguageCode.EN);
        authApi.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(request), "secret")
        );
        String adminSession = authApi.createSession(request).getToken();

        final GroupApi groupApi = new GroupApi(new ApiClient());
        final List<GroupMemberRequest> members = new ArrayList<>();

        final GroupMemberRequest memberReq = new GroupMemberRequest();
        memberReq.setId(USER_ID);
        memberReq.setRole(GroupRoleName.MEMBER);
        memberReq.setState(GroupMemberState.JOINED);
        members.add(memberReq);

        groupApi.getApiClient().setApiKey(adminSession);
        final Group group = createGroup(adminSession);
        groupApi.addGroupMembers(group.getId(), members);

        groupApi.getApiClient().setApiKey(userSession);
        ChannelMessageModel message1 = buildMessage("userA","Message 1", "data1", true);
        groupApi.publishGroupMessage(group.getId(), message1);

        // Apply app level mute
        final ApplyAppMuteUserRequest appMuteRequest = new ApplyAppMuteUserRequest();
        appMuteRequest.setAppId(APP_ID);
        appMuteRequest.setUserId(USER_ID);
        authApi.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(appMuteRequest), "secret")
        );
        authApi.muteApplicationUser(appMuteRequest);

        // validate ops after applying app mute
        groupApi.getApiClient().setApiKey(userSession);
        List<ChannelMessage> messages = groupApi.queryGroupMessages(group.getId(), 5, null, null);
        Assertions.assertEquals(messages.size(), 1);
        validateErrorResponse(() -> groupApi.publishGroupMessage(group.getId(), message1), 403);

        // Remove app level mute
        final RemoveAppMuteUserRequest removeAppMuteRequest = new RemoveAppMuteUserRequest();
        removeAppMuteRequest.setAppId(APP_ID);
        removeAppMuteRequest.setUserId(USER_ID);
        authApi.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(removeAppMuteRequest), "secret")
        );
        authApi.unmuteApplicationUser(removeAppMuteRequest);

        // validate ops after removing app mute
        groupApi.getApiClient().setApiKey(userSession);
        groupApi.publishGroupMessage(group.getId(), message1);
    }

    @Test
    @DisplayName("it should create administrator")
    void createAdministratorTest() throws Exception {
        final AuthApi api = new AuthApi(new ApiClient());
        final CreateAdminRequest createAdminRequest = new CreateAdminRequest();
        createAdminRequest.setAppId(APP_ID);
        createAdminRequest.setUserId(USER_ADMIN);
        api.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(createAdminRequest), "secret")
        );
        api.createAdministrator(createAdminRequest);
    }

    @Test
    @DisplayName("it should not create administrator with missing appId")
    void createAdministratorMissingAppIdTest() throws Exception {
        final AuthApi api = new AuthApi(new ApiClient());
        final CreateAdminRequest createAdminRequest = new CreateAdminRequest();
        createAdminRequest.setUserId(USER_ADMIN);
        api.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(createAdminRequest), "secret")
        );

        validateErrorResponse(() -> api.createAdministrator(createAdminRequest), HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("it should not create administrator with missing userId")
    void createAdministratorMissingUserIdTest() throws Exception {
        final AuthApi api = new AuthApi(new ApiClient());
        final CreateAdminRequest createAdminRequest = new CreateAdminRequest();
        createAdminRequest.setAppId(APP_ID);
        api.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(createAdminRequest), "secret")
        );

        validateErrorResponse(() -> api.createAdministrator(createAdminRequest), HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    @DisplayName("it should not create administrator with invalid secret")
    void createAdministratorInvalidSecretTest() throws Exception {
        final AuthApi api = new AuthApi(new ApiClient());
        final CreateAdminRequest createAdminRequest = new CreateAdminRequest();
        createAdminRequest.setAppId(APP_ID);
        createAdminRequest.setUserId(USER_ADMIN);
        api.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(createAdminRequest), "wrong_secret")
        );

        validateErrorResponse(() -> api.createAdministrator(createAdminRequest), HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("it should remove administrator role")
    void removeAdministratorRoleTest() throws Exception {
        final String userId = "remove-admin-testId12";
        final AuthApi api = new AuthApi(new ApiClient());
        final CreateAdminRequest createAdminRequest = new CreateAdminRequest();
        createAdminRequest.setAppId(APP_ID);
        createAdminRequest.setUserId(userId);
        api.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(createAdminRequest), "secret")
        );
        // Create admin
        api.createAdministrator(createAdminRequest);

        final RemoveAdminRequest removeAdminRequest = new RemoveAdminRequest();
        removeAdminRequest.setAppId(APP_ID);
        removeAdminRequest.setUserId(userId);
        api.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(removeAdminRequest), "secret")
        );
        // Remove admin role 
        api.removeAdministrator(removeAdminRequest);

        // Verify admin role is not applied
        validateErrorResponse(() -> api.removeAdministrator(removeAdminRequest), HttpStatus.SC_CONFLICT);
    }

    private static String reason(String json)
    {
        JsonObject obj = new JsonObject(json);
        return obj.getString("reason");
    }

    private static String generateString(int len) {
        final StringBuilder builder = new StringBuilder();
        for (int ci = 0; ci < len; ci++) {
            builder.append('c');
        }
        return builder.toString();
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

    private String createSession(AllowedChannels channels) throws ApiException {
        final AuthApi client = new AuthApi(new ApiClient());
        final SessionAuthRequest request = new SessionAuthRequest();
        request.setAppId(APP_ID);
        request.setUserId(USER_ID);
        if (!Objects.isNull(channels)) {
            request.setChannels(channels);
        }

        client.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(request), "secret")
        );

        return client.createSession(request).getToken();
    }

    private void updateSession(String sessionToken, AllowedChannels updatedChannels) throws ApiException {
        final AuthApi client = new AuthApi(new ApiClient());
        final UpdateSessionAuthRequest updateRequest = new UpdateSessionAuthRequest();
        updateRequest.setAppId(APP_ID);
        updateRequest.setToken(sessionToken);
        if (!Objects.isNull(updatedChannels)) {
            updateRequest.setChannels(updatedChannels);
        }

        client.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(updateRequest), "secret")
        );
        client.updateSession(USER_ID, updateRequest);
    }

    private AllowedChannels createMatchChannelFilter(String matchChannel) {
        AllowedChannels channels = new AllowedChannels();
        ChannelFilter filter = new ChannelFilter();
        filter.matches(matchChannel);
        channels.add(filter);
        return channels;
    }

    private AllowedChannels createStartsWithChannelFilter(String startsWithChannel) {
        AllowedChannels channels = new AllowedChannels();
        ChannelFilter filter = new ChannelFilter();
        filter.setStartsWith(startsWithChannel);
        channels.add(filter);
        return channels;
    }

    private Group createGroup(String adminToken) throws ApiException {
        final GroupApi api = new GroupApi(new ApiClient());
        final String title = "myGroup";
        final String imageURL = "title.com";
        final String description = "my group for communicating!";
        final String type = "myType";

        final CreateGroupRequest request = new CreateGroupRequest();
        request.setTitle(title);
        request.setImageUrl(imageURL);
        request.setDescription(description);
        request.setType(type);

        api.getApiClient().setApiKey(adminToken);
        return api.createGroup(request);
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
