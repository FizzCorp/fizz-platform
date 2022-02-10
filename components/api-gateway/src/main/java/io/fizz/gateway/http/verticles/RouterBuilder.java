package io.fizz.gateway.http.verticles;

import io.fizz.common.Utils;
import io.fizz.gateway.http.annotations.*;
import io.fizz.gateway.http.controllers.AbstractRestController;
import io.fizz.gateway.http.ratelimit.RateLimitConfig;
import io.fizz.gateway.http.ratelimit.RateLimitHandler;
import io.fizz.gateway.http.ratelimit.RateLimitService;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.LoggerHandler;

import java.lang.reflect.Method;
import java.util.*;

public class RouterBuilder {
    private final String apiRootPath;
    private final Vertx vertx;
    private boolean httpLogging = false;
    private boolean enableCORS = false;
    private String originPattern;
    private RateLimitService rateLimitService;
    private final Map<AuthScheme, Handler<RoutingContext>> authHandlers = new HashMap<>();
    private final List<AbstractRestController> controllers = new ArrayList<>();

    public RouterBuilder(final Vertx aVertx, final String aAPIRootPath) {
        Utils.assertRequiredArgument(aVertx, "invalid_vertx_instance");
        Utils.assertRequiredArgument(aAPIRootPath, "invalid_root_path");

        this.apiRootPath = aAPIRootPath;
        this.vertx = aVertx;
    }

    public RouterBuilder enableHttpLogging(boolean aStatus) {
        httpLogging = aStatus;
        return this;
    }

    public RouterBuilder enableCORS(final String aOriginPattern) {
        enableCORS = true;
        originPattern = aOriginPattern;

        return this;
    }

    public RouterBuilder rateLimitService(final RateLimitService aRateLimitService) {
        Utils.assertRequiredArgument(aRateLimitService, "invalid_rate_limit_service");
        rateLimitService = aRateLimitService;

        return this;
    }

    public RouterBuilder authHandler(final AuthScheme aScheme, final Handler<RoutingContext> aHandler) {
        Utils.assertRequiredArgument(aScheme, "invalid_auth_scheme");
        Utils.assertRequiredArgument(aHandler, "invalid_auth_handler");

        authHandlers.put(aScheme, aHandler);

        return this;
    }

    public RouterBuilder controller(final AbstractRestController aController) {
        Utils.assertRequiredArgument(aController, "invalid_controller");

        controllers.add(aController);

        return this;
    }

    public Router build() {
        Router router = Router.router(vertx);

        if (httpLogging) {
            router.route().handler(LoggerHandler.create());
        }

        router.route().handler(BodyHandler.create());

        if (enableCORS) {
            enableCORSSupport(router, originPattern);
        }

        for (AbstractRestController controller: controllers) {
            mount(router, controller);
        }

        return router;
    }

    private void mount(final Router aRouter, final AbstractRestController aController) {
        final Class<? extends AbstractRestController> clazz = aController.getClass();

        for (Method method: clazz.getDeclaredMethods()) {
            RLScope rlScope = RLScope.NONE;
            RLKeyType rlKeyType = RLKeyType.PATH;
            String rlKeyName = "";
            if (method.isAnnotationPresent(RateLimit.class)) {
                final RateLimit rlMeta = method.getAnnotation(RateLimit.class);
                rlScope = rlMeta.scope();
                rlKeyType = rlMeta.type();
                rlKeyName = rlMeta.keyName();
            }
            if (method.isAnnotationPresent(SyncRestController.class)) {
                final SyncRestController meta = method.getAnnotation(SyncRestController.class);
                hookRouteHandler(aRouter, aController, method, meta.path(), meta.method(), meta.auth(),
                        rlScope, rlKeyType, rlKeyName, true);
            }
            else
            if (method.isAnnotationPresent(AsyncRestController.class)) {
                final AsyncRestController meta = method.getAnnotation(AsyncRestController.class);
                hookRouteHandler(aRouter, aController, method, meta.path(), meta.method(), meta.auth(),
                        rlScope, rlKeyType, rlKeyName, false);
            }
        }
    }

    private void hookRouteHandler(final Router aRouter,
                                  final AbstractRestController aController,
                                  final Method handler,
                                  final String path,
                                  final HttpMethod method,
                                  final AuthScheme scheme,
                                  final RLScope aRLScope,
                                  final RLKeyType aRLKeyType,
                                  final String aRLKeyName,
                                  final boolean isSync) {
        final Route route = aRouter.route().method(method).path(apiRootPath + path);
        final Handler<RoutingContext> authHandler = authHandlers.get(scheme);

        if (scheme != AuthScheme.NONE && Objects.isNull(authHandler)) {
            throw new IllegalArgumentException("invalid_auth_handler");
        }

        if (Objects.nonNull(authHandler)) {
            route.handler(authHandler);
        }

        if (aRLScope != RLScope.NONE) {
            RateLimitHandler rlHandler = new RateLimitHandler(aRLScope, aRLKeyType, aRLKeyName, rateLimitService);
            route.handler(rlHandler);
        }

        if (isSync) {
            route.blockingHandler(ctx -> aController.invokeHandler(handler, ctx));
        }
        else {
            route.handler(ctx -> aController.invokeHandler(handler, ctx));
        }
    }

    private void enableCORSSupport(final Router aRouter, final String aOriginPattern) {
        Set<String> allowedHeaders = new HashSet<>();
        allowedHeaders.add("X-Requested-With");
        allowedHeaders.add("Access-Control-Allow-Origin");
        allowedHeaders.add("Origin");
        allowedHeaders.add("Content-Type");
        allowedHeaders.add("Accept");
        allowedHeaders.add("X-PINGARUNER");
        allowedHeaders.add("Authorization");
        allowedHeaders.add("Session-Token");
        allowedHeaders.add("x-fizz-app-id");

        Set<HttpMethod> allowedMethods = new HashSet<>();
        allowedMethods.add(HttpMethod.GET);
        allowedMethods.add(HttpMethod.POST);
        allowedMethods.add(HttpMethod.OPTIONS);
        allowedMethods.add(HttpMethod.DELETE);
        allowedMethods.add(HttpMethod.PATCH);
        allowedMethods.add(HttpMethod.PUT);

        aRouter.route().handler(
                CorsHandler
                        .create(aOriginPattern)
                        .allowedHeaders(allowedHeaders)
                        .allowCredentials(true)
                        .allowedMethods(allowedMethods)
        );
    }
}
