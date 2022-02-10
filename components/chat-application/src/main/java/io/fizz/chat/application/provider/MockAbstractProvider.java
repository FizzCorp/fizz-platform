package io.fizz.chat.application.provider;

import io.fizz.common.domain.ApplicationId;
import io.vertx.ext.web.client.WebClient;

import java.util.concurrent.CompletableFuture;

public class MockAbstractProvider implements AbstractProvider {
    private boolean markError;

    public void markError(boolean aMarkError) {
        this.markError = aMarkError;
    }

    @Override
    public CompletableFuture<String> filter(final ApplicationId aAppId, final WebClient aClient, final String aText) {
        CompletableFuture<String> future = new CompletableFuture<>();
        if (markError) {
            future.completeExceptionally(new Exception());
        } else {
            future.complete("success");
        }
        return future;
    }
}
