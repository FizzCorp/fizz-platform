package io.fizz.chat.application;


import com.google.gson.*;
import io.fizz.chat.application.domain.AbstractProviderConfig;
import io.fizz.chat.application.domain.AzureProviderConfig;
import io.fizz.chat.application.domain.CleanSpeakProviderConfig;

import java.lang.reflect.Type;

public class ProviderConfigSerde implements JsonSerializer<AbstractProviderConfig>,
        JsonDeserializer<AbstractProviderConfig> {

    private final String CONFIG_TYPE_KEY = "type";

    private static final String AZURE_BASE_URL_KEY = "base_url";
    private static final String AZURE_SECRET_KEY = "secret";

    private static final String CS_BASE_URL_KEY = "base_url";
    private static final String CS_SECRET_KEY = "secret";
    private static final String CS_APP_ID_KEY = "cp_app_id";

    @Override
    public JsonElement serialize(final AbstractProviderConfig aConfig,
                                 final Type aType,
                                 final JsonSerializationContext aJsonSerializationContext) {
        JsonObject result = new JsonObject();
        switch (aConfig.type()) {
            case Azure:
                result = serialize((AzureProviderConfig) aConfig);
                break;
            case CleanSpeak:
                result  = serialize((CleanSpeakProviderConfig) aConfig);
        }

        return result;
    }

    private JsonObject serialize(final AzureProviderConfig aConfig) {
        JsonObject result = new JsonObject();
        result.addProperty(CONFIG_TYPE_KEY, ProviderType.Azure.value());
        result.addProperty(AZURE_BASE_URL_KEY, aConfig.baseUrl());
        result.addProperty(AZURE_SECRET_KEY, aConfig.secret());
        return result;
    }

    private JsonObject serialize(final CleanSpeakProviderConfig aConfig) {
        JsonObject result = new JsonObject();
        result.addProperty(CONFIG_TYPE_KEY, ProviderType.CleanSpeak.value());
        result.addProperty(CS_BASE_URL_KEY, aConfig.baseUrl());
        result.addProperty(CS_SECRET_KEY, aConfig.secret());
        result.addProperty(CS_APP_ID_KEY, aConfig.appId());
        return result;
    }

    @Override
    public AbstractProviderConfig deserialize(final JsonElement aJsonElement,
                                              final Type aType,
                                              final JsonDeserializationContext aJsonDeserializationContext) throws JsonParseException {
        JsonObject json = (JsonObject) aJsonElement;
        AbstractProviderConfig config = null;
        final String pType = getValue(json, CONFIG_TYPE_KEY);
        ProviderType providerType = ProviderType.fromValue(pType);
        switch (providerType) {
            case Azure:
                config = deserializeAzure(json);
                break;
            case CleanSpeak:
                config = deserializeCleanSpeak(json);
                break;
        }

        return config;
    }

    private AbstractProviderConfig deserializeAzure(final JsonObject aConfig) {
        final String baseUrl = getValue(aConfig, AZURE_BASE_URL_KEY);
        final String secret = getValue(aConfig, AZURE_SECRET_KEY);
        return new AzureProviderConfig(baseUrl, secret);
    }

    private AbstractProviderConfig deserializeCleanSpeak(final JsonObject aConfig) {
        final String baseUrl = getValue(aConfig, CS_BASE_URL_KEY);
        final String secret = getValue(aConfig, CS_SECRET_KEY);
        final String appId = getValue(aConfig, CS_APP_ID_KEY);
        return new CleanSpeakProviderConfig(baseUrl, secret, appId);
    }

    private String getValue(final JsonObject aJson, final String aKey) {
        if (aJson.has(aKey)) {
            return aJson.get(aKey).getAsString();
        } else {
            throw new IllegalArgumentException(String.format("%s missing in: ", aKey) + aJson.toString());
        }
    }

    public enum ProviderType {
        Azure("azure"),
        CleanSpeak("cleanspeak");

        private String value;
        ProviderType(String aValue) {
            value = aValue;
        }

        public String value() {
            return value;
        }

        public static ProviderType fromValue(final String aValue) throws JsonParseException {
            for (ProviderType type: ProviderType.values()) {
                if (type.value.equals(aValue)) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.format("invalid_provider_type_%s", aValue));
        }
    }
}
