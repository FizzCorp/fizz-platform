package io.fizz.gateway.http.controllers;

import com.newrelic.api.agent.NewRelic;
import io.fizz.chatcommon.infrastructure.WebUtils;
import io.fizz.common.LoggingService;
import io.fizz.common.Utils;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.common.domain.UnauthorizedException;
import io.fizz.gateway.http.annotations.AsyncRestController;
import io.fizz.gateway.http.annotations.SyncRestController;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.DecodeException;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractRestController {
    protected static final LoggingService.Log logger = LoggingService.getLogger(AbstractRestController.class);

    private final Router router;
    private boolean routesHooked = false;

    public AbstractRestController(final Vertx aVertx) {
        if (Objects.isNull(aVertx)) {
            throw new IllegalArgumentException("Invalid vertx instance specified.");
        }
        router = Router.router(aVertx);
    }

    public Router router() {
        if (!routesHooked) {
            routesHooked = true;
            hookRoutes();
        }

        return router;
    }

    public AbstractRestController handler(final Handler<RoutingContext> aHandler) {
        if (Objects.isNull(aHandler)) {
            throw new IllegalArgumentException("invalid handler specified.");
        }
        router.route().handler(aHandler);
        return this;
    }

    private void hookRoutes() {
        final Class<? extends AbstractRestController> clazz = getClass();

        for(Method method: clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(SyncRestController.class)) {
                final Annotation annotation = method.getAnnotation(SyncRestController.class);
                final SyncRestController controller = (SyncRestController)annotation;
                hookRouteHandler(method, controller.path(), controller.method(), true);
            }
            else
            if (method.isAnnotationPresent(AsyncRestController.class)) {
                final Annotation annotation = method.getAnnotation(AsyncRestController.class);
                final AsyncRestController controller = (AsyncRestController)annotation;
                hookRouteHandler(method, controller.path(), controller.method(), false);
            }
        }
    }

    private void handleError(final Throwable ex, final RoutingContext aContext) {
        if (Objects.isNull(ex.getCause())) {
            logger.error("PATH:" + aContext.request().path() + "\nError: " + ex.getMessage());
            NewRelic.noticeError(ex);
            WebUtils.doError(aContext.response(), WebUtils.STATUS_INTERNAL_SERVER_ERROR, null);
            return;
        }

        final HttpServerResponse response = aContext.response();
        final Throwable cause = ex.getCause();

        if (cause instanceof DomainErrorException) {
            WebUtils.doErrorWithReason(
                    response,
                    WebUtils.STATUS_BAD_REQUEST,
                    ((DomainErrorException)cause).error().reason(),
                    null
            );
        }
        else
        if (cause instanceof UnauthorizedException) {
            WebUtils.doErrorWithReason(
                    response,
                    WebUtils.STATUS_UNAUTHORIZED,
                    ((UnauthorizedException)cause).error().reason(),
                    null
            );
        }
        else
        if (cause instanceof DecodeException) {
            WebUtils.doErrorWithReason(
                    response,
                    WebUtils.STATUS_BAD_REQUEST,
                    "invalid_request_body",
                    null
            );
        }
        else
        if (cause instanceof NullPointerException) {
            logger.error(ex.getMessage());
            NewRelic.noticeError(ex);
            logger.error("PATH:" + aContext.request().path() + "\nError: " + cause.getMessage());

            WebUtils.doErrorWithReason(
                    response,
                    WebUtils.STATUS_BAD_REQUEST,
                    "invalid_request_body",
                    null
            );
        }
        else
        if (cause instanceof ClassCastException) {
            WebUtils.doErrorWithReason(
                    response,
                    WebUtils.STATUS_BAD_REQUEST,
                    "invalid_request_body",
                    null
            );
        }
        else
        if (cause instanceof NumberFormatException) {
            WebUtils.doErrorWithReason(
                    response,
                    WebUtils.STATUS_BAD_REQUEST,
                    "invalid_request_body",
                    null
            );
        }
        else
        if (cause instanceof IllegalArgumentException) {
            WebUtils.doErrorWithReason(
                    response,
                    WebUtils.STATUS_BAD_REQUEST,
                    cause.getMessage(),
                    null
            );
        }
        else
        if (cause instanceof IllegalStateException) {
            WebUtils.doErrorWithReason(
                    response,
                    WebUtils.STATUS_CONFLICT,
                    cause.getMessage(),
                    null
            );
        }
        else
        if (cause instanceof SecurityException) {
            WebUtils.doErrorWithReason(
                    response,
                    WebUtils.STATUS_FORBIDDEN,
                    cause.getMessage(),
                    null
            );
        }
        else {
            logger.error(ex.getMessage());
            NewRelic.noticeError(ex);
            logger.error("PATH:" + aContext.request().path() + "\nError: " + cause.getMessage());

            WebUtils.doError(
                    aContext.response(),
                    WebUtils.STATUS_INTERNAL_SERVER_ERROR,
                    null
            );
        }
    }

    public void invokeHandler(final Method aHandler, final RoutingContext aContext) {
        Utils.async(() -> {
            try {
                final CompletableFuture<Void> handlerType = new CompletableFuture<>();

                if (aHandler.getReturnType().equals(handlerType.getClass())) {
                    return (CompletableFuture<Void>) aHandler.invoke(this, aContext, aContext.response());
                }
                else {
                    aHandler.invoke(this, aContext, aContext.response());
                    return CompletableFuture.completedFuture(null);
                }
            }
            catch (InvocationTargetException ex) {
                return Utils.failedFuture(ex.getCause());
            }
            catch (Exception ex) {
                return Utils.failedFuture(ex);
            }
        })
        .whenComplete((v, aError) -> {
            if (Objects.nonNull(aError)) {
                handleError(aError, aContext);
            }
        });
    }

    private void hookRouteHandler(final Method handler,
                                  final String path,
                                  final HttpMethod method,
                                  final boolean isSync) {
        final Route route = router.route().method(method).path(path);

        if (isSync) {
            route.blockingHandler(ctx -> invokeHandler(handler, ctx));
        }
        else {
            route.handler(ctx -> invokeHandler(handler, ctx));
        }
    }
}
