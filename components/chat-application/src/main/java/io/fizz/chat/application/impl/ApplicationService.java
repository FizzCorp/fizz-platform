package io.fizz.chat.application.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.fizz.chat.application.*;
import io.fizz.chat.application.domain.AbstractProviderConfig;
import io.fizz.chat.application.domain.AbstractProviderFactory;
import io.fizz.chat.application.domain.CleanSpeakProviderConfig;
import io.fizz.chat.application.provider.AbstractProvider;
import io.fizz.client.hbase.client.AbstractHBaseClient;
import io.fizz.common.Config;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.DomainErrorException;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ApplicationService implements AbstractApplicationService {
    private static final long CONFIG_FCM_CACHE_EXPIRE_MINUTES =
            ConfigService.config().getNumber("chat.application.config.fcm.cache.expire.minutes").longValue();
    private static final String CLEANSPEAK_BASE_URL = ConfigService.config().getString("cleanspeak.content.moderation.baseurl");
    private static final String CLEANSPEAK_SECRET = ConfigService.config().getString("cleanspeak.content.moderation.secret");
    private static final String CLEANSPEAK_APP_ID = ConfigService.config().getString("cleanspeak.content.moderation.app.id");

    private static final long CONFIG_CACHE_MAX_SIZE = ConfigService.config().getNumber( "chat.moderation.config.cache.size.max").longValue();
    private static final long CONFIG_CACHE_EXPIRE_MINUTES = ConfigService.config().getNumber("chat.moderation.config.cache.expire.minutes").longValue();

    private final AbstractApplicationRepository appRepo;
    private final Cache<ApplicationId, FCMConfiguration> cacheFCMConfig;

    private final AbstractProviderConfig defaultConfig;
    private final AbstractProviderFactory providerFactory;

    private final Cache<ApplicationId, AbstractProviderConfig> cache;

    public ApplicationService(final AbstractApplicationRepository aAppRepo) {
        Utils.assertRequiredArgument(aAppRepo, "invalid_app_repo");

        appRepo = aAppRepo;
        cacheFCMConfig = buildFCMConfigCache(CONFIG_FCM_CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        this.defaultConfig = new CleanSpeakProviderConfig(CLEANSPEAK_BASE_URL, CLEANSPEAK_SECRET, CLEANSPEAK_APP_ID);
        providerFactory = buildProviderFactory();
        cache = buildCache();

    }

    protected ProviderFactory buildProviderFactory() {
        return new ProviderFactory();
    }

    protected Cache<ApplicationId, AbstractProviderConfig> buildCache() {
        return CacheBuilder.newBuilder()
                .maximumSize(CONFIG_CACHE_MAX_SIZE)
                .expireAfterWrite(CONFIG_CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES)
                .build();
    }


    @Override
    public CompletableFuture<Void> updatePreferences(final ApplicationId aId, final Preferences aPref) {
        Utils.assertRequiredArgument(aId, "invalid_app_id");

        return getPreferences(aId)
                .thenCompose(aPrefs -> {
                    if (Objects.nonNull(aPref.isForceContentModeration())) {
                        aPrefs.setForceContentModeration(aPref.isForceContentModeration());
                    }
                    return appRepo.put(aId, aPrefs);
                });
    }

    @Override
    public CompletableFuture<Preferences> getPreferences(final ApplicationId aId) {
        Utils.assertRequiredArgument(aId, "invalid_app_id");
        return appRepo.getPreferences(aId)
                .thenApply(aPrefs -> {
                    if (Objects.isNull(aPrefs)) {
                        Preferences prefs = new Preferences();
                        prefs.setForceContentModeration(false);
                        return prefs;
                    }
                    return aPrefs;
                });
    }

    @Override
    public CompletableFuture<Void> setPushConfig(final ApplicationId aId, final FCMConfiguration aConfig) {
        Utils.assertRequiredArgument(aId, "invalid_app_id");
        Utils.assertRequiredArgument(aConfig, "invalid_config");

        return appRepo.put(aId, aConfig);
    }

    @Override
    public CompletableFuture<FCMConfiguration> getFCMConfig(final ApplicationId aId) {
        Utils.assertRequiredArgument(aId, "invalid_app_id");
        final FCMConfiguration config = cacheFCMConfig.getIfPresent(aId);
        if (Objects.nonNull(config)) {
            return CompletableFuture.completedFuture(config);
        }
        return appRepo.getConfigFCM(aId)
                .thenApply(aConfig -> {
                    if (Objects.nonNull(aConfig)) {
                        cacheFCMConfig.put(aId, aConfig);
                    }
                    return aConfig;
                });
    }

    @Override
    public CompletableFuture<Void> clearFCMConfig(final ApplicationId aId) {
        Utils.assertRequiredArgument(aId, "invalid_app_id");

        return appRepo.removeConfigFCM(aId);
    }

    protected Cache<ApplicationId, FCMConfiguration> buildFCMConfigCache(long cacheExpireMinutes, TimeUnit minutes) {
        return CacheBuilder.newBuilder().expireAfterWrite(cacheExpireMinutes, minutes).build();
    }

    @Override
    public CompletableFuture<Boolean> saveContentModerationConfig(final ApplicationId aAppId,
                                                                  final AbstractProviderConfig aConfig) {
        try {
            Utils.assertRequiredArgument(aConfig, "invalid_provider_config");
            return appRepo.saveContentModerationConfig(aAppId, aConfig);
        } catch (Exception ex) {
            return Utils.failedFuture(ex);
        }
    }

    @Override
    public CompletableFuture<Void> deleteContentModerationConfig(final ApplicationId aAppId) {
        try {
            return appRepo.deleteContentModerationConfig(aAppId);
        } catch (Exception ex) {
            return Utils.failedFuture(ex);
        }
    }

    @Override
    public CompletableFuture<AbstractProviderConfig> fetchContentModerationConfig(final ApplicationId aAppId) {
        try {
            Utils.assertRequiredArgument(aAppId, "invalid_application_id");
            return fetchConfigFromCache(aAppId);
        } catch (Exception ex) {
            return Utils.failedFuture(ex);
        }
    }

    public CompletableFuture<AbstractProvider> fetchProviderConfigOrDefault(final ApplicationId aAppId) {
        return fetchConfigFromCache(aAppId)
                .thenCompose(this::provider);
    }

    private CompletableFuture<AbstractProvider> provider(final AbstractProviderConfig aConfig) {
        try {
            return providerFactory.create(Objects.isNull(aConfig) ? defaultConfig : aConfig);
        } catch (DomainErrorException ex) {
            return Utils.failedFuture(ex);
        }
    }

    private CompletableFuture<AbstractProviderConfig> fetchConfigFromCache(final ApplicationId aAppId) {
        final AbstractProviderConfig cachedConfig = cache.getIfPresent(aAppId);
        if (Objects.nonNull(cachedConfig)) {
            return CompletableFuture.completedFuture(cachedConfig);
        }

        CompletableFuture<AbstractProviderConfig> future = new CompletableFuture<>();
        appRepo.fetchContentModerationConfig(aAppId)
                .thenApply(config -> {
                    if (Objects.nonNull(config)) {
                        cache.put(aAppId, config);
                    }
                    future.complete(config);
                    return null;
                });
        return future;
    }
}
