package io.fizz.chat.application.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.fizz.chat.application.AbstractApplicationRepository;
import io.fizz.chat.application.FCMConfiguration;
import io.fizz.chat.application.domain.AbstractProviderConfig;
import io.fizz.common.domain.ApplicationId;

import java.util.concurrent.TimeUnit;

public class MockApplicationService extends ApplicationService {
    public MockApplicationService(AbstractApplicationRepository aAppRepo) {
        super(aAppRepo);
    }

    @Override
    protected Cache<ApplicationId, FCMConfiguration> buildFCMConfigCache(long cacheExpireMinutes, TimeUnit minutes) {
        return super.buildFCMConfigCache(1, TimeUnit.SECONDS);
    }

    @Override
    protected ProviderFactory buildProviderFactory() {
        return new MockProviderFactory();
    }

    @Override
    protected Cache<ApplicationId, AbstractProviderConfig> buildCache() {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(10, TimeUnit.MILLISECONDS)
                .build();
    }
}
