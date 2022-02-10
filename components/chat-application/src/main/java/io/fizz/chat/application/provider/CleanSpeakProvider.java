package io.fizz.chat.application.provider;

import io.fizz.chat.application.domain.AbstractProviderConfig;
import io.fizz.chat.application.domain.CleanSpeakProviderConfig;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.apache.http.HttpStatus;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class CleanSpeakProvider implements AbstractProvider {

    private static final String CS_RESOURCE_URI = "%s/content/item/moderate";

    private CleanSpeakProviderConfig config;

    public CleanSpeakProvider(final AbstractProviderConfig aConfig) {
        Utils.assertRequiredArgument(aConfig, "invalid_config");

        config = (CleanSpeakProviderConfig) aConfig;
    }

    @Override
    public CompletableFuture<String> filter(final ApplicationId aAppId, final WebClient aClient, final String aText) {
        if (aText.isEmpty()) {
            return CompletableFuture.completedFuture("");
        }

        final CompletableFuture<String> resultFuture = new CompletableFuture<>();
        final String url = String.format(CS_RESOURCE_URI, config.baseUrl());

        final JsonArray contentParts = new JsonArray();
        final JsonObject contentPart = new JsonObject();
        contentPart.put("content", aText);
        contentPart.put("type", "text");
        contentParts.add(contentPart);

        final JsonObject contentJson = new JsonObject();
        contentJson.put("applicationId", config.appId());
        contentJson.put("createInstant", System.currentTimeMillis());
        contentJson.put("senderId", aAppId.value());
        contentJson.put("parts", contentParts);

        final JsonObject body = new JsonObject();
        body.put("content", contentJson);

        final HttpRequest<Buffer> request = aClient.postAbs(url);

        request.putHeader("content-length", Integer.toString(body.toString().length()));
        request.putHeader("content-type", "application/json");
        request.putHeader("Authorization", config.secret());

        request.sendJsonObject(body, aResult -> {
            if (aResult.failed()) {
                resultFuture.completeExceptionally(aResult.cause());
            }
            else {
                final HttpResponse<Buffer> response = aResult.result();

                if (response.statusCode() == HttpStatus.SC_OK) {
                    resultFuture.complete(parseResult(response.bodyAsJsonObject(), aText));
                }
                else {
                    if (Objects.nonNull(aResult.cause())) {
                        resultFuture.completeExceptionally(aResult.cause());
                    }
                    else {
                        resultFuture.completeExceptionally(new Exception("Cleanspeak moderation failed"));
                    }
                }
            }
        });

        return resultFuture;
    }

    private String parseResult(final JsonObject aBody, final String aOriginalText) {
        String filteredText = aOriginalText;

        final JsonObject contentJson = aBody.getJsonObject("content");
        final String contentAction = aBody.getString("contentAction");
        switch (contentAction) {
            case "allow":
            case "authorOnly":
            case "queuedForApproval":
                break;

            case "replace":
                filteredText = getReplacementText(contentJson, aOriginalText);
                break;

            case "reject":
                filteredText = "***";
                break;
        }

        return filteredText;
    }

    private String getReplacementText(final JsonObject aContentJson, final String aOriginalText) {
        String filteredText = aOriginalText;

        final JsonArray contentPartsArray = aContentJson.getJsonArray("parts");
        if (contentPartsArray.size() > 0) {
            final JsonObject partJson = contentPartsArray.getJsonObject(0);
            filteredText = partJson.getString("replacement");
            final JsonArray matches = partJson.getJsonArray("matches");

            boolean skip = false;
            for (int mi = 0; mi < matches.size(); mi++) {
                final JsonObject match = matches.getJsonObject(mi);
                final JsonArray tags = match.getJsonArray("tags");

                if (Objects.nonNull(tags)) {
                    for (int ti = 0; ti < tags.size(); ti++) {
                        if (tags.getString(ti).equalsIgnoreCase("PII")) {
                            filteredText = "***";
                            skip = true;
                            break;
                        }
                    }
                }
                if (skip) {
                    break;
                }
            }
        }

        return filteredText;
    }
}
