package io.fizz.chat.application.domain;

import io.fizz.common.Utils;

public class CleanSpeakProviderConfig implements AbstractProviderConfig {
    private final String baseUrl;
    private final String secret;
    private final String appId;

    public CleanSpeakProviderConfig(final String aBaseUrl,
                                    final String aSecret,
                                    final String aAppId) {

        Utils.assertRequiredArgument(aBaseUrl, "invalid_base_url");
        Utils.assertRequiredArgument(aSecret, "invalid_secret");
        Utils.assertRequiredArgument(aAppId, "invalid_appId");

        this.baseUrl = aBaseUrl;
        this.secret = aSecret;
        this.appId = aAppId;
    }

    public String baseUrl() {
        return baseUrl;
    }

    public String secret() {
        return secret;
    }

    public String appId() {
        return appId;
    }

    @Override
    public ProviderType type() {
        return ProviderType.CleanSpeak;
    }
}
