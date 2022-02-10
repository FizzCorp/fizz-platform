package io.fizz.chat.moderation.application.service;

import io.fizz.chat.application.impl.ApplicationService;
import io.fizz.chat.moderation.infrastructure.service.HystrixContentModerationClient;
import io.fizz.common.LoggingService;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ContentModerationService {
    protected static final LoggingService.Log logger = LoggingService.getLogger(ContentModerationService.class);

    private static final int MAX_TEXTS_PER_REQUEST = 5;
    private static final int MAX_LENGTH_PER_TEXT = 1024;

    private final WebClient client;
    private ApplicationService appService;

    public ContentModerationService(final ApplicationService aAppService,
                                    final Vertx aVertx) {

        Utils.assertRequiredArgument(aAppService, "invalid_application_service");
        this.appService = aAppService;

        client = buildWebClient(aVertx);
    }

    private WebClient buildWebClient(final Vertx aVertx) {
        return WebClient.create(aVertx);
    }

    public CompletableFuture<String> filter(final ApplicationId aAppId,
                                            final String aText) {

        try {
            Utils.assertRequiredArgument(aAppId, "invalid_application_id");
            Utils.assertRequiredArgumentLength(aText, MAX_LENGTH_PER_TEXT, "invalid_text_length");

            return appService.fetchProviderConfigOrDefault(aAppId)
                    .thenCompose(provider -> new HystrixContentModerationClient(provider)
                            .filter(aAppId, client, aText));
        } catch (Exception ex) {
            return Utils.failedFuture(ex);
        }
    }

    public CompletableFuture<List<String>> filter(final String aAppId,
                                               final List<String> aTexts) {

        try {
            final ApplicationId appId = new ApplicationId(aAppId);

            Utils.assertArgumentRange(aTexts.size(), 0, MAX_TEXTS_PER_REQUEST, "invalid_texts_array_size");
            for (int ti = 0; ti < aTexts.size(); ti++) {
                Utils.assertRequiredArgumentLength(aTexts.get(ti), MAX_LENGTH_PER_TEXT, "invalid_text_length");
            }

            return appService.fetchProviderConfigOrDefault(appId)
                    .thenCompose(provider -> {
                        final CompletableFuture<List<String>> resultFuture = new CompletableFuture<>();
                        final List<CompletableFuture<String>> futures = new ArrayList<>();
                        final Map<CompletableFuture<String>, String> requestMap = new HashMap<>();

                        for (int ti = 0; ti < aTexts.size(); ti++) {
                            String text = aTexts.get(ti);
                            CompletableFuture<String> request = new HystrixContentModerationClient(provider)
                                    .filter(appId, client, text);
                            futures.add(request);
                            requestMap.put(request, text);
                        }

                        CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]))
                                .handle((v, error) -> {
                                    try {
                                        if (Objects.nonNull(error)) {
                                            logger.error(error.getMessage());
                                            resultFuture.complete(aTexts);
                                            return null;
                                        }
                                        List<String> result = new ArrayList<>();
                                        for (CompletableFuture<String> future : futures) {
                                            result.add(future.isCompletedExceptionally() ? requestMap.get(future) : future.get());
                                        }

                                        resultFuture.complete(result);
                                    } catch (Exception ex) {
                                        logger.error(ex.getMessage());
                                        resultFuture.complete(aTexts);
                                    }
                                    return null;
                                });
                        return resultFuture;
                    });
        } catch (Exception ex) {
            return Utils.failedFuture(ex);
        }
    }
}
