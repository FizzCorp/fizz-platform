package io.fizz.gateway.http.auth;

import io.fizz.common.domain.DomainError;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.gateway.services.keycloak.AbstractKCService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

public class SignatureAuthProvider implements AuthProvider {

    private AbstractKCService kcService;

    private DomainError invalidCredentials = new DomainError("invalid_credentials");
    public SignatureAuthProvider(AbstractKCService kcService) {
        this.kcService = kcService;
    }
    @Override
    public void authenticate(JsonObject jsonObject, Handler<AsyncResult<User>> handler) {
        String appId = jsonObject.getString("appId");
        String signature = jsonObject.getString("signature");
        String payload = jsonObject.getString("payload");

        boolean success = false;
        boolean isHandled = false;
        try {
            success = kcService.authenticate(appId, payload, signature);
        } catch (Exception e) {
            handler.handle(Future.failedFuture(e));
            isHandled = true;
        }
        if (isHandled)
            return;
        if (success)
            handler.handle(Future.succeededFuture(new AuthenticatedUser(appId, "system", kcService)));
        else
            handler.handle(Future.failedFuture(new DomainErrorException(invalidCredentials)));

    }
}
