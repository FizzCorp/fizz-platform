package io.fizz.chat.application.impl;



import io.fizz.chat.application.domain.AbstractProviderConfig;
import io.fizz.chat.application.provider.AbstractProvider;
import io.fizz.chat.application.provider.MockAzureProvider;
import io.fizz.chat.application.provider.MockCleanSpeakProvider;

import java.util.concurrent.CompletableFuture;

public class MockProviderFactory extends ProviderFactory {

    @Override
    protected CompletableFuture<AbstractProvider> createAzureProvider(final AbstractProviderConfig aConfig) {
        return CompletableFuture.completedFuture(new MockAzureProvider(aConfig));
    }

    @Override
    protected CompletableFuture<AbstractProvider> createCleanSpeakProvider(AbstractProviderConfig aConfig) {
        return CompletableFuture.completedFuture(new MockCleanSpeakProvider(aConfig));
    }
}
