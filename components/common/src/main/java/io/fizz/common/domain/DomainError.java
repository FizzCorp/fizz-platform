package io.fizz.common.domain;

import java.util.Objects;

public class DomainError {
    private final String reason;
    public DomainError(final String aReason){
        if (Objects.isNull(aReason)) {
            throw new IllegalArgumentException("invalid reason specified.");
        }
        reason = aReason;
    }

    public String reason() {
        return reason;
    }
}
