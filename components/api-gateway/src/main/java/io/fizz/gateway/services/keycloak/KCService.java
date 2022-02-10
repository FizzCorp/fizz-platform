package io.fizz.gateway.services.keycloak;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.fizz.common.LoggingService;
import io.fizz.common.Utils;
import io.fizz.gateway.services.keycloak.retrofit2.IKCRetrofit2Service;
import io.fizz.gateway.services.keycloak.retrofit2.KCServiceFactory;
import retrofit2.Response;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

public class KCService implements AbstractKCService {
    private final IKCRetrofit2Service service;
    private final String realm;
    private final String id;
    private final String clientId;
    private final String clientSecret;

    private static LoggingService.Log logger = LoggingService.getLogger(KCService.class);

    @SuppressWarnings("UnstableApiUsage")
    private LoadingCache<String, String> cache = CacheBuilder.newBuilder().build(new CacheLoader<String, String>() {
        @Override
        public String load(final String appId) throws Exception {
            Response<KCModels.Secret> res = service.secret("Bearer " + token(), realm, appId).execute();

            if (!res.isSuccessful() || res.body() == null) {
                throw new KCAPIExceptions.ClientNotFound(appId);
            }

            return res.body().value;
        }
    });

    public KCService(final URL url, final String realm, final String id, final String clientId, final String clientSecret) {
        this.service = KCServiceFactory.build(url);
        this.realm = realm;
        this.id = id;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public boolean authenticate(final String applicationId, final String payload, final String signature) throws Exception {
        String secret = secret(applicationId);
        return verify(payload, secret, signature);
    }

    @Override
    public boolean isPermitted(final String applicationId, final String scope) {
        return true;
    }

    private boolean verify(final String payload, final String secret, final String signature) throws Exception {

        if (secret.isEmpty()) return false;
        if (signature.isEmpty()) return  false;

        final String computedSignature = Utils.computeHMACSHA256(payload, secret);

        return computedSignature.equals(signature);
    }

    private String token() throws Exception {
        Response<KCModels.Token> res = service.token(
                this.realm,
                "client_credentials",
                this.id,
                this.clientSecret)
                .execute();
        if (!res.isSuccessful()) {
            logger.fatal("Unable to get token: " + res.raw().toString());
            throw new KCAPIExceptions.OperationalError(res.message());
        }
        return Objects.requireNonNull(res.body()).access_token;
    }

    private String secret(String applicationId) {
        return cache.getUnchecked(applicationId);
    }
}
