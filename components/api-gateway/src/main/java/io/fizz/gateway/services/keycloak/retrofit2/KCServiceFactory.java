package io.fizz.gateway.services.keycloak.retrofit2;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.net.URL;

public class KCServiceFactory {
    public static IKCRetrofit2Service service = null;

    public static IKCRetrofit2Service build(URL url) {
        if (service != null) {
            return service;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url.toString())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(IKCRetrofit2Service.class);

        return service;
    }
}
