package io.fizz.chat.moderation.domain;

import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.chatcommon.domain.LanguageCode;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.common.domain.UserId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReportedMessageSearchResultTest {
    @Test
    @DisplayName("it should create reported message search result object with valid params")
    void testReportedMessageSearchResult() {
        try {
            final ReportedMessageSearchResult result = new ReportedMessageSearchResult(new ArrayList<>(), 10);

            Assertions.assertNotNull(result);
            Assertions.assertEquals(result.resultSize(), 10);
            Assertions.assertEquals(result.reportedMessages().size(), 0);

        } catch (IllegalArgumentException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    @DisplayName("it should create reported message search result object with valid params")
    void testReportedMessageSearchResultValidArrayAndSize() throws DomainErrorException {
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
            List<ReportedMessage> reportedMessageList = new ArrayList<ReportedMessage>() {{
                add(rm);
            }};
            final ReportedMessageSearchResult result = new ReportedMessageSearchResult(reportedMessageList, 10);

            Assertions.assertNotNull(result);
            Assertions.assertEquals(result.resultSize(), 10);
            Assertions.assertEquals(result.reportedMessages().size(), 1);

        } catch (IllegalArgumentException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    @DisplayName("it should not create reported message search result object with null array")
    void testReportedMessageSearchResultNullArray() {
        try {
            new ReportedMessageSearchResult(null, 10);
            Assertions.fail("it should not create the reported message search result object!");
        } catch (IllegalArgumentException ex) {
            Assertions.assertEquals("invalid_reported_message_list", ex.getMessage());
        }
    }

    @Test
    @DisplayName("it should not create reported message search result object with invalid result size")
    void testReportedMessageSearchResultInvalidResultSize() {
        try {
            new ReportedMessageSearchResult(new ArrayList<>(), -1);
            Assertions.fail("it should not create the reported message search result object!");
        } catch (IllegalArgumentException ex) {
            Assertions.assertEquals("invalid_result_size", ex.getMessage());
        }
    }

    @Test
    @DisplayName("it should not create reported message search result object with invalid result and array size")
    void testReportedMessageSearchResultInvalidResultAndArraySize() throws DomainErrorException {
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
            List<ReportedMessage> reportedMessageList = new ArrayList<ReportedMessage>() {{
                add(rm);
            }};
            new ReportedMessageSearchResult(reportedMessageList, 0);
            Assertions.fail("it should not create the reported message search result object!");
        } catch (IllegalArgumentException ex) {
            Assertions.assertEquals("invalid_result_size", ex.getMessage());
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
