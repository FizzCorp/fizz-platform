package io.fizz.chat.moderation.application.serde;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.fizz.chat.moderation.domain.ReportedChannel;

import java.lang.reflect.Type;

public class ReportedChannelSerializer implements JsonSerializer<ReportedChannel> {
    @Override
    public JsonElement serialize(ReportedChannel reportedChannel, Type type, JsonSerializationContext jsonSerializationContext) {
        final JsonObject result = new JsonObject();

        result.addProperty("channel_id", reportedChannel.channelId().value());
        result.addProperty("count", reportedChannel.count());

        return result;
    }
}
