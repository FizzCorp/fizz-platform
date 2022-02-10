package io.fizz.chat.moderation.domain;

import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.DomainErrorException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ReportedChannelTest {
    @Test
    @DisplayName("it should create report channel object with valid params")
    void testReportChannel() throws DomainErrorException {
        try {
            final ChannelId channelId = new ChannelId (new ApplicationId("appA"), "channelA");
            ReportedChannel rc = new ReportedChannel(channelId, 2);

            Assertions.assertNotNull(rc);
            Assertions.assertEquals("channelA", rc.channelId().value());
            Assertions.assertEquals("appA", rc.channelId().appId().value());
            Assertions.assertEquals(2, rc.count());

        } catch (IllegalArgumentException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    @DisplayName("it should not create report channel object with null user id")
    void testReportChannelNullUserId() {
        try {
            new ReportedChannel(null, 2);
            Assertions.fail("Reported Channel should not be created!");
        } catch (IllegalArgumentException ex) {
            Assertions.assertEquals("invalid_channel_id", ex.getMessage());
        }
    }

    @Test
    @DisplayName("it should create report channel object with valid params")
    void testReportChannelInvalidCount() throws DomainErrorException {
        try {
            final ChannelId channelId = new ChannelId (new ApplicationId("appA"), "channelA");
            new ReportedChannel(channelId, -1);
            Assertions.fail("Reported Channel should not be created!");
        } catch (IllegalArgumentException ex) {
            Assertions.assertEquals("invalid_count", ex.getMessage());
        }
    }
}
