package io.fizz.common.domain;

import java.util.Objects;

public class UnauthorizedException extends RuntimeException {
    private final DomainError error;
    public UnauthorizedException(final String aErrorValue) {
        this(new DomainError(aErrorValue));
    }

    public UnauthorizedException(final DomainError aError) {
        if (Objects.isNull(aError)) {
            throw new IllegalArgumentException("invalid error specified.");
        }
        error = aError;
    }

    public DomainError error() {
        return error;
    }

    @Override
    public String getMessage() {
        return error.reason();
    }
}
