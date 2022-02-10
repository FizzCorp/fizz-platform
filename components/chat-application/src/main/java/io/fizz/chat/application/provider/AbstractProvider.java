package io.fizz.chat.application.provider;

import io.fizz.common.domain.ApplicationId;
import io.vertx.ext.web.client.WebClient;

import java.util.concurrent.CompletableFuture;

public interface AbstractProvider {
    CompletableFuture<String> filter(final ApplicationId aAppId,
                                     final WebClient aClient,
                                     final String aText);
}
