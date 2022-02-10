package io.fizz.chat.application;

import io.fizz.chat.application.domain.AbstractProviderConfig;
import io.fizz.common.domain.ApplicationId;

import java.util.concurrent.CompletableFuture;

public interface AbstractApplicationService {
    CompletableFuture<Void> updatePreferences(final ApplicationId aId, final Preferences aPref);
    CompletableFuture<Preferences> getPreferences(final ApplicationId aId);
    CompletableFuture<Boolean> saveContentModerationConfig(final ApplicationId aAppId, final AbstractProviderConfig aConfig);
    CompletableFuture<Void> deleteContentModerationConfig(final ApplicationId aAppId);
    CompletableFuture<AbstractProviderConfig> fetchContentModerationConfig(final ApplicationId aAppId);

    CompletableFuture<Void> setPushConfig(final ApplicationId aAppId, final FCMConfiguration aConfig);
    CompletableFuture<FCMConfiguration> getFCMConfig(final ApplicationId aAppId);
    CompletableFuture<Void> clearFCMConfig(final ApplicationId aAppId);
}
