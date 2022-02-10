package io.fizz.chat.pubsub.infrastructure.serde;

import io.fizz.chat.pubsub.domain.topic.TopicMessage;

import java.util.Map;

public interface AbstractMessageSerde {
    String serialize(final TopicMessage aMessage);
    TopicMessage deserialize(final String aMessage);

    String serializePayload(final Map<String,Object> aPayload);
    Map<String,Object> deserializePayload(final String aPayload);
}
