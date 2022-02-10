package io.fizz.gateway.services.keycloak.retrofit2;

import io.fizz.gateway.services.keycloak.KCModels;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface IKCRetrofit2Service {
    @FormUrlEncoded
    @POST("/auth/realms/{realm_name}/protocol/openid-connect/token")
    Call<KCModels.Token> token(@Path("realm_name") String realm,
                               @Field("grant_type") String grantType,
                               @Field("client_id") String clientId,
                               @Field("client_secret") String clientSecret);

    @Headers("Content-Type: application/json")
    @GET("/auth/admin/realms/{realm_name}/clients/{of_client}/scope-mappings/clients/{on_client}")
    Call<List<KCModels.Role>> role(@Header("Authorization") String authorization,
                                   @Path("realm_name") String realm,
                                   @Path("of_client") String ofClient,
                                   @Path("on_client") String onClient);

    @Headers("Content-Type: application/json")
    @GET("auth/admin/realms/{realm_name}/clients/{client}/client-secret")
    Call<KCModels.Secret> secret(@Header("Authorization") String authorization,
                                 @Path("realm_name") String realm,
                                 @Path("client") String secret);
}
