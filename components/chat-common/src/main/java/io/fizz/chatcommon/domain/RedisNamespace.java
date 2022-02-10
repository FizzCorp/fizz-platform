package io.fizz.chatcommon.domain;

import io.fizz.common.domain.DomainErrorException;

public enum RedisNamespace {
    CHAT("chat"),
    PRESENCE("presence"),
    RATE_LIMIT("ratelimit");

    private String value;
    RedisNamespace(final String aValue) {
        value = aValue;
    }

    public String value() {
        return value;
    }

    public static RedisNamespace fromValue(final String aValue) throws DomainErrorException {
        for (RedisNamespace namespace: RedisNamespace.values()) {
            if (namespace.value.equals(aValue)) {
                return namespace;
            }
        }

        throw new DomainErrorException(String.format("invalid_redis_namespace_code_%s", aValue));
    }
}