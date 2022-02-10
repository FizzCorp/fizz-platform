package io.fizz.chat.pubsub.infrastructure.serde;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.fizz.chat.pubsub.domain.topic.TopicMessage;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class JsonTopicMessageSerde implements AbstractMessageSerde {
    private static final String KEY_ID = "id";
    private static final String KEY_TYPE = "type";
    private static final String KEY_FROM = "from";
    private static final String KEY_PAYLOAD = "data";
    private static final String KEY_CREATED = "created";

    static class MessageSerializer implements JsonSerializer<TopicMessage> {
        @Override
        public JsonElement serialize(final TopicMessage aMessage,
                                     final Type aType,
                                     final JsonSerializationContext aContext) {
            final JsonObject json = new JsonObject();

            json.addProperty(KEY_ID, aMessage.id());
            json.addProperty(KEY_TYPE, aMessage.type());
            json.addProperty(KEY_FROM, aMessage.from().value());
            json.addProperty(KEY_PAYLOAD, aMessage.data());
            json.addProperty(KEY_CREATED, aMessage.occurredOn());

            return json;
        }
    }

    static class MessageDeserializer implements JsonDeserializer<TopicMessage> {
        @Override
        public TopicMessage deserialize(final JsonElement aJsonElement,
                                        final Type aType,
                                        final JsonDeserializationContext aContext) throws JsonParseException {
            try {
                final JsonObject json = aJsonElement.getAsJsonObject();

                return new TopicMessage(
                    json.get(KEY_ID).getAsLong(),
                    json.get(KEY_TYPE).getAsString(),
                    json.get(KEY_FROM).getAsString(),
                    json.get(KEY_PAYLOAD).getAsString(),
                    new Date(json.get(KEY_CREATED).getAsLong())
                );
            }
            catch (IllegalArgumentException ex) {
                throw new JsonParseException(ex.getMessage());
            }
        }
    }

    @Override
    public String serialize(TopicMessage aMessage) {
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(TopicMessage.class, new MessageSerializer())
                .create();
        return gson.toJson(aMessage);
    }

    @Override
    public TopicMessage deserialize(String aMessage) {
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(TopicMessage.class, new MessageDeserializer())
                .create();

        return gson.fromJson(aMessage, TopicMessage.class);
    }

    @Override
    public String serializePayload(Map<String, Object> aPayload) {
        return Objects.isNull(aPayload) ? "{}" : new Gson().toJson(aPayload, buildPayloadType());
    }

    @Override
    public Map<String, Object> deserializePayload(String aPayload) {
        return Objects.isNull(aPayload) ? new HashMap<>() : new Gson().fromJson(aPayload, buildPayloadType());
    }

    private static Type buildPayloadType() {
        return new TypeToken<Map<String,Object>>() {}.getType();
    }
}
