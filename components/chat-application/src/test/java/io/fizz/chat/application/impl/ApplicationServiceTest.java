package io.fizz.chat.application.impl;

import io.fizz.chat.application.AbstractApplicationRepository;
import io.fizz.chat.application.FCMConfiguration;
import io.fizz.chat.application.Preferences;
import io.fizz.chat.application.domain.AzureProviderConfig;
import io.fizz.client.hbase.client.AbstractHBaseClient;
import io.fizz.client.hbase.client.MockHBaseClient;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.DomainErrorException;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;

@ExtendWith(VertxExtension.class)
public class ApplicationServiceTest {
    private static final int TEST_TIMEOUT = 10;

    private AbstractHBaseClient client;
    private AbstractApplicationRepository appRepo;
    private static ApplicationId appA;
    private static Vertx vertx;

    @BeforeEach
    public void setup(VertxTestContext aContext) throws Exception {
        vertx = Vertx.vertx();

        client = new MockHBaseClient();
        appRepo = new HBaseApplicationRepository(client);
        appA = new ApplicationId("appA");

        aContext.completeNow();

    }

    @AfterAll
    static void tearDown(VertxTestContext aContext) {
        vertx.close(res -> aContext.completeNow());
    }

    @DisplayName("It should get default application preferences")
    @Test
    public void defaultPrefsTest() throws DomainErrorException, ExecutionException, InterruptedException {
        ApplicationId appId = new ApplicationId("defaultPrefsTest");
        ApplicationService appService = new MockApplicationService(appRepo);

        Preferences prefs = appService.getPreferences(appId).get();
        Assertions.assertNotNull(prefs);
        Assertions.assertFalse(prefs.isForceContentModeration());
    }

    @DisplayName("It should get saved application preferences")
    @Test
    public void savedPrefsTest() throws DomainErrorException, ExecutionException, InterruptedException {
        ApplicationId appId = new ApplicationId("savedPrefsTest");
        ApplicationService appService = new MockApplicationService(appRepo);

        Preferences prefs = new Preferences();
        prefs.setForceContentModeration(true);
        appService.updatePreferences(appId, prefs).get();

        Preferences savedPrefs = appService.getPreferences(appId).get();
        Assertions.assertNotNull(savedPrefs);
        Assertions.assertTrue(savedPrefs.isForceContentModeration());
    }

    @DisplayName("It should get saved application preferences after cache expire")
    @Test
    public void cachePrefsTest() throws DomainErrorException, ExecutionException, InterruptedException {
        ApplicationId appId = new ApplicationId("cachePrefsTest");
        ApplicationService appService = new MockApplicationService(appRepo);

        Preferences defaultPrefs = appService.getPreferences(appId).get();
        Assertions.assertNotNull(defaultPrefs);
        Assertions.assertFalse(defaultPrefs.isForceContentModeration());

        Preferences newPrefs = new Preferences();
        newPrefs.setForceContentModeration(true);
        appService.updatePreferences(appId, newPrefs).get();

        Preferences cachePrefs = appService.getPreferences(appId).get();
        Assertions.assertNotNull(cachePrefs);
        Assertions.assertTrue(cachePrefs.isForceContentModeration());
    }

    @DisplayName("it should return null fcm push notification config")
    @Test
    public void defaultFCMConfigTest() throws DomainErrorException, ExecutionException, InterruptedException {
        ApplicationId appId = new ApplicationId("defaultPushNotificationTest");
        ApplicationService appService = new MockApplicationService(appRepo);

        FCMConfiguration config = appService.getFCMConfig(appId).get();
        Assertions.assertNull(config);
    }

    @DisplayName("it should return saved fcm push notification config")
    @Test
    public void savedFCMConfigTest() throws DomainErrorException, ExecutionException, InterruptedException {
        ApplicationId appId = new ApplicationId("defaultPushNotificationTest");
        ApplicationService appService = new MockApplicationService(appRepo);

        FCMConfiguration newConfig = new FCMConfiguration("test", "secret");
        appService.setPushConfig(appId, newConfig);

        FCMConfiguration savedConfig = appService.getFCMConfig(appId).get();
        Assertions.assertNotNull(savedConfig);
        Assertions.assertEquals(newConfig.title(), savedConfig.title());
        Assertions.assertEquals(newConfig.secret(), savedConfig.secret());
    }

