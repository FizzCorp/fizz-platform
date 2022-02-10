package io.fizz.gateway.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.fizz.common.LoggingService;
import io.fizz.common.domain.CurrencyCode;
import io.fizz.common.domain.DomainError;
import io.fizz.common.domain.DomainErrorException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class USDConversionService {
    private static final LoggingService.Log logger = LoggingService.getLogger(USDConversionService.class);
    private static final DomainErrorException ERROR_UNSUPPORTED_CURRENCY = new DomainErrorException(new DomainError("unsupported_currency_code"));

    Map<String,Double> currencyMap = new HashMap<>();

    USDConversionService() {
        final ClassLoader loader = getClass().getClassLoader();
        try(Reader reader = new InputStreamReader(Objects.requireNonNull(loader.getResourceAsStream("currency.quote")))) {
            final Gson gson = new GsonBuilder().create();
            Type type = new TypeToken<Map<String,Double>>(){}.getType();
            final Map<String,Double> quotes = gson.fromJson(reader, type);

            for (final Map.Entry<String,Double> entry: quotes.entrySet()) {
                try {
                    currencyMap.put(entry.getKey(), 1.0 / entry.getValue());
                }
                catch (Exception ex) {
                    logger.fatal("invalid currency quote encountered: currencyCode: " + entry.getKey() + " value:" + entry.getValue());
                }
            }
        }
        catch (IOException ex) {
            logger.fatal("missing currency.json file in resources folder.");
        }
    }

    public int toCents(final CurrencyCode aCurrencyCode, double aAmount) throws DomainErrorException {
       if (Objects.isNull(aCurrencyCode)) {
           throw CurrencyCode.ERROR_INVALID_CURRENCY_CODE;
       }

       final String currencyKey = "USD" + aCurrencyCode.value().toUpperCase();
       if (!currencyMap.containsKey(currencyKey)) {
           throw ERROR_UNSUPPORTED_CURRENCY;
       }

       final double factor = currencyMap.get(currencyKey);

       return (int)(factor*aAmount*100);
   }
}
