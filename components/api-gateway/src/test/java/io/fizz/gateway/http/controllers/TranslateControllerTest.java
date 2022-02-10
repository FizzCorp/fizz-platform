package io.fizz.gateway.http.controllers;

import com.google.gson.Gson;
import io.fizz.gateway.Constants;
import io.fizz.gateway.http.MockApplication;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.AuthApi;
import io.swagger.client.api.TranslateApi;
import io.swagger.client.model.*;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
class TranslateControllerTest {
    private static Vertx vertx;
    private static String sessionToken1;
    private static final String APP_ID = "appA";
    private static final String USER_ID_1 = "user1";

    static private Future<String> deployVertex() {
        Future<String> future = Future.future();
        vertx.deployVerticle(MockApplication.class.getName(), future);
        return future;
    }

    @BeforeAll
    static void setUp(VertxTestContext aContext) throws InterruptedException {
        vertx = Vertx.vertx();

        deployVertex()
                .setHandler(ar -> {
                    if (ar.succeeded()) {
                        aContext.completeNow();
                    } else {
                        aContext.failNow(ar.cause());
                    }
                });

        Assertions.assertTrue(aContext.awaitCompletion(Constants.TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @AfterAll
    static void tearDown(VertxTestContext aContext) {
        vertx.close(res -> aContext.completeNow());
    }

    @BeforeEach
    void init() throws ApiException {
        if (Objects.nonNull(sessionToken1)) {
            return;
        }
        final AuthApi api = new AuthApi(new ApiClient());
        final SessionAuthRequest request = new SessionAuthRequest();

        request.setUserId(USER_ID_1);
        request.setAppId(APP_ID);
        request.setLocale(LanguageCode.EN);
        api.getApiClient().addDefaultHeader(
                "Authorization",
                "HMAC-SHA256 " + TestUtils.createSignature(new Gson().toJson(request), "secret")
        );

        sessionToken1 = api.createSession(request).getToken();
    }

    @Test
    @DisplayName("it should translate the text correctly")
    void textTranslationDefaultFrom() throws ApiException {
        final TranslateApi api = new TranslateApi(new ApiClient());
        final TranslateTextRequest request = new TranslateTextRequest();

        api.getApiClient().setApiKey(sessionToken1);

        request.setText("Hello1");
        request.addToItem(LanguageCode.ES);
        request.addToItem(LanguageCode.FR);
        request.addToItem(LanguageCode.AR);
        final TranslateTextResponse response = api.translateText(request);

        final List<TranslatedText> translations = response.getTranslations();
        Assertions.assertEquals(translations.size(), 3);

        final Map<String,String> transMap = new HashMap<>();
        for (TranslatedText text: translations) {
            transMap.put(text.getTo(), text.getText());
        }

        Assertions.assertEquals(transMap.get(LanguageCode.ES.toString()), "Hello1_en_es");
        Assertions.assertEquals(transMap.get(LanguageCode.FR.toString()), "Hello1_en_fr");
        Assertions.assertEquals(transMap.get(LanguageCode.AR.toString()), "Hello1_en_ar");
    }

    @Test
    @DisplayName("it should translate the text correctly")
    void textTranslationWithFrom() throws ApiException {
        final TranslateApi api = new TranslateApi(new ApiClient());
        final TranslateTextRequest request = new TranslateTextRequest();

        api.getApiClient().setApiKey(sessionToken1);

        request.setText("Hello1");
        request.from(LanguageCode.FR);
        request.addToItem(LanguageCode.ES);
        request.addToItem(LanguageCode.EN);
        request.addToItem(LanguageCode.AR);
        final TranslateTextResponse response = api.translateText(request);

        final List<TranslatedText> translations = response.getTranslations();
        Assertions.assertEquals(translations.size(), 3);

        final Map<String,String> transMap = new HashMap<>();
        for (TranslatedText text: translations) {
            transMap.put(text.getTo(), text.getText());
        }

        Assertions.assertEquals(transMap.get(LanguageCode.ES.toString()), "Hello1_fr_es");
        Assertions.assertEquals(transMap.get(LanguageCode.EN.toString()), "Hello1_fr_en");
        Assertions.assertEquals(transMap.get(LanguageCode.AR.toString()), "Hello1_fr_ar");
    }

    @Test
    void emptyTargetLanguagesTest() throws ApiException {
        final TranslateApi api = new TranslateApi(new ApiClient());
        final TranslateTextRequest request = new TranslateTextRequest();

        api.getApiClient().setApiKey(sessionToken1);

        request.setText("Hello");
        final TranslateTextResponse response = api.translateText(request);

        final List<TranslatedText> translations = response.getTranslations();
        Assertions.assertEquals(translations.size(), 0);
    }

    @Test
    void emptyTextTest() throws ApiException {
        final TranslateApi api = new TranslateApi(new ApiClient());
        final TranslateTextRequest request = new TranslateTextRequest();

        api.getApiClient().setApiKey(sessionToken1);

        request.addToItem(LanguageCode.ES);
        request.setText("");

        final TranslateTextResponse response = api.translateText(request);
        Assertions.assertEquals(response.getTranslations().size(), 0);
    }
}
