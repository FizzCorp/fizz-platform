package io.fizz.chat.application.domain;

import io.fizz.common.domain.DomainErrorException;

public enum ProviderType {
    Azure(0),
    CleanSpeak(1);

    private int value;
    ProviderType(int aValue) {
        value = aValue;
    }

    public int value() {
        return value;
    }

    public static ProviderType fromValue(final int aValue) throws DomainErrorException {
        for (ProviderType type: ProviderType.values()) {
            if (type.value == aValue) {
                return type;
            }
        }
        throw new DomainErrorException(String.format("invalid_provider_type_%s", aValue));
    }
}
