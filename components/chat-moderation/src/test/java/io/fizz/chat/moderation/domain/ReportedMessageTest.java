package io.fizz.chat.moderation.domain;

import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.chatcommon.domain.LanguageCode;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.common.domain.UserId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

public class ReportedMessageTest {
    @Test
    @DisplayName("it should create reported message object with valid params")
    void testReportedMessage() {
        try {
            ReportedMessage rm = buildMessage("id1",
                    "appA",
                    "userA",
                    "userB",
                    "Test Message",
                    "message_1",
                    "channelA",
                    LanguageCode.ENGLISH,
                    "Other",
                    "This is test description",
                    1553510400L);

            Assertions.assertNotNull(rm);
            Assertions.assertEquals("id1", rm.id());
            Assertions.assertEquals("appA", rm.appId().value());
            Assertions.assertEquals("userA", rm.reporterUserId().value());
            Assertions.assertEquals("userB", rm.reportedUserId().value());
            Assertions.assertEquals("Test Message", rm.message());
            Assertions.assertEquals("message_1", rm.messageId());
            Assertions.assertEquals("channelA", rm.channelId().value());
            Assertions.assertEquals("appA", rm.channelId().appId().value());
            Assertions.assertEquals(LanguageCode.ENGLISH.value(), rm.language().value());
            Assertions.assertEquals("Other", rm.offense().value());
            Assertions.assertEquals("This is test description", rm.description());
            Assertions.assertEquals(1553510400L, rm.time().longValue());

        } catch (DomainErrorException e) {
            Assertions.fail(e);
        }
    }

    @Test
    @DisplayName("it should create reported message object with valid params")
    void testReportedMessageOptionDescription() {
        try {
            ReportedMessage rm = buildMessage("id1",
                    "appA",
                    "userA",
                    "userB",
                    "Test Message",
                    "message_1",
                    "channelA",
                    LanguageCode.ENGLISH,
                    "Other",
                    null,
                    1553510400L);

            Assertions.assertNotNull(rm);

        } catch (DomainErrorException e) {
            Assertions.fail(e);
        }
    }

    @Test
    @DisplayName("it should not create reported message object with missing id")
    void testReportedMessageId() {
        try {
            buildMessage(null,
                    "appA",
                    "userA",
                    "userB",
                    "Test Message",
                    "message_1",
                    "channelA",
                    LanguageCode.ENGLISH,
                    "Other",
                    "This is test description",
                    1553510400L);

            Assertions.fail("it should not build the reported message object");

        } catch (DomainErrorException e) {
            Assertions.fail(e);
        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("invalid_id", e.getMessage());
        }
    }

    @Test
    @DisplayName("it should not create reported message object with missing app id")
    void testReportedMessageMissingAppId() {
        try {
            buildMessage("id1",
                    null,
                    "userA",
                    "userB",
                    "Test Message",
                    "message_1",
                    "channelA",
                    LanguageCode.ENGLISH,
                    "Other",
                    "This is test description",
                    1553510400L);

            Assertions.fail("it should not build the reported message object");

        } catch (DomainErrorException e) {
            Assertions.fail(e);
        } catch (IllegalArgumentException e) {
            Assertions.assertTrue(true);
            Assertions.assertEquals("invalid_app_id", e.getMessage());
        }
    }

    @Test
    @DisplayName("it should not create reported message object with missing reporter id")
    void testReportedMessageMissingReporterId() {
        try {
            buildMessage("id1",
                    "appA",
                    null,
                    "userB",
                    "Test Message",
                    "message_1",
                    "channelA",
                    LanguageCode.ENGLISH,
                    "Other",
                    "This is test description",
                    1553510400L);

            Assertions.fail("it should not build the reported message object");

        } catch (DomainErrorException e) {
            Assertions.fail(e);
        } catch (IllegalArgumentException e) {
            Assertions.assertTrue(true);
            Assertions.assertEquals("invalid_reporter_user_id", e.getMessage());
        }
    }

    @Test
    @DisplayName("it should not create reported message object with missing reported user id")
    void testReportedMessageMissingReportedUserId() {
        try {
            buildMessage("id1",
                    "appA",
                    "userA",
                    null,
                    "Test Message",
                    "message_1",
                    "channelA",
                    LanguageCode.ENGLISH,
                    "Other",
                    "This is test description",
                    1553510400L);

            Assertions.fail("it should not build the reported message object");

        } catch (DomainErrorException e) {
            Assertions.fail(e);
        } catch (IllegalArgumentException e) {
            Assertions.assertTrue(true);
            Assertions.assertEquals("invalid_reported_user_id", e.getMessage());
        }
    }

    @Test
    @DisplayName("it should not create reported message object with same reporter and reported user id")
    void testReportedMessageSameUserIds() {
        try {
            buildMessage("id1",
                    "appA",
                    "userA",
                    "userA",
                    "Test Message",
                    "message_1",
                    "channelA",
                    LanguageCode.ENGLISH,
                    "Other",
                    "This is test description",
                    1553510400L);

            Assertions.fail("it should not build the reported message object");

        } catch (DomainErrorException e) {
            Assertions.assertTrue(true);
            Assertions.assertEquals("invalid_reporter_user_id", e.getMessage());
        } catch (IllegalArgumentException e) {
            Assertions.fail(e);

        }
    }

