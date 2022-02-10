package io.fizz.gdpr.domain;

import io.fizz.common.domain.DomainErrorException;

public enum GDPRRequestStatus {
    SCHEDULED("scheduled"),
    CANCELLED("cancelled"),
    COMPLETED("completed");

    private final String value;

    GDPRRequestStatus(final String aValue) {
        value = aValue;
    }

    public String value() {
        return value;
    }

    public static GDPRRequestStatus fromValue(final String aValue) throws DomainErrorException {
        for (GDPRRequestStatus status: GDPRRequestStatus.values()) {
            if (status.value.equals(aValue)) {
                return status;
            }
        }

        throw new DomainErrorException(String.format("invalid_gdpr_status_%s", aValue));
    }
}