package io.fizz.gateway.services;

import io.fizz.common.domain.CurrencyCode;
import io.fizz.common.domain.DomainErrorException;

public class MockUSDConversionService extends USDConversionService {
    @Override
    public int toCents(final CurrencyCode aCurrencyCode, double aAmount) throws DomainErrorException {
        updateCurrencyMap();
        return super.toCents(aCurrencyCode, aAmount);
    }

    private void updateCurrencyMap() {
        currencyMap.put("USDUSD", 1.0);
    }
}
