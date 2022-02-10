package io.fizz.chat.application;

import io.fizz.chat.application.domain.AbstractProviderConfig;
import io.fizz.common.domain.ApplicationId;

import java.util.concurrent.CompletableFuture;

public interface AbstractApplicationRepository {
    CompletableFuture<Preferences> getPreferences(final ApplicationId aId);
    CompletableFuture<Void> put(final ApplicationId aId, final Preferences aPrefs);

    CompletableFuture<FCMConfiguration> getConfigFCM(final ApplicationId aId);
    CompletableFuture<Void> put(final ApplicationId aId, final FCMConfiguration aConfig);
    CompletableFuture<Void> removeConfigFCM(final ApplicationId aId);

    CompletableFuture<AbstractProviderConfig> fetchContentModerationConfig(final ApplicationId aAppId);
    CompletableFuture<Void> deleteContentModerationConfig(final ApplicationId aAppId);
    CompletableFuture<Boolean> saveContentModerationConfig(final ApplicationId aAppId,
                                                                  final AbstractProviderConfig aConfig);
}
