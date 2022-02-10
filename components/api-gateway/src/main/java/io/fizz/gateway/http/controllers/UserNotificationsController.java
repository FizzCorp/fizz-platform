package io.fizz.gateway.http.controllers;

import io.fizz.chat.application.UserNotificationApplicationService;
import io.fizz.chat.pubsub.domain.subscriber.SubscriberId;
import io.fizz.chatcommon.infrastructure.WebUtils;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;
import io.fizz.gateway.http.annotations.AsyncRestController;
import io.fizz.gateway.http.annotations.AuthScheme;
import io.fizz.gateway.http.controllers.chat.SessionContext;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.concurrent.CompletableFuture;

public class UserNotificationsController extends AbstractRestController {
    private final UserNotificationApplicationService service;

    public UserNotificationsController(final Vertx aVertx, final UserNotificationApplicationService aService) {
        super(aVertx);

        Utils.assertRequiredArgument(aService, "invalid notification service");

        this.service = aService;
    }

    @AsyncRestController(path="/notifications", method= HttpMethod.POST, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onAddSubscriber(final RoutingContext aContext,
                                                   final HttpServerResponse aResponse) {
        final SessionContext sc = new SessionContext(aContext.session());
        final ApplicationId appId = appId(sc.appId());
        final UserId userId = new UserId(sc.userId());
        final SubscriberId subscriberId = new SubscriberId(sc.subscriberId());

        return service.subscribe(appId, userId, subscriberId)
                .thenApply(aVoid -> {
                    WebUtils.doOK(aResponse, new JsonObject());
                    return null;
                });
    }

    @AsyncRestController(path="/notifications", method= HttpMethod.DELETE, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onRemoveSubscriber(final RoutingContext aContext,
                                                      final HttpServerResponse aResponse) {
        final SessionContext sc = new SessionContext(aContext.session());
        final ApplicationId appId = appId(sc.appId());
        final UserId userId = new UserId(sc.userId());
        final SubscriberId subscriberId = new SubscriberId(sc.subscriberId());

        return service.unsubscribe(appId, userId, subscriberId)
                .thenApply(aVoid -> {
                    WebUtils.doOK(aResponse, new JsonObject());
                    return null;
                });
    }

    /*/////////////////////////////////////
    //////// Test Purpose Only //////////
    ////////////////////////////////////
    @AsyncRestController(path="/notifications/publish", method= HttpMethod.POST)
    public CompletableFuture<Void> onPublish(final RoutingContext aContext,
                                             final HttpServerResponse aResponse) {
        final SessionContext sc = new SessionContext(aContext.session());
        final ApplicationId appId = appId(sc.appId());
        final UserId userId = new UserId(sc.userId());
        final JsonObject data = new JsonObject() {{ put("name", "ABC"); }};

        final TopicMessage message = new TopicMessage(
                123L, "USRNO", userId.value(), data.toString(), new Date()
        );
        return service.publish(appId, Collections.singletonList(userId), message)
                .thenApply(aVoid -> {
                    WebUtils.doOK(aResponse, "{}");
                    return null;
                });
    }*/

    private ApplicationId appId(final String aAppId) {
        try {
            return new ApplicationId(aAppId);
        }
        catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }
}
