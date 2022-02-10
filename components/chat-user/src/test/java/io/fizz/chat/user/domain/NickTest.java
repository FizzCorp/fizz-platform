package io.fizz.chat.user.domain;

import io.fizz.chat.user.domain.Nick;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class NickTest {
    @Test
    @DisplayName("it should allow to create nick with proper value")
    void validNickTest() {
        new Nick("Nick A");
        new Nick("Nick B");
    }

    @Test
    @DisplayName("it should throw exception if nick has no value")
    void nullValueTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Nick(null));
    }

    @Test
    @DisplayName("it should throw exception if nick exceeds the character limit")
    void invalidNickSizeTest() {
        final byte[] largeValue = new byte[Nick.MAX_NICK_LEN+1];
        new Random().nextBytes(largeValue);

        Assertions.assertThrows(IllegalArgumentException.class, () -> new Nick(new String(largeValue, "UTF-8")));
    }

    @Test
    @DisplayName("it should properly equate nick objects")
    void equalityTest() {
        final Nick nick1 = new Nick("nickA");
        final Nick nick2 = new Nick("nickB");
        final Nick nick3 = new Nick("nickA");

        Assertions.assertEquals(nick1, nick3);
        Assertions.assertNotEquals(nick1, nick2);
    }
}
