package io.fizz.gateway.http.controllers;

import com.google.gson.Gson;
import io.fizz.gateway.Constants;
import io.fizz.gateway.http.MockApplication;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.AuthApi;
import io.swagger.client.api.ExplorationApi;
import io.swagger.client.api.ModerationApi;
import io.swagger.client.model.*;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
public class ChatModerationControllerTest {
    private static Vertx vertx;
    private static String sessionToken;
    private static final String APP_ID = "appA";
    private static final String USER_ID_1 = "user1";
    private static final long TIME_START = 1553510320;
    private static final long TIME_END = 1553510380;

    static private Future<String> deployVertex() {
        Future<String> future = Future.future();
        vertx.deployVerticle(MockApplication.class.getName(), future);
        return future;
    }

    @BeforeAll
    static void setUp(final VertxTestContext aContext) throws InterruptedException {
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
    static void tearDown(final VertxTestContext aContext) {
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

    @Nested
    @DisplayName("Report Message Test Suit")
    class MessagesReportTestSuite {
        @Nested
        @DisplayName("Report Message Positive Test Suit")
        class MessagesQueryPositiveTestSuite {
            @Test
            @DisplayName("it should report text message with success response")
            void reportMessageTest() throws ApiException {
                final ModerationApi api = new ModerationApi(new ApiClient());
                api.getApiClient().setApiKey(sessionToken);

                ReportMessage rm = new ReportMessage()
                        .channelId("channel_1")
                        .description("This is test reporting")
                        .language(LanguageCode.EN)
                        .message("This is just a Test Message")
                        .messageId("message_1")
                        .offense(Offense.HARASSMENT)
                        .time(BigDecimal.valueOf(new Date().getTime()/1000))
                        .userId("user_1");

                ReportMessageResponse result = api.reportMessage(rm);
                Assertions.assertEquals("Ognaeeo9kb3WO80KFD/u3Q==", result.getId());
            }

            @Test
            @DisplayName("it should report text message with missing description")
            void reportMessageMissingDescriptionTest() throws ApiException {
                final ModerationApi api = new ModerationApi(new ApiClient());
                api.getApiClient().setApiKey(sessionToken);

                ReportMessage rm = new ReportMessage()
                        .channelId("channel_1")
                        .description("This is test reporting")
                        .language(LanguageCode.EN)
                        .message("This is just a Test Message")
                        .messageId("message_1")
                        .offense(Offense.HARASSMENT)
                        .time(BigDecimal.valueOf(new Date().getTime()/1000))
                        .userId("user_1");

                ReportMessageResponse result = api.reportMessage(rm);
                Assertions.assertEquals("Ognaeeo9kb3WO80KFD/u3Q==", result.getId());
            }
        }

        @Nested
        @DisplayName("Report Message Negative Test Suit")
        class MessagesQueryNegativeTestSuite {
            @Test
            @DisplayName("it should not report text message with missing channel Id")
            void reportMessageMissingChannelIdTest() {
                final ModerationApi api = new ModerationApi(new ApiClient());
                api.getApiClient().setApiKey(sessionToken);

                Assertions.assertThrows(ApiException.class, () -> {
                    try {
                        ReportMessage rm = new ReportMessage()
                                .description("This is test reporting")
                                .language(LanguageCode.EN)
                                .message("This is just a Test Message")
                                .messageId("message_1")
                                .offense(Offense.HARASSMENT)
                                .time(BigDecimal.valueOf(new Date().getTime()/1000))
                                .userId("user_1");

                        api.reportMessage(rm);
                    } catch (ApiException ex) {
                        Assertions.assertEquals(400, ex.getCode());
                        Assertions.assertEquals("{\"reason\":\"invalid_channel_id\"}", ex.getResponseBody());
                        throw ex;
                    }
                });
            }

            @Test
            @DisplayName("it should not report text message with missing language code")
            void reportMessageMissingLanguageTest() {
                final ModerationApi api = new ModerationApi(new ApiClient());
                api.getApiClient().setApiKey(sessionToken);

                Assertions.assertThrows(ApiException.class, () -> {
                    try {
                        ReportMessage rm = new ReportMessage()
                                .channelId("channel_1")
                                .description("This is test reporting")
                                .message("This is just a Test Message")
                                .messageId("message_1")
                                .offense(Offense.HARASSMENT)
                                .time(BigDecimal.valueOf(new Date().getTime()/1000))
                                .userId("user_1");

                        api.reportMessage(rm);
                    } catch (ApiException ex) {
                        Assertions.assertEquals(400, ex.getCode());
                        Assertions.assertEquals("{\"reason\":\"invalid_language_code_null\"}", ex.getResponseBody());
                        throw ex;
                    }
                });
            }

            @Test
            @DisplayName("it should not report text message with missing message")
            void reportMessageMissingMessageTest() {
                final ModerationApi api = new ModerationApi(new ApiClient());
                api.getApiClient().setApiKey(sessionToken);

                Assertions.assertThrows(ApiException.class, () -> {
                    try {
                        ReportMessage rm = new ReportMessage()
                                .channelId("channel_1")
                                .description("This is test reporting")
                                .language(LanguageCode.EN)
                                .messageId("message_1")
                                .offense(Offense.HARASSMENT)
                                .time(BigDecimal.valueOf(new Date().getTime()/1000))
                                .userId("user_1");

                        api.reportMessage(rm);
                    } catch (ApiException ex) {
                        Assertions.assertEquals(400, ex.getCode());
                        Assertions.assertEquals("{\"reason\":\"invalid_message\"}", ex.getResponseBody());
                        throw ex;
                    }
                });
            }

            @Test
            @DisplayName("it should not report text message with missing message id")
            void reportMessageMissingMessageIdTest() {
                final ModerationApi api = new ModerationApi(new ApiClient());
                api.getApiClient().setApiKey(sessionToken);

                Assertions.assertThrows(ApiException.class, () -> {
                    try {
                        ReportMessage rm = new ReportMessage()
                                .channelId("channel_1")
                                .description("This is test reporting")
                                .language(LanguageCode.EN)
                                .message("This is just a Test Message")
                                .offense(Offense.HARASSMENT)
                                .time(BigDecimal.valueOf(new Date().getTime()/1000))
                                .userId("user_1");

                        api.reportMessage(rm);
                    } catch (ApiException ex) {
                        Assertions.assertEquals(400, ex.getCode());
                        Assertions.assertEquals("{\"reason\":\"invalid_message_id\"}", ex.getResponseBody());
                        throw ex;
                    }
                });
            }

            @Test
            @DisplayName("it should not report text message with missing Offense")
            void reportMessageMissingOffenseTest() {
                final ModerationApi api = new ModerationApi(new ApiClient());
                api.getApiClient().setApiKey(sessionToken);

                Assertions.assertThrows(ApiException.class, () -> {
                    try {
                        ReportMessage rm = new ReportMessage()
                                .channelId("channel_1")
                                .description("This is test reporting")
                                .language(LanguageCode.EN)
                                .message("This is just a Test Message")
                                .messageId("message_1")
                                .time(BigDecimal.valueOf(new Date().getTime()/1000))
                                .userId("user_1");

                        api.reportMessage(rm);
                    } catch (ApiException ex) {
                        Assertions.assertEquals(400, ex.getCode());
                        Assertions.assertEquals("{\"reason\":\"invalid_offense\"}", ex.getResponseBody());
                        throw ex;
                    }
                });
            }

            @Test
            @DisplayName("it should not report text message with missing user id")
            void reportMessageMissingUserIdTest() {
                final ModerationApi api = new ModerationApi(new ApiClient());
                api.getApiClient().setApiKey(sessionToken);

                Assertions.assertThrows(ApiException.class, () -> {
                    try {
                        ReportMessage rm = new ReportMessage()
                                .channelId("channel_1")
                                .description("This is test reporting")
                                .language(LanguageCode.EN)
                                .message("This is just a Test Message")
                                .messageId("message_1")
                                .offense(Offense.HARASSMENT)
                                .time(BigDecimal.valueOf(new Date().getTime()/1000));

                        api.reportMessage(rm);
                    } catch (ApiException ex) {
                        Assertions.assertEquals(400, ex.getCode());
                        Assertions.assertEquals("{\"reason\":\"invalid_user_id\"}", ex.getResponseBody());
                        throw ex;
                    }
                });
            }
        }
    }

    @Nested
    @DisplayName("Report Query Test Suit")
    class MessagesQueryTestSuite {
        @Nested
        @DisplayName("Report Query Test Positive Suit")
        class MessagesQueryPositiveTestSuite {
            @Test
            @DisplayName("it should search reported messages")
            void queryReportedMessageTest() throws ApiException {
                final ExplorationApi api = new ExplorationApi(new ApiClient());
                api.getApiClient().setApiKey(sessionToken);

                SearchReportMessage srm = new SearchReportMessage()
                        .end(BigDecimal.valueOf(TIME_END))
                        .start(BigDecimal.valueOf(TIME_START));

                SearchReportMessageResult result = api.searchReportMessage(srm);

                Assertions.assertEquals(3, result.getTotalSize().intValue());
                Assertions.assertEquals(3, result.getData().size());
            }

            @Test
            @DisplayName("it should search reported messages with cursor")
            void queryReportedMessageCursorTest() throws ApiException {
                final ExplorationApi api = new ExplorationApi(new ApiClient());
                api.getApiClient().setApiKey(sessionToken);

                SearchReportMessage srm = new SearchReportMessage()
                        .end(BigDecimal.valueOf(TIME_END))
                        .start(BigDecimal.valueOf(TIME_START))
                        .cursor(new BigDecimal(2));

                SearchReportMessageResult result = api.searchReportMessage(srm);

                Assertions.assertEquals(1, result.getTotalSize().intValue());
                Assertions.assertEquals(1, result.getData().size());

                SearchReportMessageResultData data = result.getData().get(0);
                Assertions.assertEquals("id_3", data.getId());
                Assertions.assertEquals("channel_a", data.getChannelId());
                Assertions.assertEquals("message_id_3", data.getMessageId());
                Assertions.assertEquals("test_message_3", data.getMessage());
                Assertions.assertEquals("fr", data.getLanguage().getValue());
                Assertions.assertEquals("user_4", data.getUserId());
                Assertions.assertEquals("user_3", data.getReporterId());
                Assertions.assertEquals("Other", data.getOffense().getValue());
                Assertions.assertEquals("this is mocked message 3", data.getDescription());
                Assertions.assertEquals(TIME_END, data.getTime().longValue());
            }

            @Test
            @DisplayName("it should search reported messages with page size")
            void queryReportedMessagePageSizeTest() throws ApiException {
                final ExplorationApi api = new ExplorationApi(new ApiClient());
                api.getApiClient().setApiKey(sessionToken);

                SearchReportMessage srm = new SearchReportMessage()
                        .end(BigDecimal.valueOf(TIME_END))
                        .start(BigDecimal.valueOf(TIME_START))
                        .pageSize(new BigDecimal(1));

                SearchReportMessageResult result = api.searchReportMessage(srm);

                Assertions.assertEquals(3, result.getTotalSize().intValue());
                Assertions.assertEquals(1, result.getData().size());

                SearchReportMessageResultData data = result.getData().get(0);
                Assertions.assertEquals("id_1", data.getId());
                Assertions.assertEquals("channel_a", data.getChannelId());
                Assertions.assertEquals("message_id_1", data.getMessageId());
                Assertions.assertEquals("test_message_1", data.getMessage());
                Assertions.assertEquals("en", data.getLanguage().getValue());
                Assertions.assertEquals("user_2", data.getUserId());
                Assertions.assertEquals("user_1", data.getReporterId());
                Assertions.assertEquals("Toxic", data.getOffense().getValue());
                Assertions.assertEquals("this is mocked message 1", data.getDescription());
                Assertions.assertEquals(TIME_START, data.getTime().longValue());
            }

            @Test
            @DisplayName("it should search reported messages with sort order")
            void queryReportedMessageSortTest() throws ApiException {
                final ExplorationApi api = new ExplorationApi(new ApiClient());
                api.getApiClient().setApiKey(sessionToken);

                SearchReportMessage srm = new SearchReportMessage()
                        .end(BigDecimal.valueOf(TIME_END))
                        .start(BigDecimal.valueOf(TIME_START))
                        .sort(SortOrder.ASC);

                SearchReportMessageResult result = api.searchReportMessage(srm);

                Assertions.assertEquals(3, result.getTotalSize().intValue());
                Assertions.assertEquals(3, result.getData().size());

                SearchReportMessageResultData data = result.getData().get(0);
                Assertions.assertEquals("id_3", data.getId());
                Assertions.assertEquals("channel_a", data.getChannelId());
                Assertions.assertEquals("message_id_3", data.getMessageId());
                Assertions.assertEquals("test_message_3", data.getMessage());
                Assertions.assertEquals("fr", data.getLanguage().getValue());
                Assertions.assertEquals("user_4", data.getUserId());
                Assertions.assertEquals("user_3", data.getReporterId());
                Assertions.assertEquals("Other", data.getOffense().getValue());
                Assertions.assertEquals("this is mocked message 3", data.getDescription());
                Assertions.assertEquals(TIME_END, data.getTime().longValue());
            }

            @Test
            @DisplayName("it should search reported messages with user id")
            void queryReportedMessageFilerUserTest() throws ApiException {
                final ExplorationApi api = new ExplorationApi(new ApiClient());
                api.getApiClient().setApiKey(sessionToken);

                SearchReportMessage srm = new SearchReportMessage()
                        .end(BigDecimal.valueOf(TIME_END))
                        .start(BigDecimal.valueOf(TIME_START))
                        .userId("user_2");

                SearchReportMessageResult result = api.searchReportMessage(srm);

                Assertions.assertEquals(2, result.getTotalSize().intValue());
                Assertions.assertEquals(2, result.getData().size());
            }

            @Test
            @DisplayName("it should search reported messages with channel id")
            void queryReportedMessageFilerChannelTest() throws ApiException {
                final ExplorationApi api = new ExplorationApi(new ApiClient());
                api.getApiClient().setApiKey(sessionToken);

                SearchReportMessage srm = new SearchReportMessage()
                        .end(BigDecimal.valueOf(TIME_END))
                        .start(BigDecimal.valueOf(TIME_START))
                        .channelId("channel_a");

                SearchReportMessageResult result = api.searchReportMessage(srm);

                Assertions.assertEquals(2, result.getTotalSize().intValue());
                Assertions.assertEquals(2, result.getData().size());
            }

            @Test
            @DisplayName("it should search reported messages with channel id")
            void queryReportedMessageFilerLanguageTest() throws ApiException {
                final ExplorationApi api = new ExplorationApi(new ApiClient());
                api.getApiClient().setApiKey(sessionToken);

                SearchReportMessage srm = new SearchReportMessage()
                        .end(BigDecimal.valueOf(TIME_END))
                        .start(BigDecimal.valueOf(TIME_START))
                        .language(LanguageCode.EN);

                SearchReportMessageResult result = api.searchReportMessage(srm);

                Assertions.assertEquals(2, result.getTotalSize().intValue());
                Assertions.assertEquals(2, result.getData().size());
            }
        }

        @Nested
        @DisplayName("Report Query Test Negative Suit")
        class MessagesQueryNegativeTestSuite {
            @Test
            @DisplayName("it should give error on missing start time")
            void queryReportedMessageMissingStartTimeTest() {
                final ExplorationApi api = new ExplorationApi(new ApiClient());
                api.getApiClient().setApiKey(sessionToken);

                Assertions.assertThrows(ApiException.class, () -> {
                    try {
                        SearchReportMessage srm = new SearchReportMessage()
                                .end(BigDecimal.valueOf(TIME_END));
                        api.searchReportMessage(srm);
                    } catch (ApiException ex) {
                        Assertions.assertEquals(400, ex.getCode());
                        Assertions.assertEquals("{\"reason\":\"invalid_query_start\"}", ex.getResponseBody());
                        throw ex;
                    }
                });
            }

            @Test
            @DisplayName("it should give error on missing end time")
            void queryReportedMessageMissingEndTimeTest() {
                final ExplorationApi api = new ExplorationApi(new ApiClient());
                api.getApiClient().setApiKey(sessionToken);

                Assertions.assertThrows(ApiException.class, () -> {
                    try {
                        SearchReportMessage srm = new SearchReportMessage()
                                .start(BigDecimal.valueOf(TIME_START));
                        api.searchReportMessage(srm);
                    } catch (ApiException ex) {
                        Assertions.assertEquals(400, ex.getCode());
                        Assertions.assertEquals("{\"reason\":\"invalid_query_end\"}", ex.getResponseBody());
                        throw ex;
                    }
                });
            }
        }
    }

    @Nested
    @DisplayName("Report User Aggregation Test Suit")
    class MessagesUserAggregationTestSuite {
        @Nested
        @DisplayName("Report User Aggregation Positive Test Suit")
        class MessagesUserAggregationPositiveTestSuite {
            @Test
            @DisplayName("it should return user aggregation of reported message")
            void queryReportedMessageUsersTest() throws ApiException {
                final ExplorationApi api = new ExplorationApi(new ApiClient());
                api.getApiClient().setApiKey(sessionToken);

                SearchReportMessageUser rmu = new SearchReportMessageUser()
                        .end(BigDecimal.valueOf(TIME_END))
                        .start(BigDecimal.valueOf(TIME_START));

                SearchReportMessageUserResultList result = api.searchReportedMessageUsers(rmu);
                Assertions.assertEquals(2, result.size());
                Assertions.assertEquals("user_2", result.get(0).getUserId());
                Assertions.assertEquals(2, result.get(0).getCount().intValue());
                Assertions.assertEquals("user_4", result.get(1).getUserId());
                Assertions.assertEquals(1, result.get(1).getCount().intValue());
            }

            @Test
            @DisplayName("it should return user aggregation of reported message with language code")
            void queryReportedMessageUsersLanguageTest() throws ApiException {
                final ExplorationApi api = new ExplorationApi(new ApiClient());
                api.getApiClient().setApiKey(sessionToken);

                SearchReportMessageUser rmu = new SearchReportMessageUser()
                        .end(BigDecimal.valueOf(TIME_END))
                        .start(BigDecimal.valueOf(TIME_START))
                        .language(LanguageCode.EN);

                SearchReportMessageUserResultList result = api.searchReportedMessageUsers(rmu);

                Assertions.assertEquals(1, result.size());
                Assertions.assertEquals("user_2", result.get(0).getUserId());
                Assertions.assertEquals(2, result.get(0).getCount().intValue());
            }

            @Test
            @DisplayName("it should return user aggregation of reported message with channel id")
            void queryReportedMessageUsersChannelIdTest() throws ApiException {
                final ExplorationApi api = new ExplorationApi(new ApiClient());
                api.getApiClient().setApiKey(sessionToken);

                SearchReportMessageUser rmu = new SearchReportMessageUser()
                        .end(BigDecimal.valueOf(TIME_END))
                        .start(BigDecimal.valueOf(TIME_START))
                        .channelId("channel_a");

                SearchReportMessageUserResultList result = api.searchReportedMessageUsers(rmu);

                Assertions.assertEquals(2, result.size());
                Assertions.assertEquals("user_2", result.get(0).getUserId());
                Assertions.assertEquals(1, result.get(0).getCount().intValue());
                Assertions.assertEquals("user_4", result.get(1).getUserId());
                Assertions.assertEquals(1, result.get(1).getCount().intValue());
            }

            @Test
            @DisplayName("it should return user aggregation of reported message with limit")
            void queryReportedMessageUsersLimitTest() throws ApiException {
                final ExplorationApi api = new ExplorationApi(new ApiClient());
                api.getApiClient().setApiKey(sessionToken);

                SearchReportMessageUser rmu = new SearchReportMessageUser()
                        .end(BigDecimal.valueOf(TIME_END))
                        .start(BigDecimal.valueOf(TIME_START))
                        .limit(new BigDecimal(1));

                SearchReportMessageUserResultList result = api.searchReportedMessageUsers(rmu);

                Assertions.assertEquals(1, result.size());
                Assertions.assertEquals("user_2", result.get(0).getUserId());
                Assertions.assertEquals(1, result.get(0).getCount().intValue());
            }
        }

        @Nested
        @DisplayName("Report User Aggregation Negative Test Suit")
        class MessagesUserAggregationNegativeTestSuite {
            @Test
            @DisplayName("it should not return user aggregation of reported message with missing start time")
            void queryReportedMessageUsersMissingStartTest() {
                final ExplorationApi api = new ExplorationApi(new ApiClient());
                api.getApiClient().setApiKey(sessionToken);

                Assertions.assertThrows(ApiException.class, () -> {
                    try {
                        SearchReportMessageUser rmu = new SearchReportMessageUser()
                                .end(BigDecimal.valueOf(TIME_END));

                        api.searchReportedMessageUsers(rmu);
                    } catch (ApiException ex) {
                        Assertions.assertEquals(400, ex.getCode());
                        Assertions.assertEquals("{\"reason\":\"invalid_query_start\"}", ex.getResponseBody());
                        throw ex;
                    }
                });
            }

            @Test
            @DisplayName("it should not return user aggregation of reported message with missing end time")
            void queryReportedMessageUsersMissingEndTest() {
                final ExplorationApi api = new ExplorationApi(new ApiClient());
                api.getApiClient().setApiKey(sessionToken);

                Assertions.assertThrows(ApiException.class, () -> {
                    try {
                        SearchReportMessageUser rmu = new SearchReportMessageUser()
                                .start(BigDecimal.valueOf(TIME_START));

                        api.searchReportedMessageUsers(rmu);
                    } catch (ApiException ex) {
                        Assertions.assertEquals(400, ex.getCode());
                        Assertions.assertEquals("{\"reason\":\"invalid_query_end\"}", ex.getResponseBody());
                        throw ex;
                    }
                });
            }
        }
    }

    @Nested
    @DisplayName("Report Channel Aggregation Test Suit")
    class MessagesChannelAggregationTestSuite {
        @Nested
        @DisplayName("Report Channel Aggregation Positive Test Suit")
        class MessagesChannelAggregationPositiveTestSuite {
            @Test
            @DisplayName("it should return user aggregation of reported message")
            void queryReportedMessageChannelTest() throws ApiException {
                final ExplorationApi api = new ExplorationApi(new ApiClient());
                api.getApiClient().setApiKey(sessionToken);

                SearchReportMessageChannel rmc = new SearchReportMessageChannel()
                        .end(BigDecimal.valueOf(TIME_END))
                        .start(BigDecimal.valueOf(TIME_START));

                SearchReportMessageChannelResultList result = api.searchReportedMessageChannels(rmc);
                Assertions.assertEquals(2, result.size());
                Assertions.assertEquals("channel_a", result.get(0).getChannelId());
                Assertions.assertEquals(2, result.get(0).getCount().intValue());
                Assertions.assertEquals("channel_b", result.get(1).getChannelId());
                Assertions.assertEquals(1, result.get(1).getCount().intValue());
            }

            @Test
            @DisplayName("it should return channel aggregation of reported message with language code")
            void queryReportedMessageChannelLanguageTest() throws ApiException {
                final ExplorationApi api = new ExplorationApi(new ApiClient());
                api.getApiClient().setApiKey(sessionToken);

                SearchReportMessageChannel rmc = new SearchReportMessageChannel()
                        .end(BigDecimal.valueOf(TIME_END))
                        .start(BigDecimal.valueOf(TIME_START))
                        .language(LanguageCode.EN);

                SearchReportMessageChannelResultList result = api.searchReportedMessageChannels(rmc);
                Assertions.assertEquals(2, result.size());
                Assertions.assertEquals("channel_a", result.get(0).getChannelId());
                Assertions.assertEquals(1, result.get(0).getCount().intValue());
                Assertions.assertEquals("channel_b", result.get(1).getChannelId());
                Assertions.assertEquals(1, result.get(1).getCount().intValue());
            }

            @Test
            @DisplayName("it should return channel aggregation of reported message with Limit")
            void queryReportedMessageChannelLimitTest() throws ApiException {
                final ExplorationApi api = new ExplorationApi(new ApiClient());
                api.getApiClient().setApiKey(sessionToken);

                SearchReportMessageChannel rmc = new SearchReportMessageChannel()
                        .end(BigDecimal.valueOf(TIME_END))
                        .start(BigDecimal.valueOf(TIME_START))
                        .limit(new BigDecimal(1));

                SearchReportMessageChannelResultList result = api.searchReportedMessageChannels(rmc);
                Assertions.assertEquals(1, result.size());
                Assertions.assertEquals("channel_a", result.get(0).getChannelId());
                Assertions.assertEquals(1, result.get(0).getCount().intValue());
            }
        }

        @Nested
        @DisplayName("Report Channel Aggregation Negative Test Suit")
        class MessagesChannelAggregationNegativeTestSuite {
            @Test
            @DisplayName("it should not return channel aggregation of reported message with missing start time")
            void queryReportedMessageChannelMissingStartTest() {
                final ExplorationApi api = new ExplorationApi(new ApiClient());
                api.getApiClient().setApiKey(sessionToken);

                Assertions.assertThrows(ApiException.class, () -> {
                    try {
                        SearchReportMessageChannel rmc = new SearchReportMessageChannel()
                                .end(BigDecimal.valueOf(TIME_END));

                        api.searchReportedMessageChannels(rmc);
                    } catch (ApiException ex) {
                        Assertions.assertEquals(400, ex.getCode());
                        Assertions.assertEquals("{\"reason\":\"invalid_query_start\"}", ex.getResponseBody());
                        throw ex;
                    }
                });
            }

            @Test
            @DisplayName("it should not return channel aggregation of reported message with missing end time")
            void queryReportedMessageChannelMissingEndTest() {
                final ExplorationApi api = new ExplorationApi(new ApiClient());
                api.getApiClient().setApiKey(sessionToken);

                Assertions.assertThrows(ApiException.class, () -> {
                    try {
                        SearchReportMessageChannel rmc = new SearchReportMessageChannel()
                                .start(BigDecimal.valueOf(TIME_START));

                        api.searchReportedMessageChannels(rmc);
                    } catch (ApiException ex) {
                        Assertions.assertEquals(400, ex.getCode());
                        Assertions.assertEquals("{\"reason\":\"invalid_query_end\"}", ex.getResponseBody());
                        throw ex;
                    }
                });
            }
        }
    }
}
