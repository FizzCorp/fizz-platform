package io.fizz.chat.moderation.domain;

import io.fizz.common.domain.DomainErrorException;
import io.fizz.common.domain.UserId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ReportedUserTest {
    @Test
    @DisplayName("it should create report User object with valid params")
    void testReportUser() throws DomainErrorException {
        try {
            final UserId UserId = new UserId ("userA");
            ReportedUser ru = new ReportedUser(UserId, 2);

            Assertions.assertNotNull(ru);
            Assertions.assertEquals("userA", ru.userId().value());
            Assertions.assertEquals(2, ru.count());
        } catch (IllegalArgumentException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    @DisplayName("it should not create report User object with null user id")
    void testReportUserNullUserId() {
        try {
            new ReportedUser(null, 2);
            Assertions.fail("Reported User should not be created!");
        } catch (IllegalArgumentException ex) {
            Assertions.assertEquals("invalid_user_id", ex.getMessage());
        }
    }

    @Test
    @DisplayName("it should create report User object with valid params")
    void testReportUserInvalidCount() throws DomainErrorException {
        try {
            final UserId UserId = new UserId ("userA");
            new ReportedUser(UserId, -1);
            Assertions.fail("Reported User should not be created!");
        } catch (IllegalArgumentException ex) {
            Assertions.assertEquals("invalid_count", ex.getMessage());
        }
    }
}
