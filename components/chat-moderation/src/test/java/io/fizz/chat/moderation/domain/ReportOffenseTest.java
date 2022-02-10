package io.fizz.chat.moderation.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ReportOffenseTest {
    @Test
    @DisplayName("it should create report offense object with valid params")
    void testReportOffense() {
        try {
            final ReportOffense offense = new ReportOffense ("Other");
            Assertions.assertNotNull(offense);
            Assertions.assertEquals("Other", offense.value());
        } catch (IllegalArgumentException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    @DisplayName("it should not create report offense object with null offense")
    void testReportOffenseNullValue() {
        try {
            new ReportOffense (null);
            Assertions.fail("it should not create report offense object!");
        } catch (IllegalArgumentException ex) {
            Assertions.assertEquals("invalid_offense", ex.getMessage());
        }
    }
}
