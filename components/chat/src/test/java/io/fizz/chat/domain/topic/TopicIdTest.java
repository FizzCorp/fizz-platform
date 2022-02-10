package io.fizz.chat.domain.topic;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

class TopicIdTest {
    @Test
    @DisplayName("it should throw exception if topic contains wildcards")
    void topicWildcardsTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new TopicId("topic#"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new TopicId("topic+"));
    }

    @Test
    @DisplayName("it should throw exception if topic has null character")
    void nullCharacterTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new TopicId("channe\u0000l"));
    }

    @Test
    @DisplayName("it should throw exception if topic has no value")
    void nullValueTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new TopicId(null));
    }

    @Test
    void invalidChannelSizeTest() {
        final byte[] largeValue = new byte[TopicId.MAX_LEN+1];
        new Random().nextBytes(largeValue);

        Assertions.assertThrows(IllegalArgumentException.class, () -> new TopicId(""));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new TopicId(new String(largeValue, "UTF-8")));
    }

    @Test
    void reservedTopicTest() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new TopicId("fizzTest"),
                "invalid_topic_id"
        );
    }

    @Test
    @DisplayName("it should properly equate topic objects")
    void equalityTest() {
        final TopicId topic1 = new TopicId("topicA");
        final TopicId topic2 = new TopicId("topicB");
        final TopicId topic3 = new TopicId("topicA");

        Assertions.assertEquals(topic1, topic3);
        Assertions.assertNotEquals(topic1, topic2);
    }
}
