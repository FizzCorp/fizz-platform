package io.fizz.gateway.http.controllers;

import com.google.gson.Gson;
import io.fizz.common.ConfigService;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.gateway.Constants;
import io.fizz.gateway.http.MockApplication;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.ConfigurationsApi;
import io.swagger.client.api.PreferencesApi;
import io.swagger.client.model.*;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
public class ApplicationConfigControllerTest {

    private static int TEST_TIMEOUT = Constants.TEST_TIMEOUT;
    private static Vertx vertx;
    private static int port;

    private static final String APP_A = "appA";
    private static final String APP_C = "appC";

    static private Future<String> deployVertex() {
        Future<String> future = Future.future();
        vertx.deployVerticle(MockApplication.class.getName(), future);
        return future;
    }

    @BeforeAll
    static void setUp(VertxTestContext aContext) throws InterruptedException {
        vertx = Vertx.vertx();
        port = ConfigService.instance().getNumber("http.port").intValue();

        deployVertex()
                .setHandler(ar -> {
                    if (ar.succeeded()) {
                        aContext.completeNow();
                    } else {
                        aContext.failNow(ar.cause());
                    }
                });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @AfterAll
    static void tearDown(VertxTestContext aContext) {
        vertx.close(res -> aContext.completeNow());
    }

    @Test
    @DisplayName("it should fetch all default configurations")
    void validDefaultFetchConfigurationsTest() throws ApiException, InvalidKeyException, NoSuchAlgorithmException {
        final ConfigurationsApi client = new ConfigurationsApi(new ApiClient());

        TestUtils.signV2(
                client.getApiClient(),
                APP_A,
                "GET",
                "/v1/configs",
                "",
                "secret"
        );

        ApplicationConfigResponse response = client.fetchConfigurations();
        Assertions.assertNotNull(response);

//        ProviderConfigResponse2 providerConfig = response.getContentModerator();
//        Assertions.assertNotNull(providerConfig);
//        Assertions.assertEquals(ProviderType.AZURE, providerConfig.getType());
//        Assertions.assertEquals("http://test.azure.com/moderate", providerConfig.getBaseUrl());
//        Assertions.assertEquals("azureSecret1", providerConfig.getSecret());

        PushConfigResponse pushConfig = response.getPush();
        Assertions.assertNotNull(pushConfig);
        Assertions.assertNull(pushConfig.getPlatform());
        Assertions.assertNull(pushConfig.getSecret());
    }

    @Test
    @DisplayName("it should fetch all saved configurations")
    void validSavedFetchConfigurationsTest() throws ApiException, InvalidKeyException, NoSuchAlgorithmException {
        final ConfigurationsApi client = new ConfigurationsApi(new ApiClient());
        PushConfig pConfig = new PushConfig();
        pConfig.setPlatform(PushPlatformType.FCM);
        pConfig.setSecret("fcm_test");

        final String appId = "validSavedFetchConfigurationsTest";

        TestUtils.signV2(
                client.getApiClient(),
                appId,
                "POST",
                "/v1/configs/push",
                new Gson().toJson(pConfig),
                "secret"
        );

        client.savePushConfig(pConfig);

        TestUtils.signV2(
                client.getApiClient(),
                appId,
                "GET",
                "/v1/configs",
                "",
                "secret"
        );

        ApplicationConfigResponse response = client.fetchConfigurations();
        Assertions.assertNotNull(response);

        PushConfigResponse pushConfig = response.getPush();
        Assertions.assertNotNull(pushConfig);
        Assertions.assertEquals(pConfig.getPlatform(), pushConfig.getPlatform());
        Assertions.assertEquals(pConfig.getSecret(), pushConfig.getSecret());
    }

    @Test
    @DisplayName("it should fetch existing provider config from application service")
    void validFetchProviderConfigTest() throws ApiException, InvalidKeyException, NoSuchAlgorithmException, DomainErrorException {

        final ConfigurationsApi client = new ConfigurationsApi(new ApiClient());

        final ProviderType PROVIDER_TYPE = ProviderType.AZURE;
        final String BASE_URL = "http://somebaseurl.com";
        final String SECRET = "someProviderSecret";
        final ProviderConfig pConfig = new ProviderConfig();
        pConfig.setType(PROVIDER_TYPE);
        pConfig.setBaseUrl(BASE_URL);
        pConfig.setSecret(SECRET);
        final String APP_ID = "appA";

        TestUtils.signV2(
                client.getApiClient(),
                APP_ID,
                "POST",
                "/v1/configs/contentModerators",
                new Gson().toJson(pConfig),
                "secret"
        );

        client.saveContentProviderConfig(pConfig);

        // Wait for Cache to expire
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        TestUtils.signV2(
                client.getApiClient(),
                "appA",
                "GET",
                "/v1/configs/contentModerators",
                "",
                "secret"
        );

        ProviderConfigResponse response1 = client.fetchContentProviderConfig();
        Assertions.assertNotNull(response1);
        Assertions.assertEquals(ProviderType.AZURE, response1.getType());
        Assertions.assertEquals("http://somebaseurl.com", response1.getBaseUrl());
        Assertions.assertEquals("someProviderSecret", response1.getSecret());

    }

    @Test
    @DisplayName("it should publish and fetch and delete provider config from content moderation service")
    void validSaveAndFetchProviderConfigTest() throws ApiException, InvalidKeyException, NoSuchAlgorithmException {

        final ConfigurationsApi client = new ConfigurationsApi(new ApiClient());
        final String APP_ID = "appD";
        final ProviderType PROVIDER_TYPE = ProviderType.AZURE;
        final String BASE_URL = "http://somebaseurl.com";
        final String SECRET = "someProviderSecret";
        final ProviderConfig pConfig = new ProviderConfig();
        pConfig.setType(PROVIDER_TYPE);
        pConfig.setBaseUrl(BASE_URL);
        pConfig.setSecret(SECRET);

        TestUtils.signV2(
                client.getApiClient(),
                APP_ID,
                "POST",
                "/v1/configs/contentModerators",
                new Gson().toJson(pConfig),
                "secret"
        );

        client.saveContentProviderConfig(pConfig);

        TestUtils.signV2(
                client.getApiClient(),
                APP_ID,
                "GET",
                "/v1/configs/contentModerators",
                "",
                "secret"
        );

        ProviderConfigResponse response1 = client.fetchContentProviderConfig();

        Assertions.assertNotNull(response1);
        Assertions.assertEquals(PROVIDER_TYPE, response1.getType());
        Assertions.assertEquals(BASE_URL, response1.getBaseUrl());
        Assertions.assertEquals(SECRET, response1.getSecret());

        TestUtils.signV2(
                client.getApiClient(),
                APP_ID,
                "DELETE",
                "/v1/configs/contentModerators",
                "",
                "secret"
        );

        client.removeContentProviderConfig();

        // Wait for Cache to expire
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        TestUtils.signV2(
                client.getApiClient(),
                APP_ID,
                "GET",
                "/v1/configs/contentModerators",
                "",
                "secret"
        );
        ProviderConfigResponse response2 = client.fetchContentProviderConfig();
        Assertions.assertNotNull(response2);
        Assertions.assertNull(response2.getType());
        Assertions.assertNull(response2.getBaseUrl());
        Assertions.assertNull(response2.getSecret());
    }

    @Test
    @DisplayName("it should not allow invalid params")
    void invalidProviderConfigTest() throws Exception {

        final ConfigurationsApi client = new ConfigurationsApi(new ApiClient());
        final String APP_ID = "appD";
        final ProviderType PROVIDER_TYPE = ProviderType.AZURE;
        final String BASE_URL = "http://somebaseurl.com";
        final String SECRET = "someProviderSecret";
        final ProviderConfig pConfig = new ProviderConfig();

        TestUtils.signV2(
                client.getApiClient(),
                APP_ID,
                "POST",
                "/v1/configs/contentModerators",
                new Gson().toJson(pConfig),
                "secret"
        );

        validateErrorResponse(() -> client.saveContentProviderConfig(pConfig), 400);

        pConfig.setType(PROVIDER_TYPE);
        TestUtils.signV2(
                client.getApiClient(),
                APP_ID,
                "POST",
                "/v1/configs/contentModerators",
                new Gson().toJson(pConfig),
                "secret"
        );
        validateErrorResponse(() -> client.saveContentProviderConfig(pConfig), 400);

        pConfig.setBaseUrl(BASE_URL);
        TestUtils.signV2(
                client.getApiClient(),
                APP_ID,
                "POST",
                "/v1/configs/contentModerators",
                new Gson().toJson(pConfig),
                "secret"
        );
        validateErrorResponse(() -> client.saveContentProviderConfig(pConfig), 400);

        pConfig.setSecret(SECRET);
        TestUtils.signV2(
                client.getApiClient(),
                APP_ID,
                "POST",
                "/v1/configs/contentModerators",
                new Gson().toJson(pConfig),
                "secret"
        );
        client.saveContentProviderConfig(pConfig);
    }

    @Test
    @DisplayName("it should not fetch any push config")
    void defaultFetchPushConfigTest() throws Exception {
        final ConfigurationsApi client = new ConfigurationsApi(new ApiClient());

        TestUtils.signV2(
                client.getApiClient(),
                APP_A,
                "GET",
                "/v1/configs/push/fcm",
                "",
                "secret"
        );

        PushConfigResponse response = client.fetchPushConfig(PushPlatformType.FCM.getValue());

        Assertions.assertNotNull(response);
        Assertions.assertNull(response.getPlatform());
        Assertions.assertNull(response.getSecret());
    }

    @Test
    @DisplayName("it should publish and fetch push config")
    void validSaveAndFetchPushConfigTest() throws ApiException, InvalidKeyException, NoSuchAlgorithmException {
        final ConfigurationsApi client = new ConfigurationsApi(new ApiClient());
        PushConfig pConfig = new PushConfig();
        pConfig.setPlatform(PushPlatformType.FCM);
        pConfig.setSecret("fcm_test");

        String appId = "validSaveAndFetchPushConfigTest";

        TestUtils.signV2(
                client.getApiClient(),
                appId,
                "POST",
                "/v1/configs/push",
                new Gson().toJson(pConfig),
                "secret"
        );
        client.savePushConfig(pConfig);

        TestUtils.signV2(
                client.getApiClient(),
                appId,
                "GET",
                "/v1/configs/push/fcm",
                "",
                "secret"
        );
        PushConfigResponse response = client.fetchPushConfig(PushPlatformType.FCM.getValue());

        Assertions.assertNotNull(response);
        Assertions.assertEquals(response.getPlatform(), PushPlatformType.FCM);
        Assertions.assertEquals(response.getSecret(), "fcm_test");
    }

    @Test
    @DisplayName("it should delete the saved config")
    void validSaveAndDeletePushConfigTest() throws Exception
    {
        final ConfigurationsApi client = new ConfigurationsApi(new ApiClient());
        PushConfig pConfig = new PushConfig();
        pConfig.setPlatform(PushPlatformType.FCM);
        pConfig.setSecret("fcm_test");

        String appId = "validSaveAndDeletePushConfigTest";

        TestUtils.signV2(
                client.getApiClient(),
                appId,
                "POST",
                "/v1/configs/push",
                new Gson().toJson(pConfig),
                "secret"
        );

        client.savePushConfig(pConfig);

        TestUtils.signV2(
                client.getApiClient(),
                appId,
                "DELETE",
                "/v1/configs/push/fcm",
                "",
                "secret"
        );

        client.removePushConfig(PushPlatformType.FCM.getValue());

        TestUtils.signV2(
                client.getApiClient(),
                appId,
                "GET",
                "/v1/configs/push/fcm",
                "",
                "secret"
        );
        PushConfigResponse response = client.fetchPushConfig(PushPlatformType.FCM.getValue());

        Assertions.assertNotNull(response);
        Assertions.assertNull(response.getPlatform());
        Assertions.assertNull(response.getSecret());
    }

    @Test
    @DisplayName("it should not publish invalid push config")
    void invalidSavePushConfigTest() throws Exception {
        final ConfigurationsApi client = new ConfigurationsApi(new ApiClient());
        final PushConfig pConfig1 = new PushConfig();
        pConfig1.setSecret("fcm_test");

        String appId = "invalidSavePushConfigTest";

        TestUtils.signV2(
                client.getApiClient(),
                appId,
                "POST",
                "/v1/configs/push",
                new Gson().toJson(pConfig1),
                "secret"
        );

        validateErrorResponse(() -> client.savePushConfig(pConfig1), 400);

        final PushConfig pConfig2 = new PushConfig();
        pConfig2.setPlatform(PushPlatformType.FCM);

        TestUtils.signV2(
                client.getApiClient(),
                appId,
                "POST",
                "/v1/configs/push",
                new Gson().toJson(pConfig2),
                "secret"
        );
        validateErrorResponse(() -> client.savePushConfig(pConfig2), 400);
    }

    @Test
    @DisplayName("it should not throw exception on deleting invalid platform value")
    void invalidDeletePushConfigTest() throws Exception
    {
        final ConfigurationsApi client = new ConfigurationsApi(new ApiClient());

        String appId = "invalidSavePushConfigTest";

        TestUtils.signV2(
                client.getApiClient(),
                appId,
                "DELETE",
                "/v1/configs/push/fcm",
                "",
                "secret"
        );

        client.removePushConfig(PushPlatformType.FCM.getValue());

        TestUtils.signV2(
                client.getApiClient(),
                appId,
                "GET",
                "/v1/configs/push/fcm",
                "",
                "secret"
        );

        PushConfigResponse response = client.fetchPushConfig(PushPlatformType.FCM.getValue());

        Assertions.assertNotNull(response);
        Assertions.assertNull(response.getPlatform());
        Assertions.assertNull(response.getSecret());
    }

    @Test
    @DisplayName("it should fetch default preferences")
    void defaultPreferencesTest() throws Exception {
        final PreferencesApi client = new PreferencesApi(new ApiClient());

        TestUtils.signV2(
                client.getApiClient(),
                APP_A,
                "GET",
                "/v1/preferences",
                "",
                "secret"
        );
        PreferencesResponse response = client.fetchPreferences();

        Assertions.assertNotNull(response);
        Assertions.assertFalse(response.isForceContentModeration());
    }

    @Test
    @DisplayName("it should publish and fetch new preferences")
    void validSaveAndFetchPreferencesTest() throws ApiException, InvalidKeyException, NoSuchAlgorithmException {
        final PreferencesApi client = new PreferencesApi(new ApiClient());

        Preferences prefs = new Preferences();
        prefs.forceContentModeration(true);

        String appId = "validSaveAndFetchPreferencesTest";
        TestUtils.signV2(
                client.getApiClient(),
                appId,
                "POST",
                "/v1/preferences",
                new Gson().toJson(prefs),
                "secret"
        );
        client.savePreferences(prefs);

        TestUtils.signV2(
                client.getApiClient(),
                appId,
                "GET",
                "/v1/preferences",
                "",
                "secret"
        );
        PreferencesResponse response = client.fetchPreferences();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(prefs.isForceContentModeration(), response.isForceContentModeration());
    }

    @Test
    @DisplayName("it should publish and fetch default preferences")
    void invalidSaveAndFetchPreferencesTest() throws ApiException, InvalidKeyException, NoSuchAlgorithmException {
        final PreferencesApi client = new PreferencesApi(new ApiClient());

        Preferences prefs = new Preferences();

        String appId = "invalidSaveAndFetchPreferencesTest";
        TestUtils.signV2(
                client.getApiClient(),
                appId,
                "POST",
                "/v1/preferences",
                new Gson().toJson(prefs),
                "secret"
        );
        client.savePreferences(prefs);

        TestUtils.signV2(
                client.getApiClient(),
                appId,
                "GET",
                "/v1/preferences",
                "",
                "secret"
        );
        PreferencesResponse response = client.fetchPreferences();

        Assertions.assertNotNull(response);
        Assertions.assertFalse(response.isForceContentModeration());
    }

    @FunctionalInterface
    public interface Executor {
        void get() throws ApiException;
    }

    void validateErrorResponse(final Executor aExecutor, final int aCode) throws Exception {
        try {
            aExecutor.get();
        }
        catch (ApiException ex) {
            Assertions.assertEquals(aCode, ex.getCode());
            return;
        }

        throw new Exception("no exception thrown");
    }
}

