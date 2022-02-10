package io.fizz.chat.user.domain;

import io.fizz.chat.user.domain.StatusMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class StatusMessageTest {
    @Test
    @DisplayName("it should allow to create status with proper value")
    void validStatusTest() {
        new StatusMessage("Status A");
        new StatusMessage("Status B");
    }

    @Test
    @DisplayName("it should throw exception if status has no value")
    void nullValueTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new StatusMessage(null));
    }

    @Test
    @DisplayName("it should throw exception if status exceeds the character limit")
    void invalidStatusSizeTest() {
        final byte[] largeValue = new byte[StatusMessage.MAX_STATUS_MESSAGE_LEN+1];
        new Random().nextBytes(largeValue);

        Assertions.assertThrows(IllegalArgumentException.class, () -> new StatusMessage(new String(largeValue, "UTF-8")));
    }

    @Test
    @DisplayName("it should properly equate status objects")
    void equalityTest() {
        final StatusMessage status1 = new StatusMessage("status A");
        final StatusMessage status2 = new StatusMessage("status B");
        final StatusMessage status3 = new StatusMessage("status A");

        Assertions.assertEquals(status1, status3);
        Assertions.assertNotEquals(status1, status2);
    }
}
