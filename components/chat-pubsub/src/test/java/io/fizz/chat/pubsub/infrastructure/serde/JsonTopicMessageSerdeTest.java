package io.fizz.chat.pubsub.infrastructure.serde;

import io.fizz.chat.pubsub.domain.topic.TopicMessage;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

class JsonTopicMessageSerdeTest {
    @Test
    void validationTest() {
        final TopicMessage message = new TopicMessage(
                1L,
                "test",
                "userA",
                new JsonObject().put("content", "test message").toString(),
                new Date()
        );
        final JsonTopicMessageSerde serde = new JsonTopicMessageSerde();
        final String json = serde.serialize(message);
        final TopicMessage message2 = serde.deserialize(json);

        Assertions.assertEquals(message, message2);
    }
}
