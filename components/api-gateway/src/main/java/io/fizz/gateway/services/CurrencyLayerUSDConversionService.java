package io.fizz.gateway.services;

import io.fizz.common.LoggingService;
import io.fizz.common.domain.CurrencyCode;
import io.fizz.common.domain.DomainErrorException;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This class updates the USD currency conversion rates from https://currencylayer.com/
 */
public class CurrencyLayerUSDConversionService extends USDConversionService {
    private static class CurrencyQuotesResponse {
        final boolean success;
        final String terms;
        final String privacy;
        final long timestamp;
        final String source;
        final Map<String,Double> quotes;

        public CurrencyQuotesResponse(boolean success, String terms, String privacy, long timestamp, String source, Map<String, Double> quotes) {
            this.success = success;
            this.terms = terms;
            this.privacy = privacy;
            this.timestamp = timestamp;
            this.source = source;
            this.quotes = quotes;
        }
    }

    private interface AbstractCurrencyLayerService {
        @GET("/api/live?access_key=d90cc3244727d2f593ad03d6cd156685&format=1")
        Call<CurrencyQuotesResponse> quotes();
    }

    private static final LoggingService.Log logger = LoggingService.getLogger(CurrencyLayerUSDConversionService.class);
    private static final int UPDATE_INTERVAL = 12*60*60*1000; // 12 hours

    private final AbstractCurrencyLayerService service;
    private long lastFetchTS = 0;

    public CurrencyLayerUSDConversionService() {
        final Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("http://www.apilayer.net:80")
        .addConverterFactory(GsonConverterFactory.create())
        .build();

        service = retrofit.create(AbstractCurrencyLayerService.class);
    }

    @Override
    public int toCents(final CurrencyCode aCurrencyCode, double aAmount) throws DomainErrorException {
        updateCurrencyMap();
        return super.toCents(aCurrencyCode, aAmount);
    }

    private void updateCurrencyMap() {
        final long curTime = new Date().getTime();
        if (curTime - lastFetchTS > UPDATE_INTERVAL) {
            try {
                final CurrencyQuotesResponse resp = service.quotes().execute().body();
                lastFetchTS = curTime;
                if (!Objects.isNull(resp)) {
                    currencyMap = new HashMap<>();
                    for (final Map.Entry<String,Double> entry: resp.quotes.entrySet()) {
                        try {
                            currencyMap.put(entry.getKey(), 1.0 / entry.getValue());
                        }
                        catch (Exception ex) {
                            logger.fatal("invalid currency quote encountered: currencyCode: " + entry.getKey() + " value:" + entry.getValue());
                        }
                    }
                }
            }
            catch (IOException ex) {
                logger.fatal("failed to download currency conversion map.");
            }
        }
    }
}
