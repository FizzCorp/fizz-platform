package io.fizz.common.domain;

import java.util.Objects;

public class CountryCode {
    private static final String COUNTRY_CODE_UNKNOWN = "??";
    public static final DomainErrorException ERROR_INVALID_COUNTRY_CODE = new DomainErrorException(new DomainError("invalid_country_code"));

    private final String value;

    private CountryCode(){
        value = COUNTRY_CODE_UNKNOWN;
    }

    public CountryCode(final String aValue) throws DomainErrorException {
        if (Objects.isNull(aValue) || aValue.length() <= 0 || aValue.length() > 2) {
            throw ERROR_INVALID_COUNTRY_CODE;
        }
        value = aValue;
    }

    public String value() {
        return value;
    }

    public static CountryCode unknown() {
        return new CountryCode();
    }
}