    @Test
    @DisplayName("it should not create reported message object with missing message")
    void testReportedMessageMissingMessage() {
        try {
            buildMessage("id1",
                    "appA",
                    "userA",
                    "userB",
                    null,
                    "message_1",
                    "channelA",
                    LanguageCode.ENGLISH,
                    "Other",
                    "This is test description",
                    1553510400L);

            Assertions.fail("it should not build the reported message object");

        } catch (DomainErrorException e) {
            Assertions.fail(e);
        } catch (IllegalArgumentException e) {
            Assertions.assertTrue(true);
            Assertions.assertEquals("invalid_message", e.getMessage());
        }
    }

    @Test
    @DisplayName("it should not create reported message object with missing message id")
    void testReportedMessageMissingMessageId() {
        try {
            buildMessage("id1",
                    "appA",
                    "userA",
                    "userB",
                    "Test Message",
                    null,
                    "channelA",
                    LanguageCode.ENGLISH,
                    "Other",
                    "This is test description",
                    1553510400L);

            Assertions.fail("it should not build the reported message object");

        } catch (DomainErrorException e) {
            Assertions.fail(e);
        } catch (IllegalArgumentException e) {
            Assertions.assertTrue(true);
            Assertions.assertEquals("invalid_message_id", e.getMessage());
        }
    }

    @Test
    @DisplayName("it should not create reported message object with missing channel id")
    void testReportedMessageMissingChannelId() {
        try {
            buildMessage("id1",
                    "appA",
                    "userA",
                    "userB",
                    "Test Message",
                    "messageA",
                    null,
                    LanguageCode.ENGLISH,
                    "Other",
                    "This is test description",
                    1553510400L);

            Assertions.fail("it should not build the reported message object");

        } catch (DomainErrorException e) {
            Assertions.fail(e);
        } catch (IllegalArgumentException e) {
            Assertions.assertTrue(true);
            Assertions.assertEquals("invalid_channel_id", e.getMessage());
        }
    }

    @Test
    @DisplayName("it should not create reported message object with missing language")
    void testReportedMessageMissingLanguage() {
        try {
            buildMessage("id1",
                    "appA",
                    "userA",
                    "userB",
                    "Test Message",
                    "messageA",
                    "channelA",
                    null,
                    "Other",
                    "This is test description",
                    1553510400L);

            Assertions.fail("it should not build the reported message object");

        } catch (DomainErrorException e) {
            Assertions.fail(e);
        } catch (IllegalArgumentException e) {
            Assertions.assertTrue(true);
            Assertions.assertEquals("invalid_language", e.getMessage());
        }
    }

    @Test
    @DisplayName("it should not create reported message object with missing offense")
    void testReportedMessageMissingOffense() {
        try {
            buildMessage("id1",
                    "appA",
                    "userA",
                    "userB",
                    "Test Message",
                    "messageA",
                    "channelA",
                    LanguageCode.ENGLISH,
                    null,
                    "This is test description",
                    1553510400L);

            Assertions.fail("it should not build the reported message object");

        } catch (DomainErrorException e) {
            Assertions.fail(e);
        } catch (IllegalArgumentException e) {
            Assertions.assertTrue(true);
            Assertions.assertEquals("invalid_offense", e.getMessage());
        }
    }

    @Test
    @DisplayName("it should not create reported message object with missing time")
    void testReportedMessageMissingTime() {
        try {
            buildMessage("id1",
                    "appA",
                    "userA",
                    "userB",
                    "Test Message",
                    "messageA",
                    "channelA",
                    LanguageCode.ENGLISH,
                    "Other",
                    "This is test description",
                    null);

            Assertions.fail("it should not build the reported message object");

        } catch (DomainErrorException e) {
            Assertions.fail(e);
        } catch (IllegalArgumentException e) {
            Assertions.assertTrue(true);
            Assertions.assertEquals("invalid_time", e.getMessage());
        }
    }

    @Test
    @DisplayName("it should not create reported message object with invalid time")
    void testReportedMessageInvalidTime() {
        try {
            buildMessage("id1",
                    "appA",
                    "userA",
                    "userB",
                    "Test Message",
                    "messageA",
                    "channelA",
                    LanguageCode.ENGLISH,
                    "Other",
                    "This is test description",
                    -1L);

            Assertions.fail("it should not build the reported message object");

        } catch (DomainErrorException e) {
            Assertions.fail(e);
        } catch (IllegalArgumentException e) {
            Assertions.assertTrue(true);
            Assertions.assertEquals("invalid_time", e.getMessage());
        }
    }

    private ReportedMessage buildMessage(final String aId,
                                         final String aAppId,
                                         final String aReporterUserId,
                                         final String aReportedUserId,
                                         final String aMessage,
                                         final String aMessageId,
                                         final String aChannelId,
                                         final LanguageCode aLang,
                                         final String aOffense,
                                         final String aDesc,
                                         final Long aTime) throws DomainErrorException {
        final ApplicationId appId = Objects.isNull(aAppId) ? null : new ApplicationId(aAppId);
        final UserId reporterUserId = Objects.isNull(aReporterUserId) ? null : new UserId(aReporterUserId);
        final UserId reportedUserId = Objects.isNull(aReportedUserId) ? null : new UserId(aReportedUserId);
        final ChannelId channelId = Objects.isNull(aChannelId) ? null : new ChannelId(appId, aChannelId);
        final ReportOffense offense = new ReportOffense(aOffense);

        return new ReportedMessage(aId, appId, reporterUserId, reportedUserId, aMessage, aMessageId,
                channelId, aLang, offense, aDesc, aTime);
    }
}
