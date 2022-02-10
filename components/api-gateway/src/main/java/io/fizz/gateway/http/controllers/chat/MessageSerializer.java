package io.fizz.gateway.http.controllers.chat;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.fizz.chat.domain.channel.ChannelMessage;
import io.fizz.chatcommon.domain.LanguageCode;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MessageSerializer implements JsonSerializer<ChannelMessage> {
    @Override
    public JsonElement serialize(final ChannelMessage aMessage,
                                 final Type aType,
                                 final JsonSerializationContext aContext) {
        final com.google.gson.JsonObject json = new com.google.gson.JsonObject();

        json.addProperty("id", aMessage.id());
        json.addProperty("from", aMessage.from().value());
        json.addProperty("to", aMessage.to().value());
        json.addProperty("created", aMessage.created());

        if (!Objects.isNull(aMessage.nick())) {
            json.addProperty("nick", aMessage.nick());
        }
        if (!Objects.isNull(aMessage.body())) {
            json.addProperty("body", aMessage.body());
        }
        if (!Objects.isNull(aMessage.data())) {
            json.addProperty("data", aMessage.data());
        }
        final Map<String,String> translations = new HashMap<>();
        if (!Objects.isNull(aMessage.translations()) && aMessage.translations().size() > 0) {
            for (Map.Entry<LanguageCode,String> entry: aMessage.translations().entrySet()) {
                translations.put(entry.getKey().value(), entry.getValue());
            }
        }
        json.add("translations", aContext.serialize(translations));

        return json;
    }
}
