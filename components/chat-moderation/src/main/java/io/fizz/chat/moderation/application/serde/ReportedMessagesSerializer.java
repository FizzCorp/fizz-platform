package io.fizz.chat.moderation.application.serde;

import com.google.gson.*;
import io.fizz.chat.moderation.domain.ReportedMessage;
import io.fizz.chat.moderation.domain.ReportedMessageSearchResult;

import java.lang.reflect.Type;

public class ReportedMessagesSerializer implements JsonSerializer<ReportedMessageSearchResult> {
    @Override
    public JsonElement serialize(ReportedMessageSearchResult reportedMessages, Type type, JsonSerializationContext jsonSerializationContext) {
        final JsonObject results = new JsonObject();

        JsonArray array = new JsonArray();
        for(ReportedMessage message: reportedMessages.reportedMessages()) {
            final JsonObject json = new JsonObject();

            json.addProperty("id", message.id());
            json.addProperty("user_id", message.reportedUserId().value());
            json.addProperty("reporter_id", message.reporterUserId().value());
            json.addProperty("channel_id", message.channelId().value());
            json.addProperty("message_id", message.messageId());
            json.addProperty("message", message.message());
            json.addProperty("language", message.language().value());
            json.addProperty("offense", message.offense().value());
            json.addProperty("description", message.description());
            json.addProperty("time", message.time());

            array.add(json);
        }

        results.add("data", array);
        results.addProperty("total_size", reportedMessages.resultSize());

        return results;
    }
}
