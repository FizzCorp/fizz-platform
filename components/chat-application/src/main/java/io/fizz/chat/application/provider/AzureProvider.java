package io.fizz.chat.application.provider;


import io.fizz.chat.application.domain.AbstractProviderConfig;
import io.fizz.chat.application.domain.AzureProviderConfig;
import io.fizz.chatcommon.infrastructure.ConfigService;
import io.fizz.common.Config;
import io.fizz.common.LoggingService;
import io.fizz.common.domain.ApplicationId;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.apache.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class AzureProvider implements AbstractProvider {

    protected static final LoggingService.Log logger = LoggingService.getLogger(AzureProvider.class);

    private static final Config CONFIG = ConfigService.config();
    private static final String CONTENT_REPLACE_STRING = CONFIG.getString("content.moderation.replacement.string");

    private static final String AZURE_RESOURCE_URL = "%s/moderate/v1.0/ProcessText/Screen?PII=true";

    private final AzureProviderConfig azureConfig;

    public AzureProvider(final AbstractProviderConfig aConfig) {
        this.azureConfig = (AzureProviderConfig) aConfig;
    }

    @Override
    public CompletableFuture<String> filter(final ApplicationId aAppId,
                                            final WebClient aClient,
                                            final String aText) {

        if (aText.isEmpty()) {
            return CompletableFuture.completedFuture("");
        }

        CompletableFuture<String> resultFuture = new CompletableFuture<>();
        String absUrl = String.format(AZURE_RESOURCE_URL, azureConfig.baseUrl());

        final HttpRequest<Buffer> request = aClient.postAbs(absUrl);
        request.putHeader("content-length", Integer.toString(aText.length()));
        request.putHeader("content-type", "text/plain");
        request.putHeader("Ocp-Apim-Subscription-Key", azureConfig.secret());
        request.sendBuffer(Buffer.buffer(aText), aResult -> {
            if (aResult.failed()) {
                resultFuture.completeExceptionally(aResult.cause());
            }
            else {
                final HttpResponse<Buffer> response = aResult.result();
                if (response.statusCode() == HttpStatus.SC_OK) {
                    final JsonObject resultJson = response.bodyAsJsonObject();
                    List<String> terms = getFilteredTerms(resultJson);
                    resultFuture.complete(filteredText(terms, aText));
                }
                else {
                    if (Objects.nonNull(aResult.cause())) {
                        resultFuture.completeExceptionally(aResult.cause());
                    }
                    else {
                        resultFuture.completeExceptionally(new Exception("Azure moderation failed"));
                    }
                }
            }
        });
        return resultFuture;
    }

    private List<String> getFilteredTerms(final JsonObject aJson) {
        List<String> terms = new ArrayList<>();
        JsonObject PPIJson = aJson.getJsonObject("PII");
        terms.addAll(terms(PPIJson, "Email", "Text"));
        terms.addAll(terms(PPIJson, "IPA", "Text"));
        terms.addAll(terms(PPIJson, "Phone", "Text"));
        terms.addAll(terms(PPIJson, "Address", "Text"));
        terms.addAll(terms(PPIJson, "SSN", "Text"));
        terms.addAll(terms(aJson, "Terms", "Term"));
        return terms;
    }

    private List<String> terms(final JsonObject aJson,
                               final String aGroupKey,
                               final String aTermKey) {

        List<String> terms = new ArrayList<>();
        if (Objects.nonNull(aJson)) {
            JsonArray group = aJson.getJsonArray(aGroupKey);
            if (Objects.nonNull(group)) {
                for (int itemId = 0; itemId < group.size(); itemId++) {
                    terms.add(group.getJsonObject(itemId).getString(aTermKey));
                }
            }
        }
        return terms;
    }

    String filteredText(final List<String> aTerms,
                        final String aText) {

        String filteredText = aText;
        for (int i = aTerms.size() - 1; i >= 0; i--) {
            String term = aTerms.get(i);
            if (Objects.nonNull(term)) {
                String caseInsensitiveTerm = String.format("(?i)%s", Pattern.quote(term));
                filteredText = filteredText.replaceAll(caseInsensitiveTerm, CONTENT_REPLACE_STRING);
            }
        }
        return filteredText;
    }
}
