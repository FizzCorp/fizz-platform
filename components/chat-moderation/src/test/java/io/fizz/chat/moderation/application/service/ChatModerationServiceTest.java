package io.fizz.chat.moderation.application.service;


import io.fizz.chat.moderation.domain.ReportedChannel;
import io.fizz.chat.moderation.domain.ReportedMessage;
import io.fizz.chat.moderation.domain.ReportedMessageSearchResult;
import io.fizz.chat.moderation.domain.ReportedUser;
import io.fizz.chat.moderation.infrastructure.repository.MockChatModerationRepository;
import io.fizz.common.domain.DomainErrorException;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
public class ChatModerationServiceTest {
    private static final int TEST_TIMEOUT = 5;

    private static final String APP_ID = "appA";
    private static final String REPORTER_USER_ID = "userA";
    private static final String REPORTED_USER_ID = "userB";
    private static final String CHANNEL_ID = "channelA";
    private static final String MESSAGE = "This is test message";
    private static final String MESSAGE_ID = "messageA";
    private static final String LANGUAGE = "en";
    private static final String OFFENSE = "Other";
    private static final String DESCRIPTION = "This is test description";
    private static final Long TIME = 1553510400L;
    private static final long TIME_START = 1553510320;
    private static final long TIME_END = 1553510380;

    @Nested
    @DisplayName("Report Message Test Suite")
    class MessageReportTestSuit {
        @Nested
        @DisplayName("Report Message Positive Test Suite")
        class MessageReportPositiveTestSuit {
            @Test
            @DisplayName("it should report text message with success future")
            void reportMessageTest(final VertxTestContext aContext) throws InterruptedException {
                ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
                CompletableFuture<String> future = service.reportMessage(APP_ID,
                        REPORTER_USER_ID,
                        REPORTED_USER_ID,
                        CHANNEL_ID,
                        MESSAGE,
                        MESSAGE_ID,
                        LANGUAGE,
                        OFFENSE,
                        DESCRIPTION,
                        TIME);

                future.handle((result, error) -> {
                    Assertions.assertNull(error);
                    Assertions.assertEquals("1w1TiYdpBn/G/CLhg3goFw==", result);
                    aContext.completeNow();
                    return CompletableFuture.completedFuture(error);
                });

                Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
            }

            @Test
            @DisplayName("it should report text message with missing description")
            void reportMessageMissingDescriptionTest(final VertxTestContext aContext) throws InterruptedException {
                ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
                CompletableFuture<String> future = service.reportMessage(APP_ID,
                        REPORTER_USER_ID,
                        REPORTED_USER_ID,
                        CHANNEL_ID,
                        MESSAGE,
                        MESSAGE_ID,
                        LANGUAGE,
                        OFFENSE,
                        null,
                        TIME);

                future.handle((result, error) -> {
                    Assertions.assertNull(error);
                    Assertions.assertEquals("1w1TiYdpBn/G/CLhg3goFw==", result);
                    aContext.completeNow();
                    return CompletableFuture.completedFuture(error);
                });

                Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
            }
        }

        @Nested
        @DisplayName("Report Message Negative Test Suit")
        class MessagesQueryNegativeTestSuite {
            @Test
            @DisplayName("it should not report text message with missing channel Id")
            void reportMessageMissingChannelIdTest(final VertxTestContext aContext) throws InterruptedException {
                ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
                CompletableFuture<String> future = service.reportMessage(APP_ID,
                        REPORTER_USER_ID,
                        REPORTED_USER_ID,
                        null,
                        MESSAGE,
                        MESSAGE_ID,
                        LANGUAGE,
                        OFFENSE,
                        DESCRIPTION,
                        TIME);

                future.handle((result, error) -> {
                    Assertions.assertNotNull(error);
                    Assertions.assertEquals(IllegalArgumentException.class, error.getCause().getClass());
                    Assertions.assertEquals("invalid_channel_id", error.getCause().getMessage());
                    aContext.completeNow();
                    return CompletableFuture.completedFuture(error);
                });

                Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
            }
        }

