package io.fizz.gateway.http.ratelimit;

import java.util.concurrent.CompletableFuture;

public interface AbstractRateLimitRepository {
    CompletableFuture<Integer> incrementRate(final String aKey);
    CompletableFuture<Integer> getRate(final String aKey);
}
