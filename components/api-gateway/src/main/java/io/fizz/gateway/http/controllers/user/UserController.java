package io.fizz.gateway.http.controllers.user;

import io.fizz.chat.pubsub.domain.subscriber.SubscriberId;
import io.fizz.chat.user.application.UserDTO;
import io.fizz.chat.user.domain.PushPlatform;
import io.fizz.chatcommon.infrastructure.WebUtils;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.Url;
import io.fizz.common.domain.UserId;
import io.fizz.gateway.http.annotations.AsyncRestController;
import io.fizz.gateway.http.annotations.AuthScheme;
import io.fizz.gateway.http.controllers.AbstractRestController;
import io.fizz.gateway.http.controllers.chat.SessionContext;
import io.fizz.chat.user.application.UpdateUserCommand;
import io.fizz.chat.user.application.service.UserService;
import io.fizz.chat.user.domain.Nick;
import io.fizz.chat.user.domain.StatusMessage;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class UserController extends AbstractRestController {
    private final UserService userService;

    public UserController(Vertx aVertx, final UserService aUserService) {
        super(aVertx);

        if (Objects.isNull(aUserService)) {
            throw new IllegalArgumentException("invalid user service specified.");
        }

        userService = aUserService;
    }

    @AsyncRestController(path="/users/:userId", method=HttpMethod.GET, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onQueryUser(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final SessionContext sc = new SessionContext(aContext.session());
            final ApplicationId appId = appId(sc);
            final UserId userId = userId(aContext);

            return userService.fetchUser(appId, userId)
                    .thenApply(userDTO -> {
                        WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, serialize(userDTO));
                        return null;
                    });
        });
    }

    @AsyncRestController(path="/users/:userId", method=HttpMethod.POST, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onUpdateUser(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final SessionContext sc = new SessionContext(aContext.session());
            final ApplicationId appId = appId(sc);

            String pathUserId = aContext.pathParam("userId");
            if (Objects.isNull(pathUserId) || !pathUserId.equals(sc.userId())) {
                throw new SecurityException("can't update other user");
            }
            final UserId userId = new UserId(sc.userId());
            final JsonObject body = aContext.getBodyAsJson();
            final Nick nick = nick(body);
            final StatusMessage statusMessage = statusMessage(body);
            final Url profileUrl = profileUrl(body);

            UpdateUserCommand updateCommand = new UpdateUserCommand(appId, userId, nick, statusMessage, profileUrl);
            return userService.updateUser(updateCommand)
                    .thenApply(v -> {
                        WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, "{}");
                        return null;
                    });
        });
    }

    @AsyncRestController(path="/users/:userId/subscribers", method=HttpMethod.POST, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onSubscribeUser(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final SessionContext sc = new SessionContext(aContext.session());
            final ApplicationId appId = appId(sc);
            final UserId userId = userId(aContext);
            final SubscriberId subscriberId = new SubscriberId(sc.subscriberId());

            return userService.subscribeUser(appId, userId, subscriberId)
                    .thenApply(v -> {
                        WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, "{}");
                        return null;
                    });
        });
    }

    @AsyncRestController(path="/users/:userId/subscribers", method=HttpMethod.DELETE, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onUnsubscribeUser(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final SessionContext sc = new SessionContext(aContext.session());
            final ApplicationId appId = appId(sc);
            final UserId userId = userId(aContext);
            final SubscriberId subscriberId = new SubscriberId(sc.subscriberId());

            return userService.unsubscribeUser(appId, userId, subscriberId)
                    .thenApply(v -> {
                        WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, "{}");
                        return null;
                    });
        });
    }

    @AsyncRestController(path="/users/:userId/devices", method=HttpMethod.POST, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onSetDevice(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final JsonObject body = aContext.getBodyAsJson();
            final SessionContext sc = new SessionContext(aContext.session());
            final ApplicationId appId = appId(sc);
            final UserId userId = new UserId(sc.userId());
            final PushPlatform platform = PushPlatform.fromValue(body.getString("platform"));
            final String token = body.getString("token");

            Utils.assertArgumentsEquals(userId, userId(aContext), "invalid_user");
            Utils.assertRequiredArgument(platform, "invalid_platform");

            return userService.setDeviceToken(appId, userId, token)
                    .thenApply(aSaved -> {
                        if (!aSaved) {
                            throw new IllegalStateException("conflicted_state");
                        }
                        return null;
                    })
                    .thenApply(v -> {
                        WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, "{}");
                        return null;
                    });
        });
    }

    @AsyncRestController(path="/users/:userId/devices/:platform", method=HttpMethod.DELETE, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onClearDevice(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final SessionContext sc = new SessionContext(aContext.session());
            final ApplicationId appId = appId(sc);
            final UserId userId = new UserId(sc.userId());
            final PushPlatform platform = PushPlatform.fromValue(aContext.request().getParam("platform"));

            Utils.assertArgumentsEquals(userId, userId(aContext), "invalid_user");
            Utils.assertRequiredArgument(platform, "invalid_platform");

            return userService.clearDeviceToken(appId, userId)
                    .thenApply(aSaved -> {
                        if (!aSaved) {
                            throw new IllegalStateException("conflicted_state");
                        }
                        return null;
                    })
                    .thenApply(v -> {
                        WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, "{}");
                        return null;
                    });
        });
    }

    private UserId userId(final RoutingContext aContext) {
        return new UserId(aContext.request().getParam("userId"));
    }

    private ApplicationId appId(SessionContext aSessionContext) {
        try {
            return new ApplicationId(aSessionContext.appId());
        }
        catch (Exception ex ){
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    private Nick nick(final JsonObject body) {
        String value = body.getString("nick");
        return Objects.isNull(value) ? null : new Nick(value);
    }

    private StatusMessage statusMessage(final JsonObject body) {
        String value = body.getString("status_message");
        return Objects.isNull(value) ? null : new StatusMessage(value);
    }

    private Url profileUrl(final JsonObject body) {
        String value = body.getString("profile_url");
        return Objects.isNull(value) ? null : new Url(value);
    }

    private String serialize(final UserDTO aUser) {
        final JsonObject json = new JsonObject();

        json.put("nick", Objects.isNull(aUser.nick()) ? "" : aUser.nick().value());
        json.put("status_message", Objects.isNull(aUser.statusMessage()) ? "" : aUser.statusMessage().value());
        json.put("profile_url", Objects.isNull(aUser.profileUrl()) ? "" : aUser.profileUrl().value());
        json.put("is_online", aUser.isOnline());

        if (Objects.nonNull(aUser.token())) {
            final JsonObject tokens = new JsonObject();
            tokens.put(PushPlatform.FCM.value(), aUser.token());
            json.put("tokens", tokens);
        }

        return json.toString();
    }
}
