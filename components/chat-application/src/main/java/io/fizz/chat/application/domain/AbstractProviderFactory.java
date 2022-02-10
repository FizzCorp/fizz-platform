package io.fizz.chat.application.domain;

import io.fizz.chat.application.provider.AbstractProvider;
import io.fizz.common.domain.DomainErrorException;

import java.util.concurrent.CompletableFuture;

public interface AbstractProviderFactory {
    CompletableFuture<AbstractProvider> create(final AbstractProviderConfig aConfig) throws DomainErrorException;
}
