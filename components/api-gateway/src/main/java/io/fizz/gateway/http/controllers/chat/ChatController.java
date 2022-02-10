package io.fizz.gateway.http.controllers.chat;

import com.google.gson.*;
import io.fizz.chat.application.channel.*;
import io.fizz.chat.domain.topic.TopicId;
import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.chatcommon.domain.PublicChannelId;
import io.fizz.common.Utils;
import io.fizz.chat.domain.channel.ChannelMessage;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.common.domain.UnauthorizedException;
import io.fizz.common.domain.UserId;
import io.fizz.gateway.http.annotations.*;
import io.fizz.chatcommon.infrastructure.WebUtils;
import io.fizz.gateway.http.auth.AuthenticatedUser;
import io.fizz.gateway.http.controllers.AbstractRestController;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class ChatController extends AbstractRestController {
    private final ChannelApplicationService channelService;

    public ChatController(final Vertx aVertx, final ChannelApplicationService aChannelService) {
        super(aVertx);

        if (Objects.isNull(aChannelService)) {
            throw new IllegalArgumentException("invalid channel service specified.");
        }

        channelService = aChannelService;
    }

    @AsyncRestController(path="/channels/:channelId/topics", method= HttpMethod.GET, auth=AuthScheme.DIGEST)
    public CompletableFuture<Void> onQueryChannelTopics(final RoutingContext aContext,
                                                        final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final User user = aContext.user();
            final String channelId = aContext.request().getParam("channelId");
            final String requesterId = aContext.request().getParam("requesterId");
            return channelService.fetchTopics(AuthenticatedUser.appId(user), requesterId, channelId)
                    .thenApply(topics -> {
                        final JsonArray resultJson = new JsonArray();
                        for (TopicId topic : topics) {
                            resultJson.add(topic.value());
                        }

                        WebUtils.doOK(aResponse, resultJson);
                        return null;
                    });
        });
    }

    @AsyncRestController(
            path="/channels/:channelId/topics/:topicId/messages",
            method=HttpMethod.POST,
            auth=AuthScheme.DIGEST
    )
    public CompletableFuture<Void> onPublishTopicMessage(final RoutingContext aContext,
                                                         final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final ChannelMessagePublishCommand cmd = buildTopicPublishCommand(aContext);
            final String topicId = aContext.request().getParam("topicId");

            return channelService.publish(cmd, topicId).thenApply(v -> {
                WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, "{}");
                return null;
            });
        });
    }

    @AsyncRestController(
            path="/channels/:channelId/topics/:topicId/messages",
            method=HttpMethod.GET,
            auth=AuthScheme.DIGEST
    )
    public CompletableFuture<Void> onQueryTopicMessages(final RoutingContext aContext,
                                                        final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final String topicId = aContext.request().getParam("topicId");
            final String requesterId = aContext.request().getParam("requesterId");
            final ChannelHistoryQuery query = buildQuery(aContext, requesterId);

            return channelService.queryMessages(query, topicId)
                    .thenApply(this::serialize)
                    .thenApply(aBody -> {
                WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, aBody);
                return null;
            });
        });
    }

    @RateLimit(scope=RLScope.CHANNEL, type=RLKeyType.PATH, keyName="channelId")
    @AsyncRestController(path="/channels/:channelId/messages", method=HttpMethod.POST, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onPublishMessage(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final ChannelMessagePublishCommand cmd = buildChannelPublishCommand(aContext);
            final SessionContext sc = new SessionContext(aContext.session());

            assertApplyChannelFilter(sc, cmd.channelId());
            return channelService.publish(cmd, sc.subscriberId(), sc.locale())
                    .thenApply(v -> {
                        WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, "{}");
                        return null;
                    });
        });
    }

    @AsyncRestController(path="/channels/:channelId/messages", method=HttpMethod.GET, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onQueryMessages(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final SessionContext sc = new SessionContext(aContext.session());
            final ChannelHistoryQuery query = buildQuery(aContext, AuthenticatedUser.userId(aContext.user()));

            assertApplyChannelFilter(sc, query.channelId());

            final CompletableFuture<List<ChannelMessage>> queried =
                                channelService.queryMessages(query, sc.subscriberId(), sc.locale());

            final CompletableFuture<String> serialized = queried.thenApply(this::serialize);

            return serialized.thenApply(aBody -> {
                WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, aBody);
                return null;
            });
        });
    }

    @RateLimit(scope=RLScope.CHANNEL, type=RLKeyType.PATH, keyName="channelId")
    @AsyncRestController(path="/channels/:channelId/messages/:messageId", method=HttpMethod.POST, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onUpdateMessage(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final ChannelMessageUpdateCommand cmd = buildUpdateCommand(aContext);

            final SessionContext sc = new SessionContext(aContext.session());
            assertApplyChannelFilter(sc, cmd.channelId());
            return channelService.update(cmd)
                    .thenApply(v -> {
                        WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, "{}");
                        return null;
                    });
        });
    }

    @RateLimit(scope=RLScope.CHANNEL, type=RLKeyType.PATH, keyName="channelId")
    @AsyncRestController(path="/channels/:channelId/messages/:messageId", method=HttpMethod.DELETE, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onDeleteMessage(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final SessionContext sc = new SessionContext(aContext.session());
            final ChannelId channelId = channelId(sc.appId(), aContext);
            final long messageId = messageId(aContext);

            final ChannelMessageDeleteCommand cmd = new ChannelMessageDeleteCommand(channelId, sc.userId(), messageId, Collections.emptySet());
            assertApplyChannelFilter(sc, channelId);
            return channelService.delete(cmd)
                    .thenApply(v -> {
                        WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, "{}");
                        return null;
                    });
        });
    }

    @AsyncRestController(path="/channels/:channelId/subscribers", method=HttpMethod.POST, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onAddSubscriber(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final SessionContext sc = new SessionContext(aContext.session());
            final ChannelId channelId = channelId(sc.appId(), aContext);
            final ChannelSubscribeCommand cmd = new ChannelSubscribeCommand(
                    channelId, sc.userId(), sc.subscriberId(), sc.locale()
            );

            assertApplyChannelFilter(sc, channelId);
            return channelService.subscribe(cmd)
                    .thenApply(v -> {
                        WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, "{}");
                        return null;
                    });
        });
    }

    @AsyncRestController(path="/channels/:channelId/subscribers", method=HttpMethod.DELETE, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onRemoveSubscriber(final RoutingContext aContext,
                                                      final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final SessionContext sc = new SessionContext(aContext.session());
            final ChannelId channelId = channelId(sc.appId(), aContext);

            final ChannelUnsubscribeCommand cmd = new ChannelUnsubscribeCommand(
                    channelId, sc.userId(), sc.subscriberId(), sc.locale()
            );

            return channelService.unsubscribe(cmd)
                    .thenApply(v -> {
                        WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, "{}");
                        return null;
                    });
        });
    }

    @AsyncRestController(path="/channels/:channelId/mutes", method=HttpMethod.POST, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onMuteUser(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final JsonObject body = aContext.getBodyAsJson();
            final SessionContext sc = new SessionContext(aContext.session());
            final ChannelId channelId = channelId(sc.appId(), aContext);
            final String userToMuteId = body.getString("user_id");
            final Date ends = parseRoleEnd(body);

            return channelService.muteUser(channelId.appId().value(), channelId.value(), sc.userId(), userToMuteId, ends)
                    .thenApply(v -> {
                        WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, "{}");
                        return null;
                    });
        });
    }

    @AsyncRestController(path="/channels/:channelId/mutes/:mutedUserId", method=HttpMethod.DELETE, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onUnmuteUser(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final SessionContext sc = new SessionContext(aContext.session());
            final ChannelId channelId = channelId(sc.appId(), aContext);
            final String mutedUserId = routeParam(aContext, "mutedUserId");

            return unmuteUser(aResponse, channelId, sc.userId(), mutedUserId);
        });
    }

    @AsyncRestController(path="/channels/:channelId/mutes", method=HttpMethod.DELETE, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onUnmuteUserWithBody(final RoutingContext aContext,
                                                        final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final SessionContext sc = new SessionContext(aContext.session());
            final JsonObject body = aContext.getBodyAsJson();
            final ChannelId channelId = channelId(sc.appId(), aContext);
            final String mutedUserId = body.getString("user_id");

            return unmuteUser(aResponse, channelId, sc.userId(), mutedUserId);
        });
    }

    @AsyncRestController(path="/channels/:channelId/bans", method=HttpMethod.POST, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onAddUserBan(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final JsonObject body = aContext.getBodyAsJson();
            final SessionContext sc = new SessionContext(aContext.session());
            final ChannelId channelId = channelId(sc.appId(), aContext);
            final String userToMuteId = body.getString("user_id");
            final Date ends = parseRoleEnd(body);

            return channelService.addUserBan(channelId.appId().value(), channelId.value(), sc.userId(), userToMuteId, ends)
                    .thenApply(v -> {
                        WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, "{}");
                        return null;
                    });
        });
    }

    @AsyncRestController(path="/channels/:channelId/bans/:bannedUserId", method=HttpMethod.DELETE, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onRemoveUserBan(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final SessionContext sc = new SessionContext(aContext.session());
            final ChannelId channelId = channelId(sc.appId(), aContext);
            final String bannedUserId = routeParam(aContext, "bannedUserId");

            return removeUserBan(aResponse, channelId, sc.userId(), bannedUserId);
        });
    }

    @AsyncRestController(path="/channels/:channelId/bans", method=HttpMethod.DELETE, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onRemoveUserBanWithBody(final RoutingContext aContext,
                                                           final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final SessionContext sc = new SessionContext(aContext.session());
            final JsonObject body = aContext.getBodyAsJson();
            final ChannelId channelId = channelId(sc.appId(), aContext);
            final String bannedUserId = body.getString("user_id");

            return removeUserBan(aResponse, channelId, sc.userId(), bannedUserId);
        });
    }

    private CompletableFuture<Void> unmuteUser(final HttpServerResponse aResponse,
                                               final ChannelId aChannelId,
                                               final String aModeratorId,
                                               final String aMutedUserId ) {
        return channelService.unmuteUser(aChannelId.appId().value(), aChannelId.value(), aModeratorId, aMutedUserId)
                .thenApply(v -> {
                    WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, "{}");
                    return null;
                });
    }

    private CompletableFuture<Void> removeUserBan(final HttpServerResponse aResponse,
                                                  final ChannelId aChannelId,
                                                  final String aModeratorId,
                                                  final String aBannedUserId) {
        return channelService.removeUserBan(aChannelId.appId().value(), aChannelId.value(), aModeratorId, aBannedUserId)
                .thenApply(v -> {
                    WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, "{}");
                    return null;
                });
    }

    private ChannelMessagePublishCommand buildTopicPublishCommand(final RoutingContext aContext) {
        ChannelMessagePublishCommand.Builder messagePublishBuilder = messagePublishCommandBuilder(aContext);
        final JsonObject params = aContext.getBodyAsJson();
        final String from = params.getString("from");
        Set<UserId> notifyList = new HashSet<>();
        if (params.containsKey("notify")) {
            final JsonArray users = params.getJsonArray("notify");
            if (Objects.nonNull(users)) {
                for (int i = 0; i < users.size(); i++) {
                    notifyList.add(new UserId(users.getString(i)));
                }
            }
        }

        return messagePublishBuilder
                .setAuthorId(from)
                .setNotifyList(notifyList)
                .build();
    }

    private ChannelMessagePublishCommand buildChannelPublishCommand(final RoutingContext aContext) {
        return messagePublishCommandBuilder(aContext)
                .setAuthorId(AuthenticatedUser.userId(aContext.user()))
                .build();
    }

    private ChannelMessagePublishCommand.Builder messagePublishCommandBuilder(final RoutingContext aContext) {
        final JsonObject params = aContext.getBodyAsJson();
        final ChannelId channelId = channelId(AuthenticatedUser.appId(aContext.user()), aContext);
        final String nick = params.getString("nick");
        final String body = params.getString("body");
        final String data = params.getString("data");
        final String locale = params.getString("locale");
        final boolean translate = params.containsKey("translate") ? params.getBoolean("translate") : false;
        final boolean filter = params.containsKey("filter") ? params.getBoolean("filter") : true;
        final boolean persist = params.containsKey("persist") ? params.getBoolean("persist") : true;

        return new ChannelMessagePublishCommand.Builder()
                .setChannelId(channelId)
                .setNick(nick)
                .setBody(body)
                .setData(data)
                .setLocale(locale)
                .setTranslate(translate)
                .setFilter(filter)
                .setPersist(persist);
    }

    private ChannelMessageUpdateCommand buildUpdateCommand(final RoutingContext aContext) {
        final JsonObject params = aContext.getBodyAsJson();
        final SessionContext sc = new SessionContext(aContext.session());
        final ChannelId channelId = channelId(sc.appId(), aContext);
        final long messageId = messageId(aContext);
        final String nick = params.getString("nick");
        final String body = params.getString("body");
        final String data = params.getString("data");
        final String locale = params.getString("locale");
        final boolean translate = params.containsKey("translate") ? params.getBoolean("translate") : false;
        final boolean filter = params.containsKey("filter") ? params.getBoolean("filter") : true;

        return new ChannelMessageUpdateCommand.Builder()
                .setChannelId(channelId)
                .setAuthorId(sc.userId())
                .setMessageId(messageId)
                .setNick(nick)
                .setBody(body)
                .setData(data)
                .setLocale(locale)
                .setTranslate(translate)
                .setFilter(filter)
                .setNotifyList(Collections.emptySet())
                .build();
    }

    private String serialize(final List<ChannelMessage> aMessages) {
        final Gson serde = new GsonBuilder()
                .registerTypeAdapter(ChannelMessage.class, new MessageSerializer())
                .create();
        final StringBuilder builder = new StringBuilder();

        builder.append("[");
        for (int mi = 0; mi < aMessages.size(); mi++) {
            if (mi > 0) {
                builder.append(",");
            }
            final ChannelMessage message = aMessages.get(mi);
            builder.append(serde.toJson(message));
        }

        builder.append("]");

        return builder.toString();
    }

    private ChannelHistoryQuery buildQuery(final RoutingContext aContext, final String requesterId) {
        final HttpServerRequest req = aContext.request();
        final ChannelId channelId = channelId(AuthenticatedUser.appId(aContext.user()), aContext);
        final int messageCount = Integer.parseInt(req.getParam("count"));
        final Long beforeId =
                Objects.isNull(req.getParam("before_id")) ? null : Long.parseLong(req.getParam("before_id"));
        final Long afterId =
                Objects.isNull(req.getParam("after_id")) ? null : Long.parseLong(req.getParam("after_id"));

        return new ChannelHistoryQuery(
                channelId, requesterId, messageCount, beforeId, afterId
        );
    }

    private ChannelId channelId(final String aAppId, final RoutingContext aContext) {
        try {
            final ApplicationId appId = new ApplicationId(aAppId);
            return new PublicChannelId(appId, aContext.request().getParam("channelId"));
        }
        catch (DomainErrorException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    private String topicId(final RoutingContext aContext) {
        return aContext.request().getParam("topicId");
    }

    private long messageId(final RoutingContext aContext) {
        return Long.parseLong(aContext.request().getParam("messageId"));
    }

    private String routeParam(final RoutingContext aContext, final String aParam) {
        return aContext.request().getParam(aParam);
    }

    private Date parseRoleEnd(final JsonObject aBody) {
        if (aBody.containsKey("duration")) {
            return new Date(Utils.now() + aBody.getLong("duration")*1000L);
        }
        else {
            return Utils.TIME_END;
        }
    }

    private void assertApplyChannelFilter(final SessionContext aContext, final ChannelId aChannelId) {
        if (Objects.isNull(aContext.channelFilter())) {
            return;
        }
        
        if (Objects.isNull(aChannelId) || !aChannelId.value().matches(aContext.channelFilter())) {
            throw new UnauthorizedException("restricted channel");
        }
    }
}
