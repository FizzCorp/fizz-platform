package io.fizz.chat.pubsub.infrastructure.domain;

import io.fizz.chat.pubsub.domain.topic.TopicName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TopicNameTest {
    @Test
    @DisplayName("it should create a valid topic name")
    void valid() {
        TopicName topic = new TopicName().append("l1").append("l2").append("l3").append("l4");

        Assertions.assertEquals("l1/l2/l3/l4", topic.value('/'));
    }

    @Test
    @DisplayName("it should not create topic name with invalid symbols")
    void invalidTopicNameTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new TopicName().append(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new TopicName().append(""));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new TopicName().append("invalid/topic"));
    }

    @Test
    @DisplayName("it should not create topic name with invalid levels")
    void invalidLevelsTest() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new TopicName().append("l1").append("l2").append("l3").append("l4").append("l5")
        );
    }

    @Test
    @DisplayName("it should not create a topic with invalid level size")
    void invalidLevelSizeTest() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new TopicName().append(generate())
        );
    }

    private String generate() {
        StringBuilder builder = new StringBuilder();
        for (int ci = 0; ci < 151; ci++) {
            builder.append('1');
        }
        return builder.toString();
    }
}
