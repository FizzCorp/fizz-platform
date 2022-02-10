package io.fizz.gateway.http.auth;

import com.google.gson.Gson;
import io.fizz.common.Utils;
import io.fizz.common.domain.DomainErrorException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.AuthHandlerImpl;
import io.vertx.ext.web.handler.impl.HttpStatusException;

import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class SignatureAuthHandler extends AuthHandlerImpl {
    static class ErrorBody {
        private String reason;
        ErrorBody (String aReason) {
            reason = aReason;
        }
    }

    static class AuthError extends Error {
        HttpResponseStatus status;
        String reason;
        AuthError (HttpResponseStatus aStatus, String aReason) {
            status = aStatus;
            reason = aReason;
        }
    }

    private static final String KEY_CONTENT_TYPE = "content-type";
    private static final String CONTENT_JSON = "application/json";
    private static final String KEY_APP_ID = "x-fizz-app-id";

    private static final String AUTH_TYPE = "HMAC-SHA256";

    public SignatureAuthHandler(AuthProvider authProvider) {
        super(authProvider);
    }

    @Override
    public void parseCredentials(RoutingContext aContext, Handler<AsyncResult<JsonObject>> aHandler) {
        try {
            JsonObject credentials;
            HttpServerRequest request = aContext.request();
            MultiMap headers = request.headers();

            if (headers.contains(KEY_APP_ID)) {
                credentials = parseCredentialsV2(aContext);
            }
            else {
                credentials = parseCredentialsV1(aContext);
            }

            aHandler.handle(Future.succeededFuture(credentials));
        } catch (Exception e) {
            aHandler.handle(Future.failedFuture(e));
        }
    }

    private JsonObject parseCredentialsV1(final RoutingContext aContext) {
        final String appId = parseAppId(aContext);
        if (Objects.isNull(appId)) {
            throw new HttpStatusException(HttpResponseStatus.UNAUTHORIZED.code(), "invalid_app_id");
        }

        if (isGetMethod(aContext)) {
            String nonce = nonce(aContext);
            if (Objects.isNull(nonce)) {
                throw new HttpStatusException(HttpResponseStatus.BAD_REQUEST.code(), "invalid_nonce");
            }
        }

        final String authorization = aContext.request().headers().get(HttpHeaders.AUTHORIZATION);

        return new JsonObject()
                .put("payload", payload(aContext))
                .put("signature", signature(authorization))
                .put("appId", appId);
    }

    private JsonObject parseCredentialsV2(final RoutingContext aContext) {
        final HttpServerRequest request = aContext.request();
        final String appId = request.headers().get(KEY_APP_ID);
        final String method = request.method().toString().toUpperCase();
        final String path = aContext.normalisedPath();
        final String bodyDigest = bodyDigest(aContext.getBodyAsString());
        final String payload = method + '\n' + path + '\n' + bodyDigest;
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        return new JsonObject()
                .put("payload", payload)
                .put("signature", signature(authHeader))
                .put("appId", appId);
    }

    private String bodyDigest(final String aBody) {
        try {
            return Utils.computeSHA256(aBody);
        }
        catch (NoSuchAlgorithmException ex) {
            throw new HttpStatusException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), "invalid_hash_algo");
        }
    }

    private String signature(final String aHeader) {
        if (Objects.isNull(aHeader)) {
            throw new HttpStatusException(HttpResponseStatus.UNAUTHORIZED.code(), "auth_header_not_found");
        }

        int idx = aHeader.indexOf(' ');
        if (idx <= 0) {
            // auth header scheme is incorrect
            throw new HttpStatusException(HttpResponseStatus.BAD_REQUEST.code(), "incorrect_auth_header");
        }

        if (!AUTH_TYPE.equals(aHeader.substring(0, idx))) {
            // auth type not correct
            throw new HttpStatusException(HttpResponseStatus.UNAUTHORIZED.code(), "incorrect_auth_header");
        }

        return aHeader.substring(idx + 1);
    }

    @Override
    protected void processException(RoutingContext ctx, Throwable exception) {
        if (exception instanceof AuthError) {
            String reason = ((AuthError) exception).reason;
            HttpResponseStatus code = ((AuthError) exception).status;
            String header = this.authenticateHeader(ctx);
            if (header != null) {
                ctx.response().putHeader("WWW-Authenticate", header);
            }
            doError(ctx.response(), code.code(), reason);
        } else if (exception instanceof HttpStatusException) {
            String reason = reason((HttpStatusException) exception);
            int code = ((HttpStatusException) exception).getStatusCode();
            String header = this.authenticateHeader(ctx);
            if (header != null) {
                ctx.response().putHeader("WWW-Authenticate", header);
            }
            doError(ctx.response(), code, Objects.isNull(reason) ? exception.getMessage() : reason);
        } else if (exception instanceof DomainErrorException) {
            String reason = ((DomainErrorException) exception).error().reason();
            String header = this.authenticateHeader(ctx);
            if (header != null) {
                ctx.response().putHeader("WWW-Authenticate", header);
            }
            doError(ctx.response(), HttpResponseStatus.UNAUTHORIZED.code(), reason);
        } else {
            doError(ctx.response(), HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), exception.getMessage());
        }
    }

    private String reason(final HttpStatusException aException) {
        if (Objects.nonNull(aException.getCause())) {
            return aException.getCause().getMessage();
        }
        else {
            return aException.getPayload();
        }
    }

    @Override
    protected String authenticateHeader(RoutingContext ctx) {
        return AUTH_TYPE;
    }

    private void doError(final HttpServerResponse aResponse, final int aCode, final String aReason) {
        aResponse.putHeader(KEY_CONTENT_TYPE, CONTENT_JSON);
        aResponse.setStatusCode(aCode);
        if (aReason != null) {
            ErrorBody body = new ErrorBody(aReason);
            aResponse.end(new Gson().toJson(body));
        }
        else {
            aResponse.end();
        }
    }

    private String parseAppIdFromBody(RoutingContext aContext) {
        try {
            final JsonObject body = aContext.getBodyAsJson();
            return body.getString("app_id");
        }
        catch (Exception ex) {
            return null;
        }
    }

    private String parseAppId(RoutingContext aContext) {
        String appId = parseAppIdFromBody(aContext);
        if (Objects.isNull(appId)) {
            final String path = aContext.request().path();
            int tokenIdx = path.indexOf("apps/");
            if (tokenIdx < 0) {
                return null;
            }

            int startIn =  tokenIdx + 5;
            int endIn = path.indexOf('/', startIn);
            appId = path.substring(startIn, endIn);
        }

        return appId;
    }

    private boolean isGetMethod(RoutingContext aContext) {
        return aContext.request().method() == HttpMethod.GET;
    }

    private String nonce(RoutingContext aContext) {
        return aContext.request().getParam("nonce");
    }

    private String payload(RoutingContext aContext) {
        if (isGetMethod(aContext)) {
            JsonObject payload = new JsonObject();
            payload.put("nonce", nonce(aContext));
            return payload.toString();
        }
        return aContext.getBodyAsString();
    }
}