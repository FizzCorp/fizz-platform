package io.fizz.logger.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class LogCountTest {

    @Test
    @DisplayName("it should return max count if LogCount has no value")
    void nullValueTest() {
        LogCount logCount = new LogCount(null);
        Assertions.assertEquals(50, logCount.value());
    }

    @Test
    @DisplayName("it should allow valid range")
    void logCountValidRangeTest() {
        new LogCount(1);
        new LogCount(50);
    }

    @Test
    @DisplayName("it should throw exception if log count is not in valid range")
    void invalidLogIdSizeTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new LogCount(0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new LogCount(51));
    }
}
