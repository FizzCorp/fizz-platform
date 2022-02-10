package io.fizz.chat.application.impl;


import io.fizz.chat.application.domain.AbstractProviderConfig;
import io.fizz.chat.application.domain.AbstractProviderFactory;
import io.fizz.chat.application.provider.AbstractProvider;
import io.fizz.chat.application.provider.AzureProvider;
import io.fizz.chat.application.provider.CleanSpeakProvider;
import io.fizz.common.LoggingService;
import io.fizz.common.Utils;
import io.fizz.common.domain.DomainError;
import io.fizz.common.domain.DomainErrorException;

import java.util.concurrent.CompletableFuture;

public class ProviderFactory implements AbstractProviderFactory {
    protected static final LoggingService.Log logger = LoggingService.getLogger(ProviderFactory.class);
    public static final DomainErrorException ERROR_INVALID_PROVIDER_TYPE = new DomainErrorException(new DomainError("invalid_provider_type"));

    @Override
    public CompletableFuture<AbstractProvider> create(final AbstractProviderConfig aConfig) throws DomainErrorException {
        Utils.assertRequiredArgument(aConfig, "invalid_provider_config");
        CompletableFuture<AbstractProvider> provider;
        switch (aConfig.type()) {
            case Azure:
                provider = createAzureProvider(aConfig);
                break;

            case CleanSpeak:
                provider = createCleanSpeakProvider(aConfig);
                break;

            default:
                throw ERROR_INVALID_PROVIDER_TYPE;
        }
        return provider;
    }

    protected CompletableFuture<AbstractProvider> createAzureProvider(final AbstractProviderConfig aConfig) {
        return CompletableFuture.completedFuture(new AzureProvider(aConfig));
    }

    protected CompletableFuture<AbstractProvider> createCleanSpeakProvider(final AbstractProviderConfig aConfig) {
        return CompletableFuture.completedFuture(new CleanSpeakProvider(aConfig));
    }
}
