package io.fizz.gateway.http.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.fizz.gateway.Constants;
import io.fizz.gateway.http.MockApplication;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.AuthApi;
import io.swagger.client.api.ChannelApi;
import io.swagger.client.api.TopicApi;
import io.swagger.client.model.*;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
public class ChatControllerTest {
    private static Vertx vertx;
    private static String sessionToken1;
    private static String sessionToken2;
    private static String adminToken;
    private static final String APP_ID = "appA";
    private static final String USER_ID_1 = "user1";
    private static final String USER_ID_2 = "user2";
    private static final String USER_ADMIN = "admin";

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
        if (Objects.nonNull(sessionToken1)) {
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

        sessionToken1 = api.createSession(request).getToken();

        request.setUserId(USER_ID_2);
        request.setLocale(LanguageCode.ES);
        api.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(request), "secret")
        );
        sessionToken2 = api.createSession(request).getToken();

        request.setUserId(USER_ADMIN);
        request.setLocale(LanguageCode.EN);
        api.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(request), "secret")
        );
        adminToken = api.createSession(request).getToken();

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
    @DisplayName("it should send publish all messages to the channel message journal")
    void channelMessageJournalTest() throws ApiException {
        final String channelId = UUID.randomUUID().toString();
        final String channelId2 = UUID.randomUUID().toString();

        final ChannelApi api = new ChannelApi(new ApiClient());
        final ChannelMessageModel message1 = buildMessage("userA","Filter 1", "data1", true);
        final ChannelMessageModel message2 = buildMessage("userB", "Filter 2", "data2", true);
        final ChannelMessageModel message3 = buildMessage("userA", "Filter 3", "data3", true);
        final ChannelMessageModel message4 = buildMessage("userB", "Filter 4", "data4", true);
        final ChannelMessageModel nonPersistMsg = buildMessage("userB", "Filter 5", "data5", false);

        api.getApiClient().setApiKey(sessionToken1);
        api.publishChannelMessage(channelId, message1);
        api.getApiClient().setApiKey(sessionToken2);
        api.publishChannelMessage(channelId2, message2);
        api.getApiClient().setApiKey(sessionToken1);
        api.publishChannelMessage(channelId, message3);
        api.getApiClient().setApiKey(sessionToken2);
        api.publishChannelMessage(channelId, message4);
        api.publishChannelMessage(channelId, nonPersistMsg);

        List<ChannelMessage> messages = api.queryChannelMessages(channelId, 2, null, null);
        Assertions.assertEquals(messages.size(), 2);
        validateMessage(messages.get(0), USER_ID_1, "userA", channelId, "***", message3.getData());
        validateMessage(messages.get(1), USER_ID_2, "userB", channelId, message4.getBody(), message4.getData());

        messages = api.queryChannelMessages(channelId, 5, messages.get(0).getId(), null);
        validateMessage(messages.get(0), USER_ID_1, "userA", channelId, "***", message1.getData());

        messages = api.queryChannelMessages(channelId, 3, null, messages.get(0).getId());
        Assertions.assertEquals(messages.size(), 2);
        validateMessage(messages.get(0), USER_ID_1, "userA", channelId, "***", message3.getData());
        validateMessage(messages.get(1), USER_ID_2, "userB", channelId, message4.getBody(), message4.getData());
    }

    @Test
    @DisplayName("it should translate messages")
    void messageTranslationTest() throws ApiException {
        final String channel = UUID.randomUUID().toString();
        final ChannelApi api = new ChannelApi(new ApiClient());

        api.getApiClient().setApiKey(sessionToken1);
        api.addChannelSubscriber(channel);

        api.getApiClient().setApiKey(sessionToken2);
        api.addChannelSubscriber(channel);

        final ChannelMessageModel message = buildMessage("userA","hello1", "data1", true);
        message.setTranslate(true);
        api.getApiClient().setApiKey(sessionToken1);
        api.publishChannelMessage(channel, message);

        List<ChannelMessage> messages = api.queryChannelMessages(channel, 1, null, null);
        Assertions.assertEquals(messages.size(), 1);
        Assertions.assertEquals(messages.get(0).getTranslations().size(), 2);
        Assertions.assertEquals(messages.get(0).getTranslations().get(LanguageCode.ES.toString()).compareToIgnoreCase("hello1_en_es"), 0);

        message.setTranslate(false);
        api.getApiClient().setApiKey(sessionToken1);
        api.publishChannelMessage(channel, message);

        messages = api.queryChannelMessages(channel, 1, null, null);
        Assertions.assertEquals(0, messages.get(0).getTranslations().size());
    }

    @Test
    @DisplayName("it should translate messages")
    void messageTranslationTestWithLocale() throws ApiException {
        final String channel = UUID.randomUUID().toString();
        final ChannelApi api = new ChannelApi(new ApiClient());

        api.getApiClient().setApiKey(sessionToken1);
        api.addChannelSubscriber(channel);

        api.getApiClient().setApiKey(sessionToken2);
        api.addChannelSubscriber(channel);

        final ChannelMessageModel message = buildMessage("userA","hello1", "data1", true);
        message.setTranslate(true);
        message.setLocale(LanguageCode.FR);
        api.getApiClient().setApiKey(sessionToken1);
        api.publishChannelMessage(channel, message);

        List<ChannelMessage> messages = api.queryChannelMessages(channel, 1, null, null);
        Assertions.assertEquals(messages.size(), 1);
        Assertions.assertEquals(messages.get(0).getTranslations().size(), 2);
        Assertions.assertEquals(messages.get(0).getTranslations().get(LanguageCode.ES.toString()).compareToIgnoreCase("hello1_fr_es"), 0);

        message.setTranslate(false);
        api.getApiClient().setApiKey(sessionToken1);
        api.publishChannelMessage(channel, message);

        messages = api.queryChannelMessages(channel, 1, null, null);
        Assertions.assertEquals(0, messages.get(0).getTranslations().size());
    }

    @Test
    @DisplayName("it should update a message")
    void messageUpdateTest() throws ApiException {
        final String channel = UUID.randomUUID().toString();
        final ChannelApi api = new ChannelApi(new ApiClient());
        final ChannelMessageModel message = buildMessage("userA","message1", "data1", true);

        api.getApiClient().setApiKey(sessionToken1);
        api.publishChannelMessage(channel, message);

        List<ChannelMessage> messages = api.queryChannelMessages(channel, 1, null, null);
        Assertions.assertEquals(messages.size(), 1);

        final UpdateChannelMessageRequest updatedMessageRequest = buildUpdateMessageRequest("userA2","message12", "data12");
        updatedMessageRequest.setTranslate(true);
        api.updateChannelMessage(channel, messages.get(0).getId().toString(), updatedMessageRequest);

        messages = api.queryChannelMessages(channel, 1, null, null);
        Assertions.assertEquals(messages.size(), 1);
        validateMessage(
                messages.get(0),
                USER_ID_1,
                updatedMessageRequest.getNick(),
                channel,
                updatedMessageRequest.getBody(),
                updatedMessageRequest.getData()
        );
    }

    @Test
    @DisplayName("it should update a message")
    void messageUpdateTestTranslationWithoutLocale() throws ApiException {
        final String channel = UUID.randomUUID().toString();
        final ChannelApi api = new ChannelApi(new ApiClient());

        final ChannelMessageModel message = buildMessage("userA","message1", "data1", true);
        message.setLocale(LanguageCode.FR);

        api.getApiClient().setApiKey(sessionToken2);
        api.addChannelSubscriber(channel);

        api.getApiClient().setApiKey(sessionToken1);
        api.addChannelSubscriber(channel);
        api.publishChannelMessage(channel, message);

        List<ChannelMessage> messages = api.queryChannelMessages(channel, 1, null, null);
        Assertions.assertEquals(messages.size(), 1);

        final UpdateChannelMessageRequest updatedMessageRequest = buildUpdateMessageRequest("userA2","Hello1", "data12");
        updatedMessageRequest.setTranslate(true);
        api.updateChannelMessage(channel, messages.get(0).getId().toString(), updatedMessageRequest);

        messages = api.queryChannelMessages(channel, 1, null, null);
        Assertions.assertEquals(messages.size(), 1);
        Assertions.assertEquals(messages.get(0).getTranslations().size(), 2);
        Assertions.assertEquals(messages.get(0).getTranslations().get(LanguageCode.ES.toString()).compareToIgnoreCase("hello1_en_es"), 0);
    }

    @Test
    @DisplayName("it should update a message")
    void messageUpdateTestTranslationWithLocale() throws ApiException {
        final String channel = UUID.randomUUID().toString();
        final ChannelApi api = new ChannelApi(new ApiClient());

        final ChannelMessageModel message = buildMessage("userA","message1", "data1", true);

        api.getApiClient().setApiKey(sessionToken2);
        api.addChannelSubscriber(channel);

        api.getApiClient().setApiKey(sessionToken1);
        api.addChannelSubscriber(channel);
        api.publishChannelMessage(channel, message);

        List<ChannelMessage> messages = api.queryChannelMessages(channel, 1, null, null);
        Assertions.assertEquals(messages.size(), 1);

        final UpdateChannelMessageRequest updatedMessageRequest = buildUpdateMessageRequest("userA2","Hello1", "data12");
        updatedMessageRequest.setTranslate(true);
        updatedMessageRequest.setLocale(LanguageCode.FR);
        api.updateChannelMessage(channel, messages.get(0).getId().toString(), updatedMessageRequest);

        messages = api.queryChannelMessages(channel, 1, null, null);
        Assertions.assertEquals(messages.size(), 1);
        Assertions.assertEquals(messages.get(0).getTranslations().size(), 2);
        Assertions.assertEquals(messages.get(0).getTranslations().get(LanguageCode.ES.toString()).compareToIgnoreCase("hello1_fr_es"), 0);
    }

    @Test
    @DisplayName("it should send an error when updating a non-existent message")
    void nonexistentMessageUpdateTest() {
        final String channel = UUID.randomUUID().toString();
        final ChannelApi api = new ChannelApi(new ApiClient());
        final UpdateChannelMessageRequest updateMessageRequest = buildUpdateMessageRequest("userA", "message1", "data1");

        Assertions.assertThrows(ApiException.class, () -> {
            api.getApiClient().setApiKey(sessionToken1);
            api.updateChannelMessage(channel, Long.toString(1001L), updateMessageRequest);
        });
    }

    @Test
    @DisplayName("it should delete a message")
    void messageDeletionTest() throws ApiException {
        final String channel = UUID.randomUUID().toString();
        final ChannelApi api = new ChannelApi(new ApiClient());
        final ChannelMessageModel message = buildMessage("userA","message1", "data1", true);

        api.getApiClient().setApiKey(sessionToken1);
        api.publishChannelMessage(channel, message);

        List<ChannelMessage> messages = api.queryChannelMessages(channel, 1, null, null);
        Assertions.assertEquals(messages.size(), 1);

        api.deleteChannelMessage(channel, messages.get(0).getId().toString());
        messages = api.queryChannelMessages(channel, 1, null, null);
        Assertions.assertEquals(messages.size(), 0);
    }

    @Test
    @DisplayName("it should send an error when deleting a non-existent message")
    void nonexistentMessageDeletionTest() {
        final String channel = UUID.randomUUID().toString();
        final ChannelApi api = new ChannelApi(new ApiClient());

        Assertions.assertThrows(ApiException.class, () -> {
            api.getApiClient().setApiKey(sessionToken1);
            api.deleteChannelMessage(channel, Long.toString(1001L));
        });
    }

    @Test
    @DisplayName("it should not allow a muted user to publish message")
    void mutedUserTest() throws Exception {
        final ChannelApi api = new ChannelApi(new ApiClient());
        final String channel = UUID.randomUUID().toString();
        ChannelMessageModel message1 = buildMessage("userA","message1", "data1", true);
        UpdateChannelMessageRequest updateMessageRequest = new UpdateChannelMessageRequest();
        updateMessageRequest.setBody("updated message");

        // publish message before banning to test edit, delete etc
        api.getApiClient().setApiKey(sessionToken1);
        api.publishChannelMessage(channel, message1);

        // mute user
        final MuteUserRequest muteUserRequest = new MuteUserRequest();
        muteUserRequest.setUserId(USER_ID_1);

        validateErrorResponse(() -> api.muteChannelUser(channel, muteUserRequest), 403);

        api.getApiClient().setApiKey(adminToken);
        api.muteChannelUser(channel, muteUserRequest);

        // validate ops after mute
        api.getApiClient().setApiKey(sessionToken1);

        List<ChannelMessage> messages = api.queryChannelMessages(channel, 5, null, null);
        Assertions.assertEquals(messages.size(), 1);
        String messageId = messages.get(0).getId().toString();

        api.addChannelSubscriber(channel);
        validateErrorResponse(() -> api.publishChannelMessage(channel, message1), 403);
        validateErrorResponse(() -> api.updateChannelMessage(channel, messageId, updateMessageRequest), 403);
        validateErrorResponse(() -> api.deleteChannelMessage(channel, messageId), 403);
        api.removeChannelSubscriber(channel);

        // unmute user
        api.getApiClient().setApiKey(adminToken);
        api.unmuteChannelUser(channel, USER_ID_1);

        // validate ops after unmute
        api.getApiClient().setApiKey(sessionToken1);
        api.publishChannelMessage(channel, message1);
        api.updateChannelMessage(channel, messageId, updateMessageRequest);
        api.deleteChannelMessage(channel, messageId);
    }

    @Test
    @DisplayName("it should allow a user to publish message whose mute has expired")
    void timedMuteUserTest() throws Exception {
        final ChannelApi api = new ChannelApi(new ApiClient());
        final String channel = UUID.randomUUID().toString();
        ChannelMessageModel message1 = buildMessage("userA","message1", "data1", true);
        UpdateChannelMessageRequest updateMessageRequest = new UpdateChannelMessageRequest();
        updateMessageRequest.setBody("updated message");

        // publish message before banning to test edit, delete etc
        api.getApiClient().setApiKey(sessionToken1);
        api.publishChannelMessage(channel, message1);

        List<ChannelMessage> messages = api.queryChannelMessages(channel, 5, null, null);
        Assertions.assertEquals(messages.size(), 1);
        String messageId = messages.get(0).getId().toString();

        // mute user
        final MuteUserRequest muteUserRequest = new MuteUserRequest();
        muteUserRequest.setUserId(USER_ID_1);
        muteUserRequest.setDuration(BigDecimal.ONE);
        api.getApiClient().setApiKey(adminToken);
        api.muteChannelUser(channel, muteUserRequest);

        // wait for mute to expire
        TimeUnit.SECONDS.sleep(2);

        // validate ops after unmute
        api.getApiClient().setApiKey(sessionToken1);
        api.publishChannelMessage(channel, message1);
        api.updateChannelMessage(channel, messageId, updateMessageRequest);

        messages = api.queryChannelMessages(channel, 5, null, null);
        Assertions.assertEquals(messages.size(), 2);

        api.deleteChannelMessage(channel, messageId);
    }


    @Test
    @DisplayName("it should not allow a banned user to publish message")
    void bannedUserTest() throws Exception {
        final ChannelApi api = new ChannelApi(new ApiClient());
        final String channel = UUID.randomUUID().toString();
        ChannelMessageModel message1 = buildMessage("userA","message1", "data1", true);
        UpdateChannelMessageRequest updateMessageRequest = new UpdateChannelMessageRequest();
        updateMessageRequest.setBody("updated message");

        // publish message before banning to test edit, delete etc
            api.getApiClient().setApiKey(sessionToken1);
        api.publishChannelMessage(channel, message1);

        List<ChannelMessage> messages = api.queryChannelMessages(channel, 5, null, null);
        Assertions.assertEquals(messages.size(), 1);
        String messageId = messages.get(0).getId().toString();

        // add user ban
        final AddUserBanRequest addUserBanRequest = new AddUserBanRequest();
        addUserBanRequest.setUserId(USER_ID_1);
        api.getApiClient().setApiKey(adminToken);
        api.addUserBan(channel, addUserBanRequest);

        // perform forbidden ops
        api.getApiClient().setApiKey(sessionToken1);
        validateErrorResponse(() -> api.queryChannelMessages(channel, 5, null, null), 403);
        validateErrorResponse(() -> api.publishChannelMessage(channel, message1), 403);
        validateErrorResponse(() -> api.updateChannelMessage(channel, messageId, updateMessageRequest), 403);
        validateErrorResponse(() -> api.deleteChannelMessage(channel, messageId), 403);
        validateErrorResponse(() -> api.addChannelSubscriber(channel), 403);
        validateErrorResponse(() -> api.removeChannelSubscriber(channel), 403);

        // remove user ban
        api.getApiClient().setApiKey(adminToken);
        api.removeUserBan(channel, USER_ID_1);

        // perform allowed ops
        api.getApiClient().setApiKey(sessionToken1);
        api.publishChannelMessage(channel, message1);
        api.updateChannelMessage(channel, messageId, updateMessageRequest);
        api.deleteChannelMessage(channel, messageId);
    }

    @Test
    @DisplayName("it should not allow a user to publish message whose ban has expired")
    void timedBannedUserTest() throws Exception {
        final ChannelApi api = new ChannelApi(new ApiClient());
        final String channel = UUID.randomUUID().toString();
        ChannelMessageModel message1 = buildMessage("userA","message1", "data1", true);
        UpdateChannelMessageRequest updateMessageRequest = new UpdateChannelMessageRequest();
        updateMessageRequest.setBody("updated message");

        // publish message before banning to test edit, delete etc
        api.getApiClient().setApiKey(sessionToken1);
        api.publishChannelMessage(channel, message1);

        List<ChannelMessage> messages = api.queryChannelMessages(channel, 5, null, null);
        Assertions.assertEquals(messages.size(), 1);
        String messageId = messages.get(0).getId().toString();

        // add user ban
        final AddUserBanRequest addUserBanRequest = new AddUserBanRequest();
        addUserBanRequest.setUserId(USER_ID_1);
        addUserBanRequest.setDuration(BigDecimal.ONE);
        api.getApiClient().setApiKey(adminToken);
        api.addUserBan(channel, addUserBanRequest);

        // wait for ban to expire
        TimeUnit.SECONDS.sleep(2);

        // perform allowed ops
        api.getApiClient().setApiKey(sessionToken1);
        api.publishChannelMessage(channel, message1);
        api.updateChannelMessage(channel, messageId, updateMessageRequest);
        api.deleteChannelMessage(channel, messageId);
    }

    @Test
    @DisplayName("it should allow an admin to delete message")
    void adminMessageDeleteTest() throws ApiException {
        final ChannelApi api = new ChannelApi(new ApiClient());
        final String channel = UUID.randomUUID().toString();
        final ChannelMessageModel message1 = buildMessage("userA","message1", "data1", true);

        api.getApiClient().setApiKey(sessionToken1);
        api.publishChannelMessage(channel, message1);

        List<ChannelMessage> messages = api.queryChannelMessages(channel, 5, null, null);
        Assertions.assertEquals(messages.size(), 1);
        String messageId = messages.get(0).getId().toString();

        api.getApiClient().setApiKey(adminToken);
        api.deleteChannelMessage(channel, messageId);

        messages = api.queryChannelMessages(channel, 5, null, null);
        Assertions.assertEquals(messages.size(), 0);
    }

    @Test
    @DisplayName("it should allow admin to fetch all topics")
    void validFetchTopicsTest() throws Throwable {
        final String channel = "channelA";
        final TopicApi client = new TopicApi(new ApiClient());
        TestUtils.signV2(
                client.getApiClient(),
                APP_ID,
                "GET",
                "/v1/channels/"+channel+"/topics",
                "",
                "secret"
        );

        QueryChannelTopicsResponse topics = client.queryChannelTopics(channel, USER_ADMIN);
        Assertions.assertNotNull(topics);
        Assertions.assertEquals(2, topics.size());

        final String topicA = topics.get(0);
        final String topicB = topics.get(1);

        Assertions.assertTrue(topicA.equals("topic1") || topicA.equals("topic2"));
        Assertions.assertTrue(topicB.equals("topic1") || topicB.equals("topic2"));
    }

    @Test
    @DisplayName("it should not allow non-admin to fetch topics")
    void invalidFetchTopicsTest() throws Exception {
        final String channel = "channelA";
        final TopicApi client = new TopicApi(new ApiClient());
        TestUtils.signV2(
                client.getApiClient(),
                APP_ID,
                "GET",
                "/v1/channels/"+channel+"/topics",
                "",
                "secret"
        );

        validateErrorResponse(() -> client.queryChannelTopics(channel, USER_ID_1), 403);
    }

    @Test
    @DisplayName("it should allow admin to fetch topic messages")
    void validFetchTopicMessagesTest() throws Throwable {
        final TopicApi topicApi = new TopicApi(new ApiClient());
        final ChannelApi channelApi = new ChannelApi(new ApiClient());
        final String channel = "channel123";

        // publish message
        ChannelMessageModel message1 = buildMessage("userA","message1", "data1", true);
        channelApi.getApiClient().setApiKey(sessionToken1);
        channelApi.publishChannelMessage(channel, message1);

        // Fetch topic messages
        TestUtils.signV2(
                topicApi.getApiClient(),
                APP_ID,
                "GET",
                "/v1/channels/"+channel+"/topics/"+channel+"/messages",
                "",
                "secret"
        );

        List<ChannelMessage> messages = topicApi.queryChannelTopicMessages(channel, channel, USER_ID_1, 5, null, null);
        Assertions.assertEquals(messages.size(), 1);

        messages = topicApi.queryChannelTopicMessages(channel, channel, USER_ADMIN, 5, null, null);
        Assertions.assertEquals(messages.size(), 1);
    }

    @Test
    @DisplayName("it should not allow banned user to fetch topic messages")
    void invalidFetchTopicMessagesTest() throws Exception {
        ChannelApi channelApi = new ChannelApi(new ApiClient());
        final String channel = "channel456";

        // publish message
        channelApi.getApiClient().setApiKey(sessionToken1);
        ChannelMessageModel message1 = buildMessage("userA","message1", "data1", true);
        channelApi.publishChannelMessage(channel, message1);

        String bannerUserId = "bannedUser";
        final AddUserBanRequest addUserBanRequest = new AddUserBanRequest();
        addUserBanRequest.setUserId(bannerUserId);
        channelApi.getApiClient().setApiKey(adminToken);
        channelApi.addUserBan(channel, addUserBanRequest);

        TopicApi topicApi = new TopicApi(new ApiClient());
        TestUtils.signV2(
                topicApi.getApiClient(),
                APP_ID,
                "GET",
                "/v1/channels/"+channel+"/topics/"+channel+"/messages",
                "",
                "secret"
        );
        validateErrorResponse(() -> topicApi.queryChannelTopicMessages(channel, channel, bannerUserId, 5, null, null), 403);
    }

    @Test
    @DisplayName("it should allow user to publish topic messages")
    void validPublishTopicMessagesTest() throws Throwable {
        final TopicApi topicApi = new TopicApi(new ApiClient());
        final String channel = "channel321";

        // publish topic message
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("from", USER_ID_1);
        jsonObject.addProperty("nick", USER_ID_1);
        jsonObject.addProperty("body", "message1");
        jsonObject.addProperty("data", "data1");
        jsonObject.addProperty("persist", true);

        TestUtils.signV2(
                topicApi.getApiClient(),
                APP_ID,
                "POST",
                "/v1/channels/"+channel+"/topics/"+channel+"/messages",
                jsonObject.toString(),
                "secret"
        );
        topicApi.publishChannelTopicMessage(channel, channel, jsonObject);

        // Verify topic message
        TestUtils.signV2(
                topicApi.getApiClient(),
                APP_ID,
                "GET",
                "/v1/channels/"+channel+"/topics/"+channel+"/messages",
                "",
                "secret"
        );
        String usedId = "user12";
        List<ChannelMessage> messages = topicApi.queryChannelTopicMessages(channel, channel, usedId, 5, null, null);
        Assertions.assertEquals(messages.size(), 1);

        messages = topicApi.queryChannelTopicMessages(channel, channel, USER_ADMIN, 5, null, null);
        Assertions.assertEquals(messages.size(), 1);
    }

    @Test
    @DisplayName("it should not allow banned user to publish topic messages")
    void invalidPublishTopicMessagesTest() throws Exception {
        ChannelApi channelApi = new ChannelApi(new ApiClient());
        final String channel = "channel1234";

        // Ban user
        String bannerUserId = "bannedUser";
        final AddUserBanRequest addUserBanRequest = new AddUserBanRequest();
        addUserBanRequest.setUserId(bannerUserId);
        channelApi.getApiClient().setApiKey(adminToken);
        channelApi.addUserBan(channel, addUserBanRequest);

        // Verify topic publish message
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("from", bannerUserId);
        jsonObject.addProperty("nick", bannerUserId);
        jsonObject.addProperty("body", "message1");
        jsonObject.addProperty("data", "data1");
        jsonObject.addProperty("persist", true);
        TopicApi topicApi = new TopicApi(new ApiClient());
        TestUtils.signV2(
                topicApi.getApiClient(),
                APP_ID,
                "POST",
                "/v1/channels/"+channel+"/topics/"+channel+"/messages",
                jsonObject.toString(),
                "secret"
        );
        ;
        validateErrorResponse(() -> topicApi.publishChannelTopicMessage(channel, channel, jsonObject), 403);
    }

    private void validateMessage(final ChannelMessage aMessage,
                                 final String aFrom,
                                 final String aNick,
                                 final String aTo,
                                 final String aBody,
                                 final String aData) {
        Assertions.assertEquals(aFrom, aMessage.getFrom());
        Assertions.assertEquals(aNick, aMessage.getNick());
        Assertions.assertEquals(aTo, aMessage.getTo());
        Assertions.assertEquals(aData, aMessage.getData());
        Assertions.assertEquals(aBody, aMessage.getBody());
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
