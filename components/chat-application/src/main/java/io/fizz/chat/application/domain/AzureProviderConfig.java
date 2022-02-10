package io.fizz.chat.application.domain;

import io.fizz.common.Utils;

public class AzureProviderConfig implements AbstractProviderConfig {

    private final String baseUrl;
    private final String secret;

    public AzureProviderConfig(final String aBaseUrl,
                               final String aSecret) {

        Utils.assertRequiredArgument(aBaseUrl, "invalid_base_url");
        Utils.assertRequiredArgument(aSecret, "invalid_secret");

        this.baseUrl = aBaseUrl;
        this.secret = aSecret;
    }

    public String baseUrl() {
        return baseUrl;
    }

    public String secret() {
        return secret;
    }

    @Override
    public ProviderType type() {
        return ProviderType.Azure;
    }
}
