package io.fizz.analytics.common.opentsdb.retrofit2;

import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.net.URL;

public class TSDBServiceFactory {
    public static ITSDBRetrofit2Service service = null;

    public static ITSDBRetrofit2Service build (URL url) {
        if (service != null) {
            return service;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url.toString())
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        service = retrofit.create(ITSDBRetrofit2Service.class);

        return service;
    }
}
