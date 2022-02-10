package io.fizz.gateway.http.controllers;

import io.fizz.chat.application.services.TranslationService;
import io.fizz.chatcommon.domain.LanguageCode;
import io.fizz.chatcommon.infrastructure.WebUtils;
import io.fizz.common.Utils;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.gateway.http.annotations.AsyncRestController;
import io.fizz.gateway.http.annotations.AuthScheme;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class TranslateController extends AbstractRestController {
    private final TranslationService translationService;

    public TranslateController(final Vertx aVertx,
                               final TranslationService aTranslationService) {
        super(aVertx);

        Utils.assertRequiredArgument(aTranslationService, "invalid_translation_service");

        translationService = aTranslationService;
    }


    @AsyncRestController(path="/translations", method= HttpMethod.POST, auth=AuthScheme.SESSION_TOKEN)
    CompletableFuture<Void> onTranslateText(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            try {
                final JsonObject params = aContext.getBodyAsJson();
                final JsonArray targetLanguages = params.getJsonArray("to");
                final String sourceLanguages = params.getString("from");
                final String text = params.getString("text");

                Utils.assertRequiredArgument(targetLanguages, "invalid_to");
                Utils.assertRequiredArgument(text, "invalid_text");
                Utils.assertArgumentRange(text.length(), 0, 1024, "invalid_text_length");

                final Set<LanguageCode> languages = new HashSet<>();
                for (int li = 0; li < targetLanguages.size(); li++) {
                    languages.add(LanguageCode.fromValue(targetLanguages.getString(li)));
                }

                LanguageCode from = Objects.isNull(sourceLanguages) ? null : LanguageCode.fromValue(sourceLanguages);

                return translationService.translate(text, from, languages.toArray(new LanguageCode[0]))
                        .thenCompose(aTranslationsMap -> {
                            final JsonArray translations = new JsonArray();

                            for (Map.Entry<LanguageCode,String> entry: aTranslationsMap.entrySet()) {
                                translations.add(
                                    new JsonObject()
                                            .put("to", entry.getKey().value())
                                            .put("text", entry.getValue())
                                );
                            }

                            WebUtils.doOK(aResponse, new JsonObject().put("translations", translations));

                            return CompletableFuture.completedFuture(null);
                        });
            }
            catch (DomainErrorException ex) {
                return Utils.failedFuture(ex);
            }
        });
    }
}
