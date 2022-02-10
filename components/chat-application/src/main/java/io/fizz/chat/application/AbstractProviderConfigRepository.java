package io.fizz.chat.application;


import io.fizz.chat.application.domain.AbstractProviderConfig;
import io.fizz.common.domain.ApplicationId;

import java.util.concurrent.CompletableFuture;

public interface AbstractProviderConfigRepository {
    CompletableFuture<Boolean> save(final ApplicationId aAppId, final AbstractProviderConfig aConfig);
    CompletableFuture<Void> delete(final ApplicationId aAppId);
    CompletableFuture<AbstractProviderConfig> fetch(final ApplicationId aAppId);
}
