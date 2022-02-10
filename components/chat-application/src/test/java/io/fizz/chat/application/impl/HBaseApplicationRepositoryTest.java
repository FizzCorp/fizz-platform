package io.fizz.chat.application.impl;

import io.fizz.chat.application.AbstractApplicationRepository;
import io.fizz.chat.application.FCMConfiguration;
import io.fizz.chat.application.Preferences;
import io.fizz.client.hbase.client.AbstractHBaseClient;
import io.fizz.client.hbase.client.MockHBaseClient;
import io.fizz.common.domain.ApplicationId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

public class HBaseApplicationRepositoryTest {
    @Test
    void retrievalTestFCMConfig() throws Throwable {
        final FCMConfiguration config = new FCMConfiguration("test", "secret");
        ApplicationId appId = new ApplicationId("appA");
        final AbstractHBaseClient client = new MockHBaseClient();
        final AbstractApplicationRepository repo = new HBaseApplicationRepository(client);

        Assertions.assertNull(repo.getConfigFCM(appId).get());
        Assertions.assertThrows(ExecutionException.class, () -> repo.getConfigFCM(null).get());
        Assertions.assertThrows(ExecutionException.class, () -> repo.put(appId,(FCMConfiguration) null).get());

        repo.put(appId, config).get();
        FCMConfiguration fcmConfig = repo.getConfigFCM(appId).get();
        Assertions.assertEquals(fcmConfig.title(), config.title());
        Assertions.assertEquals(fcmConfig.secret(), config.secret());
    }

    @Test
    void retrievalTestPrefs() throws Throwable {
        ApplicationId appId = new ApplicationId("appA");
        Preferences prefs = new Preferences();
        prefs.setForceContentModeration(true);

        final AbstractHBaseClient client = new MockHBaseClient();
        final AbstractApplicationRepository repo = new HBaseApplicationRepository(client);

        Assertions.assertNull(repo.getPreferences(appId).get());
        Assertions.assertThrows(ExecutionException.class, () -> repo.getPreferences(null).get());
        Assertions.assertThrows(ExecutionException.class, () -> repo.put(appId,(Preferences) null).get());

        repo.put(appId, prefs).get();
        Preferences savedPrefs = repo.getPreferences(appId).get();
        Assertions.assertEquals(savedPrefs.isForceContentModeration(), prefs.isForceContentModeration());
    }
}
