package io.fizz.analytics.jobs.streamProcessing.store;

import com.google.gson.*;
import io.fizz.common.domain.EventType;
import io.fizz.common.domain.events.*;

import java.lang.reflect.Type;

public class DomainEventDeserializer implements JsonDeserializer<AbstractDomainEvent> {
    @Override
    public AbstractDomainEvent deserialize(JsonElement aElement, Type aType, JsonDeserializationContext aContext) throws JsonParseException {
        final JsonObject obj = aElement.getAsJsonObject();
        final EventType type = EventType.valueOf(obj.get("type").getAsString());

        switch (type) {
            case SESSION_STARTED:
                return aContext.deserialize(aElement, SessionStarted.class);
            case SESSION_ENDED:
                return aContext.deserialize(aElement, SessionEnded.class);
            case TEXT_MESSAGE_SENT:
                return aContext.deserialize(aElement, TextMessageSent.class);
            case PRODUCT_PURCHASED:
                return aContext.deserialize(aElement, ProductPurchased.class);
            case TEXT_TRANSLATED:
                return aContext.deserialize(aElement, TextMessageTranslated.class);
            case INVALID:
                throw new JsonParseException("invalid event type encountered");
        }

        return null;
    }
}
