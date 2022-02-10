package io.fizz.chat.infrastructure.translation;

import io.fizz.chat.application.channel.AbstractTranslationClient;
import io.fizz.chat.infrastructure.ConfigService;
import io.fizz.chatcommon.domain.LanguageCode;
import io.fizz.common.LoggingService;
import io.fizz.common.domain.DomainErrorException;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.apache.hadoop.hbase.shaded.org.apache.http.HttpStatus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class BingTranslationClient implements AbstractTranslationClient {
    private static final LoggingService.Log logger = LoggingService.getLogger(BingTranslationClient.class);
    private static final String BING_SUBSCRIPTION_KEY = ConfigService.config().getString("chat.bing.subscription.key");

    private final WebClient client;

    public BingTranslationClient(final Vertx aVertx) {
        if (Objects.isNull(aVertx)) {
            throw new IllegalArgumentException("invalid vertx instance specified.");
        }

        client = WebClient.create(aVertx);
    }

    @Override
    public CompletableFuture<Map<LanguageCode,String>> translate(final String aText, final LanguageCode aFrom, final LanguageCode[] aTo) {
        if (Objects.isNull(aText) || aText.length() <= 0) {
            return CompletableFuture.completedFuture(new HashMap<>());
        }

        if (Objects.isNull(aTo) || aTo.length <= 0) {
            return CompletableFuture.completedFuture(new HashMap<>());
        }

        final Buffer body = buildRequestBody(new String[]{ aText });
        final HttpRequest<Buffer> request = client
                .post(443, "api.cognitive.microsofttranslator.com", "/translate")
                .ssl(true)
                .putHeader("Ocp-Apim-Subscription-Key", BING_SUBSCRIPTION_KEY)
                .putHeader("Content-Type", "application/json")
                .putHeader("Content-Length", Integer.toString(body.length()))
                .addQueryParam("api-version", "3.0");

        if (Objects.nonNull(aFrom)) {
            request.addQueryParam("from", aFrom.value());
        }

        for (LanguageCode code: aTo) {
            request.addQueryParam("to", code.value());
        }


        final CompletableFuture<Map<LanguageCode,String>> translated = new CompletableFuture<>();


        request.sendBuffer(body, aResult -> {
            if (aResult.failed()) {
                translated.completeExceptionally(new CompletionException(aResult.cause()));
            } else {
                try {
                    final HttpResponse<Buffer> response = aResult.result();
                    if (response.statusCode() == HttpStatus.SC_OK) {
                        final JsonArray responseBody = response.bodyAsJsonArray();
                        translated.complete(parseTranslatedTexts(responseBody));
                    }
                    else {
                        final JsonObject responseBody = response.bodyAsJsonObject();
                        final JsonObject error = responseBody.getJsonObject("error");
                        String message = "translation error";

                        if (Objects.nonNull(error)) {
                            message = error.getString("message");
                        }

                        logger.fatal(message);
                        translated.completeExceptionally(new CompletionException(new IOException(message)));
                    }
                } catch (ClassCastException ex) {
                    translated.completeExceptionally(new CompletionException(ex));
                }
            }
        });

        return translated;
    }

    private Map<LanguageCode,String> parseTranslatedTexts(final JsonArray aTextTranslations) {
        final Map<LanguageCode, String> texts = new HashMap<>();

        for (int ti = 0; ti < aTextTranslations.size(); ti++) {
            final JsonObject textTranslation = aTextTranslations.getJsonObject(ti);
            final JsonArray translations = textTranslation.getJsonArray("translations");

            for (int tri = 0; tri < translations.size(); tri++) {
                final JsonObject translation = translations.getJsonObject(tri);
                try {
                    final LanguageCode code = LanguageCode.fromValue(translation.getString("to"));
                    texts.put(code, translation.getString("text"));
                }
                catch (DomainErrorException ex) {
                    logger.error("Unsupported language code detected: " + translation.getString("to"));
                }
            }
        }

        return texts;
    }

    private Buffer buildRequestBody(final String[] aTexts) {
        final JsonArray textArray = new JsonArray();

        for (String text: aTexts) {
            textArray.add(new JsonObject().put("Text", text));
        }

        return Buffer.buffer(textArray.toString());
    }
}