        @Test
        @DisplayName("it should not report text message with missing language code")
        void reportMessageMissingLanguageTest(final VertxTestContext aContext) throws InterruptedException {
            ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
            CompletableFuture<String> future = service.reportMessage(APP_ID,
                    REPORTER_USER_ID,
                    REPORTED_USER_ID,
                    CHANNEL_ID,
                    MESSAGE,
                    MESSAGE_ID,
                    null,
                    OFFENSE,
                    DESCRIPTION,
                    TIME);

            future.handle((result, error) -> {
                Assertions.assertNotNull(error);
                Assertions.assertEquals(DomainErrorException.class, error.getCause().getClass());
                Assertions.assertEquals("invalid_language_code_null", error.getCause().getMessage());
                aContext.completeNow();
                return CompletableFuture.completedFuture(error);
            });

            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should not report text message with missing message")
        void reportMessageMissingMessageTest(final VertxTestContext aContext) throws InterruptedException {
            ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
            CompletableFuture<String> future = service.reportMessage(APP_ID,
                    REPORTER_USER_ID,
                    REPORTED_USER_ID,
                    CHANNEL_ID,
                    null,
                    MESSAGE_ID,
                    LANGUAGE,
                    OFFENSE,
                    DESCRIPTION,
                    TIME);

            future.handle((result, error) -> {
                Assertions.assertNotNull(error);
                Assertions.assertEquals(IllegalArgumentException.class, error.getCause().getClass());
                Assertions.assertEquals("invalid_message", error.getCause().getMessage());
                aContext.completeNow();
                return CompletableFuture.completedFuture(error);
            });

            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should not report text message with missing message id")
        void reportMessageMissingMessageIdTest(final VertxTestContext aContext) throws InterruptedException {
            ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
            CompletableFuture<String> future = service.reportMessage(APP_ID,
                    REPORTER_USER_ID,
                    REPORTED_USER_ID,
                    CHANNEL_ID,
                    MESSAGE,
                    null,
                    LANGUAGE,
                    OFFENSE,
                    DESCRIPTION,
                    TIME);

            future.handle((result, error) -> {
                Assertions.assertNotNull(error);
                Assertions.assertEquals(IllegalArgumentException.class, error.getCause().getClass());
                Assertions.assertEquals("invalid_message_id", error.getCause().getMessage());
                aContext.completeNow();
                return CompletableFuture.completedFuture(error);
            });

            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should not report text message with missing Offense")
        void reportMessageMissingOffenseTest(final VertxTestContext aContext) throws InterruptedException {
            ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
            CompletableFuture<String> future = service.reportMessage(APP_ID,
                    REPORTER_USER_ID,
                    REPORTED_USER_ID,
                    CHANNEL_ID,
                    MESSAGE,
                    MESSAGE_ID,
                    LANGUAGE,
                    null,
                    DESCRIPTION,
                    TIME);

            future.handle((result, error) -> {
                Assertions.assertNotNull(error);
                Assertions.assertEquals(IllegalArgumentException.class, error.getCause().getClass());
                Assertions.assertEquals("invalid_offense", error.getCause().getMessage());
                aContext.completeNow();
                return CompletableFuture.completedFuture(error);
            });

            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should not report text message with missing time")
        void reportMessageMissingTimeTest(final VertxTestContext aContext) throws InterruptedException {
            ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
            CompletableFuture<String> future = service.reportMessage(APP_ID,
                    REPORTER_USER_ID,
                    REPORTED_USER_ID,
                    CHANNEL_ID,
                    MESSAGE,
                    MESSAGE_ID,
                    LANGUAGE,
                    OFFENSE,
                    DESCRIPTION,
                    null);

            future.handle((result, error) -> {
                Assertions.assertNotNull(error);
                Assertions.assertEquals(IllegalArgumentException.class, error.getCause().getClass());
                Assertions.assertEquals("invalid_time", error.getCause().getMessage());
                aContext.completeNow();
                return CompletableFuture.completedFuture(error);
            });

            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should not report text message with missing reported user id")
        void reportMessageMissingReportedUserIdTest(final VertxTestContext aContext) throws InterruptedException {
            ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
            CompletableFuture<String> future = service.reportMessage(APP_ID,
                    REPORTER_USER_ID,
                    null,
                    CHANNEL_ID,
                    MESSAGE,
                    MESSAGE_ID,
                    LANGUAGE,
                    OFFENSE,
                    DESCRIPTION,
                    TIME);

            future.handle((result, error) -> {
                Assertions.assertNotNull(error);
                Assertions.assertEquals(IllegalArgumentException.class, error.getCause().getClass());
                Assertions.assertEquals("invalid_user_id", error.getCause().getMessage());
                aContext.completeNow();
                return CompletableFuture.completedFuture(error);
            });

            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should not report text message with missing reporter user id")
        void reportMessageMissingUserIdTest(final VertxTestContext aContext) throws InterruptedException {
            ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
            CompletableFuture<String> future = service.reportMessage(APP_ID,
                    null,
                    REPORTED_USER_ID,
                    CHANNEL_ID,
                    MESSAGE,
                    MESSAGE_ID,
                    LANGUAGE,
                    OFFENSE,
                    DESCRIPTION,
                    TIME);

            future.handle((result, error) -> {
                Assertions.assertNotNull(error);
                Assertions.assertEquals(IllegalArgumentException.class, error.getCause().getClass());
                Assertions.assertEquals("invalid_user_id", error.getCause().getMessage());
                aContext.completeNow();
                return CompletableFuture.completedFuture(error);
            });

            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("it should not report text message with missing app id")
        void reportMessageMissingAppIdTest(final VertxTestContext aContext) throws InterruptedException {
            ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
            CompletableFuture<String> future = service.reportMessage(null,
                    REPORTER_USER_ID,
                    REPORTED_USER_ID,
                    CHANNEL_ID,
                    MESSAGE,
                    MESSAGE_ID,
                    LANGUAGE,
                    OFFENSE,
                    DESCRIPTION,
                    TIME);

            future.handle((result, error) -> {
                Assertions.assertNotNull(error);
                Assertions.assertEquals(DomainErrorException.class, error.getCause().getClass());
                Assertions.assertEquals("invalid_app_id", error.getCause().getMessage());
                aContext.completeNow();
                return CompletableFuture.completedFuture(error);
            });

            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
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
            void queryReportedMessageTest(final VertxTestContext aContext) throws InterruptedException {
                ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
                CompletableFuture<ReportedMessageSearchResult> future = service.searchMessages(APP_ID,
                        null, null, null, null,
                        null, null, TIME_START, TIME_END);

                future.handle((result, error) -> {
                    Assertions.assertEquals(3, result.resultSize());
                    Assertions.assertEquals(3, result.reportedMessages().size());
                    aContext.completeNow();
                    return CompletableFuture.completedFuture(error);
                });

                Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
            }

            @Test
            @DisplayName("it should search reported messages with cursor")
            void queryReportedMessageCursorTest(final VertxTestContext aContext) throws InterruptedException {
                ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
                CompletableFuture<ReportedMessageSearchResult> future = service.searchMessages(APP_ID,
                        null, null, null, 2,
                        null, null, TIME_START, TIME_END);

                future.handle((result, error) -> {
                    Assertions.assertEquals(1, result.resultSize());
                    Assertions.assertEquals(1, result.reportedMessages().size());

                    ReportedMessage data = result.reportedMessages().get(0);
                    Assertions.assertEquals("id_3", data.id());
                    Assertions.assertEquals("channel_a", data.channelId().value());
                    Assertions.assertEquals("message_id_3", data.messageId());
                    Assertions.assertEquals("test_message_3", data.message());
                    Assertions.assertEquals("fr", data.language().value());
                    Assertions.assertEquals("user_4", data.reportedUserId().value());
                    Assertions.assertEquals("user_3", data.reporterUserId().value());
                    Assertions.assertEquals("Other", data.offense().value());
                    Assertions.assertEquals("this is mocked message 3", data.description());
                    Assertions.assertEquals(TIME_END, data.time().longValue());

                    aContext.completeNow();
                    return CompletableFuture.completedFuture(error);
                });

                Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
            }

            @Test
            @DisplayName("it should search reported messages with page size")
            void queryReportedMessagePageSizeTest(final VertxTestContext aContext) throws InterruptedException {
                ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
                CompletableFuture<ReportedMessageSearchResult> future = service.searchMessages(APP_ID,
                        null, null, null, null,
                        1, null, TIME_START, TIME_END);

                future.handle((result, error) -> {
                    Assertions.assertEquals(3, result.resultSize());
                    Assertions.assertEquals(1, result.reportedMessages().size());

                    ReportedMessage data = result.reportedMessages().get(0);
                    Assertions.assertEquals("id_1", data.id());
                    Assertions.assertEquals("channel_a", data.channelId().value());
                    Assertions.assertEquals("message_id_1", data.messageId());
                    Assertions.assertEquals("test_message_1", data.message());
                    Assertions.assertEquals("en", data.language().value());
                    Assertions.assertEquals("user_2", data.reportedUserId().value());
                    Assertions.assertEquals("user_1", data.reporterUserId().value());
                    Assertions.assertEquals("Toxic", data.offense().value());
                    Assertions.assertEquals("this is mocked message 1", data.description());
                    Assertions.assertEquals(TIME_START, data.time().longValue());

                    aContext.completeNow();
                    return CompletableFuture.completedFuture(error);
                });

                Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
            }

            @Test
            @DisplayName("it should search reported messages with sort order")
            void queryReportedMessageSortTest(final VertxTestContext aContext) throws InterruptedException {
                ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
                CompletableFuture<ReportedMessageSearchResult> future = service.searchMessages(APP_ID,
                        null, null, null, null,
                        null, "asc", TIME_START, TIME_END);

                future.handle((result, error) -> {
                    Assertions.assertEquals(3, result.resultSize());
                    Assertions.assertEquals(3, result.reportedMessages().size());

                    ReportedMessage data = result.reportedMessages().get(0);
                    Assertions.assertEquals("id_3", data.id());
                    Assertions.assertEquals("channel_a", data.channelId().value());
                    Assertions.assertEquals("message_id_3", data.messageId());
                    Assertions.assertEquals("test_message_3", data.message());
                    Assertions.assertEquals("fr", data.language().value());
                    Assertions.assertEquals("user_4", data.reportedUserId().value());
                    Assertions.assertEquals("user_3", data.reporterUserId().value());
                    Assertions.assertEquals("Other", data.offense().value());
                    Assertions.assertEquals("this is mocked message 3", data.description());
                    Assertions.assertEquals(TIME_END, data.time().longValue());

                    aContext.completeNow();
                    return CompletableFuture.completedFuture(error);
                });

                Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
            }

            @Test
            @DisplayName("it should search reported messages with user id")
            void queryReportedMessageFilerUserTest(final VertxTestContext aContext) throws InterruptedException {
                ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
                CompletableFuture<ReportedMessageSearchResult> future = service.searchMessages(APP_ID,
                        "user_2", null, null, null,
                        null, null, TIME_START, TIME_END);

                future.handle((result, error) -> {
                    Assertions.assertEquals(2, result.resultSize());
                    Assertions.assertEquals(2, result.reportedMessages().size());

                    aContext.completeNow();
                    return CompletableFuture.completedFuture(error);
                });

                Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
            }

            @Test
            @DisplayName("it should search reported messages with channel id")
            void queryReportedMessageFilerChannelTest(final VertxTestContext aContext) throws InterruptedException {
                ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
                CompletableFuture<ReportedMessageSearchResult> future = service.searchMessages(APP_ID,
                        null, "channel_a", null, null,
                        null, null, TIME_START, TIME_END);

                future.handle((result, error) -> {
                    Assertions.assertEquals(2, result.resultSize());
                    Assertions.assertEquals(2, result.reportedMessages().size());

                    aContext.completeNow();
                    return CompletableFuture.completedFuture(error);
                });

                Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
            }

            @Test
            @DisplayName("it should search reported messages with channel id")
            void queryReportedMessageFilerLanguageTest(final VertxTestContext aContext) throws InterruptedException {
                ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
                CompletableFuture<ReportedMessageSearchResult> future = service.searchMessages(APP_ID,
                        null, null, "en", null,
                        null, null, TIME_START, TIME_END);

                future.handle((result, error) -> {
                    Assertions.assertEquals(2, result.resultSize());
                    Assertions.assertEquals(2, result.reportedMessages().size());

                    aContext.completeNow();
                    return CompletableFuture.completedFuture(error);
                });

                Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
            }
        }

        @Nested
        @DisplayName("Report Query Test Negative Suit")
        class MessagesQueryNegativeTestSuite {
            @Test
            @DisplayName("it should give error on missing start time")
            void queryReportedMessageMissingStartTimeTest(final VertxTestContext aContext) throws InterruptedException {
                ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
                CompletableFuture<ReportedMessageSearchResult> future = service.searchMessages(APP_ID,
                        null, null, null, null,
                        null, null, null, TIME_END);

                future.handle((result, error) -> {
                    Assertions.assertNotNull(error);
                    Assertions.assertEquals(DomainErrorException.class, error.getCause().getClass());
                    Assertions.assertEquals("invalid_query_start", error.getCause().getMessage());
                    aContext.completeNow();
                    return CompletableFuture.completedFuture(error);
                });

                Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
            }
            @Test
            @DisplayName("it should give error on missing end time")
            void queryReportedMessageMissingEndTimeTest(final VertxTestContext aContext) throws InterruptedException {
                ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
                CompletableFuture<ReportedMessageSearchResult> future = service.searchMessages(APP_ID,
                        null, null, null, null,
                        null, null, TIME_START, null);

                future.handle((result, error) -> {
                    Assertions.assertNotNull(error);
                    Assertions.assertEquals(DomainErrorException.class, error.getCause().getClass());
                    Assertions.assertEquals("invalid_query_end", error.getCause().getMessage());
                    aContext.completeNow();
                    return CompletableFuture.completedFuture(error);
                });

                Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
            }

            @Test
            @DisplayName("it should give error on missing app id")
            void queryReportedMessageMissingAppIdTest(final VertxTestContext aContext) throws InterruptedException {
                ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
                CompletableFuture<ReportedMessageSearchResult> future = service.searchMessages(null,
                        null, null, null, null,
                        null, null, TIME_START, TIME_END);

                future.handle((result, error) -> {
                    Assertions.assertNotNull(error);
                    Assertions.assertEquals(DomainErrorException.class, error.getCause().getClass());
                    Assertions.assertEquals("invalid_app_id", error.getCause().getMessage());
                    aContext.completeNow();
                    return CompletableFuture.completedFuture(error);
                });

                Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
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
            void queryReportedMessageUsersTest(final VertxTestContext aContext) throws InterruptedException {
                ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
                CompletableFuture<List<ReportedUser>> future = service.searchUsers(APP_ID, null, null, 10,
                        TIME_START, TIME_END);

                future.handle((result, error) -> {
                    Assertions.assertEquals(2, result.size());
                    Assertions.assertEquals("user_2", result.get(0).userId().value());
                    Assertions.assertEquals(2, result.get(0).count());
                    Assertions.assertEquals("user_4", result.get(1).userId().value());
                    Assertions.assertEquals(1, result.get(1).count());
                    aContext.completeNow();
                    return CompletableFuture.completedFuture(error);
                });

                Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
            }

            @Test
            @DisplayName("it should return user aggregation of reported message with language code")
            void queryReportedMessageUsersLanguageTest(final VertxTestContext aContext) throws InterruptedException {
                ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
                CompletableFuture<List<ReportedUser>> future = service.searchUsers(APP_ID, null, "en", 10,
                        TIME_START, TIME_END);

                future.handle((result, error) -> {
                    Assertions.assertEquals(1, result.size());
                    Assertions.assertEquals("user_2", result.get(0).userId().value());
                    Assertions.assertEquals(2, result.get(0).count());
                    aContext.completeNow();
                    return CompletableFuture.completedFuture(error);
                });

                Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
            }

            @Test
            @DisplayName("it should return user aggregation of reported message with channel id")
            void queryReportedMessageUsersChannelIdTest(final VertxTestContext aContext) throws InterruptedException {
                ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
                CompletableFuture<List<ReportedUser>> future = service.searchUsers(APP_ID, "channel_a", null, 10,
                        TIME_START, TIME_END);

                future.handle((result, error) -> {
                    Assertions.assertEquals(2, result.size());
                    Assertions.assertEquals("user_2", result.get(0).userId().value());
                    Assertions.assertEquals(1, result.get(0).count());
                    Assertions.assertEquals("user_4", result.get(1).userId().value());
                    Assertions.assertEquals(1, result.get(1).count());
                    aContext.completeNow();
                    return CompletableFuture.completedFuture(error);
                });

                Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
            }

            @Test
            @DisplayName("it should return user aggregation of reported message with limit")
            void queryReportedMessageUserLimitTest(final VertxTestContext aContext) throws InterruptedException {
                ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
                CompletableFuture<List<ReportedUser>> future = service.searchUsers(APP_ID, null, null, 1,
                        TIME_START, TIME_END);

                future.handle((result, error) -> {
                    Assertions.assertEquals(1, result.size());
                    Assertions.assertEquals("user_2", result.get(0).userId().value());
                    Assertions.assertEquals(1, result.get(0).count());
                    aContext.completeNow();
                    return CompletableFuture.completedFuture(error);
                });

                Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
            }
        }

        @Nested
        @DisplayName("Report User Aggregation Negative Test Suit")
        class MessagesUserAggregationNegativeTestSuite {
            @Test
            @DisplayName("it should not return user aggregation of reported message with missing start time")
            void queryReportedMessageUsersMissingStartTest(final VertxTestContext aContext) throws InterruptedException {
                ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
                CompletableFuture<List<ReportedUser>> future = service.searchUsers(APP_ID, null, null, 10,
                        null, TIME_END);

                future.handle((result, error) -> {
                    Assertions.assertNotNull(error);
                    Assertions.assertEquals(DomainErrorException.class, error.getCause().getClass());
                    Assertions.assertEquals("invalid_query_start", error.getCause().getMessage());
                    aContext.completeNow();
                    return CompletableFuture.completedFuture(error);
                });

                Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
            }

            @Test
            @DisplayName("it should not return user aggregation of reported message with missing end time")
            void queryReportedMessageUsersMissingEndTest(final VertxTestContext aContext) throws InterruptedException {
                ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
                CompletableFuture<List<ReportedUser>> future = service.searchUsers(APP_ID, null, null, 10,
                        TIME_START, null);

                future.handle((result, error) -> {
                    Assertions.assertNotNull(error);
                    Assertions.assertEquals(DomainErrorException.class, error.getCause().getClass());
                    Assertions.assertEquals("invalid_query_end", error.getCause().getMessage());
                    aContext.completeNow();
                    return CompletableFuture.completedFuture(error);
                });

                Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
            }

            @Test
            @DisplayName("it should not return user aggregation of reported message with missing app id")
            void queryReportedMessageUsersMissingAppIdTest(final VertxTestContext aContext) throws InterruptedException {
                ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
                CompletableFuture<List<ReportedUser>> future = service.searchUsers(null, null, null, 10,
                        TIME_START, TIME_END);

                future.handle((result, error) -> {
                    Assertions.assertNotNull(error);
                    Assertions.assertEquals(DomainErrorException.class, error.getCause().getClass());
                    Assertions.assertEquals("invalid_app_id", error.getCause().getMessage());
                    aContext.completeNow();
                    return CompletableFuture.completedFuture(error);
                });

                Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
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
            void queryReportedMessageChannelTest(final VertxTestContext aContext) throws InterruptedException {
                ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
                CompletableFuture<List<ReportedChannel>> future = service.searchChannels(APP_ID, null, 10, TIME_START, TIME_END);

                future.handle((result, error) -> {
                    Assertions.assertEquals(2, result.size());
                    Assertions.assertEquals("channel_a", result.get(0).channelId().value());
                    Assertions.assertEquals(2, result.get(0).count());
                    Assertions.assertEquals("channel_b", result.get(1).channelId().value());
                    Assertions.assertEquals(1, result.get(1).count());
                    aContext.completeNow();
                    return CompletableFuture.completedFuture(error);
                });

                Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
            }

            @Test
            @DisplayName("it should return channel aggregation of reported message with language code")
            void queryReportedMessageChannelLanguageTest(final VertxTestContext aContext) throws InterruptedException {
                ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
                CompletableFuture<List<ReportedChannel>> future = service.searchChannels(APP_ID, "en", 10, TIME_START, TIME_END);

                future.handle((result, error) -> {
                    Assertions.assertEquals(2, result.size());
                    Assertions.assertEquals("channel_a", result.get(0).channelId().value());
                    Assertions.assertEquals(1, result.get(0).count());
                    Assertions.assertEquals("channel_b", result.get(1).channelId().value());
                    Assertions.assertEquals(1, result.get(1).count());
                    aContext.completeNow();
                    return CompletableFuture.completedFuture(error);
                });

                Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
            }

            @Test
            @DisplayName("it should return channel aggregation of reported message with limit")
            void queryReportedMessageChannelLimitTest(final VertxTestContext aContext) throws InterruptedException {
                ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
                CompletableFuture<List<ReportedChannel>> future = service.searchChannels(APP_ID, "en", 1, TIME_START, TIME_END);

                future.handle((result, error) -> {
                    Assertions.assertEquals(1, result.size());
                    Assertions.assertEquals("channel_a", result.get(0).channelId().value());
                    Assertions.assertEquals(1, result.get(0).count());
                    aContext.completeNow();
                    return CompletableFuture.completedFuture(error);
                });

                Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
            }
        }

        @Nested
        @DisplayName("Report Channel Aggregation Negative Test Suit")
        class MessagesChannelAggregationNegativeTestSuite {
            @Test
            @DisplayName("it should not return channel aggregation of reported message with missing start time")
            void queryReportedMessageChannelMissingStartTest(final VertxTestContext aContext) throws InterruptedException {
                ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
                CompletableFuture<List<ReportedChannel>> future = service.searchChannels(APP_ID, null, 10, null, TIME_END);

                future.handle((result, error) -> {
                    Assertions.assertNotNull(error);
                    Assertions.assertEquals(DomainErrorException.class, error.getCause().getClass());
                    Assertions.assertEquals("invalid_query_start", error.getCause().getMessage());
                    aContext.completeNow();
                    return CompletableFuture.completedFuture(error);
                });

                Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
            }

            @Test
            @DisplayName("it should not return channel aggregation of reported message with missing end time")
            void queryReportedMessageChannelMissingEndTest(final VertxTestContext aContext) throws InterruptedException {
                ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
                CompletableFuture<List<ReportedChannel>> future = service.searchChannels(APP_ID, null, 10, TIME_START, null);

                future.handle((result, error) -> {
                    Assertions.assertNotNull(error);
                    Assertions.assertEquals(DomainErrorException.class, error.getCause().getClass());
                    Assertions.assertEquals("invalid_query_end", error.getCause().getMessage());
                    aContext.completeNow();
                    return CompletableFuture.completedFuture(error);
                });

                Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
            }

            @Test
            @DisplayName("it should not return channel aggregation of reported message with missing app id")
            void queryReportedMessageChannelMissingAppIdTest(final VertxTestContext aContext) throws InterruptedException {
                ChatModerationService service = new ChatModerationService(new MockChatModerationRepository());
                CompletableFuture<List<ReportedChannel>> future = service.searchChannels(null, null, 10, TIME_START, TIME_END);

                future.handle((result, error) -> {
                    Assertions.assertNotNull(error);
                    Assertions.assertEquals(DomainErrorException.class, error.getCause().getClass());
                    Assertions.assertEquals("invalid_app_id", error.getCause().getMessage());
                    aContext.completeNow();
                    return CompletableFuture.completedFuture(error);
                });

                Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
            }
        }


    }
}
