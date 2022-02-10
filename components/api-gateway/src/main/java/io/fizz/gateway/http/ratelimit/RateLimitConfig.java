package io.fizz.gateway.http.ratelimit;

import io.fizz.gateway.http.annotations.RLScope;

public class RateLimitConfig {

    final private RLScope scope;
    final private int limit;

    public RateLimitConfig(final RLScope aScope, final int aLimit) {
        scope = aScope;
        limit = aLimit;
    }

    public RLScope scope() {
        return scope;
    }

    public int limit() {
        return limit;
    }
}
