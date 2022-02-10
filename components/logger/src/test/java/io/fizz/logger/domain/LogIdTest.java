package io.fizz.logger.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Random;

public class LogIdTest {

    @Test
    @DisplayName("it should throw exception if logId contains invalid characters")
    void validCharactersTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new LogId("123?s"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new LogId("asz=s"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new LogId("as|z*@s"));
    }

    @Test
    @DisplayName("it should throw exception if LogId has no value")
    void invalidNullValueTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new LogId(null));
    }

    @Test
    @DisplayName("it should throw exception if logId length is not in valid range")
    void invalidLogIdSizeTest() {
        final byte[] largeValue = new byte[129];
        new Random().nextBytes(largeValue);

        Assertions.assertThrows(IllegalArgumentException.class, () -> new LogId(""));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new LogId(new String(largeValue, StandardCharsets.UTF_8)));
    }

}
