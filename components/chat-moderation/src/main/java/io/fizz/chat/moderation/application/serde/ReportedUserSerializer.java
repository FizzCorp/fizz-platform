package io.fizz.chat.moderation.application.serde;

import com.google.gson.*;
import io.fizz.chat.moderation.domain.ReportedUser;

import java.lang.reflect.Type;

public class ReportedUserSerializer implements JsonSerializer<ReportedUser> {
    @Override
    public JsonElement serialize(ReportedUser reportedMessages, Type type, JsonSerializationContext jsonSerializationContext) {
        final JsonObject result = new JsonObject();

        result.addProperty("user_id", reportedMessages.userId().value());
        result.addProperty("count", reportedMessages.count());

        return result;
    }
}
