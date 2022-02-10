package io.fizz.gateway.services;

import io.fizz.common.domain.CurrencyCode;
import io.fizz.common.domain.DomainErrorException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class USDConversionServiceTest {
    @Test
    @DisplayName("it should convert various currency amounts into USD cents")
    void currencyConversionTest() throws DomainErrorException {
        final USDConversionService service = new USDConversionService();

        assert (service.toCents(new CurrencyCode("PKR"), 100.0) == 86);
        assert (service.toCents(new CurrencyCode("pkr"), 100.0) == 86);

        assert (service.toCents(new CurrencyCode("GBP"), 1) == 133);
        assert (service.toCents(new CurrencyCode("gbp"), 1) == 133);

        assert (service.toCents(new CurrencyCode("EUR"), 1) == 117);
        assert (service.toCents(new CurrencyCode("eur"), 1) == 117);

        assert (service.toCents(new CurrencyCode("JPY"), 100) == 91);
        assert (service.toCents(new CurrencyCode("jpy"), 100) == 91);
    }

    @Test
    @DisplayName("it should throw error for invalid currency code")
    void invalidCurrencyCode() {
        final USDConversionService service = new USDConversionService();

        Assertions.assertThrows(
            DomainErrorException.class,
            () -> service.toCents(new CurrencyCode("xyz"), 100)
        );
    }
}