    @DisplayName("It should get saved application fcm config after cache expire")
    @Test
    public void cacheFCMConfigTest() throws DomainErrorException, ExecutionException, InterruptedException {
        ApplicationId appId = new ApplicationId("cachePrefsTest");
        ApplicationService appService = new MockApplicationService(appRepo);

        FCMConfiguration defaultConfig = appService.getFCMConfig(appId).get();
        Assertions.assertNull(defaultConfig);

        FCMConfiguration newConfig = new FCMConfiguration("test", "secret");
        appService.setPushConfig(appId, newConfig).get();

        FCMConfiguration savedConfig = appService.getFCMConfig(appId).get();
        Assertions.assertNotNull(savedConfig);
        Assertions.assertEquals(newConfig.secret(), savedConfig.secret());
        Assertions.assertEquals(newConfig.title(), savedConfig.title());

        FCMConfiguration newConfig2 = new FCMConfiguration("test2", "secret2");
        appService.setPushConfig(appId, newConfig2).get();

        FCMConfiguration cacheConfig = appService.getFCMConfig(appId).get();
        Assertions.assertNotNull(cacheConfig);
        Assertions.assertEquals(newConfig.secret(), cacheConfig.secret());
        Assertions.assertEquals(newConfig.title(), cacheConfig.title());

        Thread.sleep(1000);

        FCMConfiguration savedConfig2 = appService.getFCMConfig(appId).get();
        Assertions.assertNotNull(savedConfig2);
        Assertions.assertEquals(newConfig2.secret(), savedConfig2.secret());
        Assertions.assertEquals(newConfig2.title(), savedConfig2.title());
    }

    @DisplayName("It should delete saved application fcm config after cache expire")
    @Test
    public void deleteFCMConfigTest() throws DomainErrorException, ExecutionException, InterruptedException {
        ApplicationId appId = new ApplicationId("cachePrefsTest");
        ApplicationService appService = new MockApplicationService(appRepo);

        FCMConfiguration newConfig = new FCMConfiguration("test", "secret");
        appService.setPushConfig(appId, newConfig).get();

        FCMConfiguration savedConfig = appService.getFCMConfig(appId).get();
        Assertions.assertNotNull(savedConfig);
        Assertions.assertEquals(newConfig.secret(), savedConfig.secret());
        Assertions.assertEquals(newConfig.title(), savedConfig.title());

        appService.clearFCMConfig(appId);

        FCMConfiguration cacheConfig = appService.getFCMConfig(appId).get();
        Assertions.assertNotNull(cacheConfig);
        Assertions.assertEquals(newConfig.secret(), cacheConfig.secret());
        Assertions.assertEquals(newConfig.title(), cacheConfig.title());

        Thread.sleep(1000);

        FCMConfiguration savedConfig2 = appService.getFCMConfig(appId).get();
        Assertions.assertNull(savedConfig2);
    }

    @Test
    @DisplayName("it should save an Azure provider config")
    void testSaveAzureProviderConfig(VertxTestContext aContext) throws InterruptedException, DomainErrorException {
        final ApplicationId appD = new ApplicationId("appD");
        final AzureProviderConfig createdConfig = new AzureProviderConfig("http://test.azure.com/moderate", "123456789");
        ApplicationService appService = new MockApplicationService(appRepo);

        appService.saveContentModerationConfig(appD, createdConfig)
            .thenApply(result -> {
                        Assertions.assertTrue(result);
                        return true;
            })
            .thenApply(v -> appService.fetchContentModerationConfig(appD))
            .thenApply(fetchedConfig -> {
                try {
                    Assertions.assertNotNull(fetchedConfig.get());
                    Assertions.assertTrue(fetchedConfig.get() instanceof AzureProviderConfig);
                    AzureProviderConfig azureConfig = (AzureProviderConfig) fetchedConfig.get();
                    Assertions.assertEquals(createdConfig.type(), azureConfig.type());
                    Assertions.assertEquals(createdConfig.baseUrl(), azureConfig.baseUrl());
                    Assertions.assertEquals(createdConfig.secret(), azureConfig.secret());
                } catch (Exception e) {
                    aContext.failNow(e);
                }

                aContext.completeNow();
                return null;
            });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("it should delete a provider config")
    void testDeleteProviderConfig(VertxTestContext aContext) throws InterruptedException {
        try {
            final ApplicationId appB = new ApplicationId("appB");
            ApplicationService appService = new MockApplicationService(appRepo);
            final AzureProviderConfig createdConfig = new AzureProviderConfig("http://test.azure.com/moderate", "123456789");

            appService.saveContentModerationConfig(appB, createdConfig)
                .thenApply(result -> {
                    Assertions.assertTrue(result);
                    return true;
                })
                .thenApply(result -> appService.fetchContentModerationConfig(appB))
                .thenApply(fetchedConfig -> {
                    Assertions.assertNotNull(fetchedConfig);
                    return fetchedConfig;
                })
                .thenApply(fetchedConfig -> appService.deleteContentModerationConfig(appB))
                .thenApply(v -> {
                    // Wait for cache to expire
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .thenApply(result -> appService.fetchContentModerationConfig(appB))
                .thenApply(fetchedConfig -> {
                    try {
                        Assertions.assertNull(fetchedConfig.get());
                        aContext.completeNow();
                    } catch (Exception e) {
                        aContext.failNow(e);
                    }
                    return null;
                });

            Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
        } catch (Exception ex) {
            aContext.failNow(ex);
        }
    }
}
