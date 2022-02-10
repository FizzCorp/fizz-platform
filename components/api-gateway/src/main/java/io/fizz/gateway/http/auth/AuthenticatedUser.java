package io.fizz.gateway.http.auth;

import io.fizz.common.Utils;
import io.fizz.gateway.services.keycloak.AbstractKCService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AbstractUser;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

import java.util.Objects;

public class AuthenticatedUser extends AbstractUser {

    private AbstractKCService kcService;

    private final JsonObject principal = new JsonObject();

    public static void setAppId(final User aUser, final String aAppId) {
        Utils.assertRequiredArgument(aUser, "invalid_user");
        Utils.assertRequiredArgument(aAppId, "invalid_app_id");

        aUser.principal().put("app_id", aAppId);
    }

    public static String appId(final User aUser) {
        return Objects.nonNull(aUser) ? aUser.principal().getString("app_id") : null;
    }

    public static void setUserId(final User aUser, final String aUserId) {
        Utils.assertRequiredArgument(aUser, "invalid_user");
        Utils.assertRequiredArgument(aUserId, "invalid_user_id");

        aUser.principal().put("user_id", aUserId);
    }

    public static String userId(final User aUser) {
        return Objects.nonNull(aUser) ? aUser.principal().getString("user_id") : null;
    }

    public AuthenticatedUser(final String aAppId, final String aUserId, final AbstractKCService kcService) {
        super();
        init(aAppId, aUserId, kcService);
    }

    public AuthenticatedUser(final String aAppId, final String aUserId)  {
        super();
        init(aAppId, aUserId, null);
    }

    private void init(final String aAppId, final String aUserId, final AbstractKCService kcService) {
        setAppId(this, aAppId);
        setUserId(this, aUserId);
        this.kcService = kcService;
    }

    @Override
    protected void doIsPermitted(String s, Handler<AsyncResult<Boolean>> handler) {
        try {
            if (Objects.nonNull(kcService)) {
                handler.handle(Future.succeededFuture(kcService.isPermitted(appId(this), s)));
            } else {
                handler.handle(Future.succeededFuture(true));
            }
        } catch (Exception e) {
            handler.handle(Future.failedFuture(e));
        }
    }

    @Override
    public JsonObject principal() {
        return principal;
    }

    @Override
    public void setAuthProvider(AuthProvider authProvider) {

    }

    @Override
    public User clearCache() {
        return this;
    }
}
