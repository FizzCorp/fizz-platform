package io.fizz.common.domain;

import java.util.Objects;

public class CurrencyCode {
    public static final DomainErrorException ERROR_INVALID_CURRENCY_CODE = new DomainErrorException(new DomainError("invalid_currency_code"));

    private final String value;
    public CurrencyCode(final String aValue) throws DomainErrorException {
        if (Objects.isNull(aValue) || aValue.length() <= 0 || aValue.length() > 3) {
            throw ERROR_INVALID_CURRENCY_CODE;
        }
        value = aValue;
    }

    public String value() {
        return value;
    }
}
