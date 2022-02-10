package io.fizz.common.domain;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Platform {
    private static final Set<String> values = new HashSet<String>() {
        {
            add("ios");
            add("android");
            add("windows");
            add("windows_phone");
            add("mac_osx");
            add("web_player");
        }
    };
    private static final DomainErrorException ERROR_UNSUPPORTED_PLATFORM = new DomainErrorException(new DomainError("unsupported_platform"));
    private static final DomainErrorException ERROR_INVALID_PLATFORM = new DomainErrorException(new DomainError("invalid_platform"));

    private final String value;
    public Platform(final String aValue) throws DomainErrorException {
        if (Objects.isNull(aValue)) {
            throw ERROR_INVALID_PLATFORM;
        }
        if (!values.contains(aValue)) {
            throw ERROR_UNSUPPORTED_PLATFORM;
        }

        value = aValue;
    }

    public String value() {
        return value;
    }
}
