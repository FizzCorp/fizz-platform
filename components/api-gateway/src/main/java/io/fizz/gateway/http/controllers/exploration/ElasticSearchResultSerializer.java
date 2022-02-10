package io.fizz.gateway.http.controllers.exploration;

import com.google.gson.*;
import io.fizz.common.infastructure.model.TextMessageES;

import java.lang.reflect.Type;

public class ElasticSearchResultSerializer implements JsonSerializer<TextMessageES> {

    @Override
    public JsonElement serialize(TextMessageES textMessageES, Type type, JsonSerializationContext jsonSerializationContext) {
        final JsonObject json = new JsonObject();

        json.addProperty(MessageQueryTokens.ID.value(), textMessageES.getId());
        json.addProperty(MessageQueryTokens.APP_ID.value(), textMessageES.getAppId());
        json.addProperty(MessageQueryTokens.COUNTRY_CODE.value(), textMessageES.getCountryCode());
        json.addProperty(MessageQueryTokens.ACTOR_ID.value(), textMessageES.getActorId());
        json.addProperty(MessageQueryTokens.NICK.value(), textMessageES.getNick());
        json.addProperty(MessageQueryTokens.CONTENT.value(), textMessageES.getContent());
        json.addProperty(MessageQueryTokens.CHANNEL.value(), textMessageES.getChannel());
        json.addProperty(MessageQueryTokens.PLATFORM.value(), textMessageES.getPlatform());
        json.addProperty(MessageQueryTokens.BUILD.value(), textMessageES.getBuild());
        json.addProperty(MessageQueryTokens.CUSTOM_01.value(), textMessageES.getCustom01());
        json.addProperty(MessageQueryTokens.CUSTOM_02.value(), textMessageES.getCustom02());
        json.addProperty(MessageQueryTokens.CUSTOM_03.value(), textMessageES.getCustom03());
        json.addProperty(MessageQueryTokens.AGE.value(), textMessageES.getAge());
        json.addProperty(MessageQueryTokens.SPENDER.value(), textMessageES.getSpender());
        json.addProperty(MessageQueryTokens.TIME_STAMP.value(), textMessageES.getTimestamp());
        json.addProperty(MessageQueryTokens.SENTIMENT_SCORE.value(), textMessageES.getSentimentScore());

        return json;
    }
}
