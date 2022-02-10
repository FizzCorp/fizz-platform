package io.fizz.common.domain;

import java.util.Objects;

public class DomainErrorException extends Exception {
    private final DomainError error;
    public DomainErrorException(final String aErrorValue) {
        this(new DomainError(aErrorValue));
    }

    public DomainErrorException(final DomainError aError) {
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
