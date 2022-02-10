package io.fizz.chatcommon.infrastructure.serde;

import com.google.gson.*;
import io.fizz.chatcommon.domain.events.AbstractDomainEvent;
import io.fizz.chatcommon.domain.events.DomainEventType;

import java.lang.reflect.Type;
import java.util.Objects;

public class JsonEventSerde implements AbstractEventSerde {
    private static class DomainEventDeserializer implements JsonDeserializer<AbstractDomainEvent> {
        @Override
        public AbstractDomainEvent deserialize(JsonElement aElement, Type aType, JsonDeserializationContext aContext) throws JsonParseException {
            final JsonObject obj = aElement.getAsJsonObject();
            final String type = obj.get("type").getAsString();

            if (Objects.isNull(type)) {
                throw new JsonParseException("type missing in event: " + obj.toString());
            }

            final Class<?> clazz = DomainEventType.toClass(type);
            if (Objects.isNull(clazz)) {
                throw new JsonParseException("class missing for event type: " + type);
            }

            return aContext.deserialize(aElement, clazz);
        }
    }

    @Override
    public String serialize(AbstractDomainEvent aEvent) {
        final Gson gson = new Gson();
        final JsonElement element = gson.toJsonTree(aEvent);

        element.getAsJsonObject().addProperty("type", aEvent.type().value());

        return gson.toJson(element);
    }

    @Override
    public AbstractDomainEvent deserialize(String aEvent) {
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(AbstractDomainEvent.class, new DomainEventDeserializer())
                .create();

        return gson.fromJson(aEvent, AbstractDomainEvent.class);
    }
}
