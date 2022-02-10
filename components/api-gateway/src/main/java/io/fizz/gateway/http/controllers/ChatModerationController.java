package io.fizz.gateway.http.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.fizz.chat.moderation.application.serde.ReportedChannelSerializer;
import io.fizz.chat.moderation.application.serde.ReportedMessagesSerializer;
import io.fizz.chat.moderation.application.serde.ReportedUserSerializer;
import io.fizz.chat.moderation.application.service.ChatModerationService;
import io.fizz.chat.moderation.domain.ReportedChannel;
import io.fizz.chat.moderation.domain.ReportedMessageSearchResult;
import io.fizz.chat.moderation.domain.ReportedUser;
import io.fizz.chatcommon.infrastructure.WebUtils;
import io.fizz.common.Utils;
import io.fizz.gateway.http.annotations.AsyncRestController;
import io.fizz.gateway.http.annotations.AuthScheme;
import io.fizz.session.SessionUtils;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ChatModerationController extends AbstractRestController  {
    private final ChatModerationService chatModerationService;
    public ChatModerationController(final Vertx aVertx, final ChatModerationService aChatModerationService) {
        super(aVertx);

        if (Objects.isNull(aChatModerationService)) {
            throw new IllegalArgumentException("invalid reporting service specified.");
        }
        chatModerationService = aChatModerationService;
    }

    @AsyncRestController(path="/reports", method= HttpMethod.POST, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onReportMessage(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final JsonObject body = aContext.getBodyAsJson();
            final String appId = SessionUtils.getAppId(aContext.session());
            final String reporterUserId = SessionUtils.getUserId(aContext.session());
            final String channelId = body.getString("channel_id");
            final String message = body.getString("message");
            final String messageId = body.getString("message_id");
            final String language = body.getString("language");
            final String reportedUserId = body.getString("user_id");
            final String offense = body.getString("offense");
            final String description = body.getString("description");
            final Long time = Utils.now()/1000;

            return chatModerationService.reportMessage(appId, reporterUserId, reportedUserId, channelId, message, messageId, language, offense, description, time)
                    .thenApply(id -> {
                        WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, new JsonObject().put("id", id).toString());
                        return null;
                    });
        });
    }

    @AsyncRestController(path="/queries/reports", method= HttpMethod.POST, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onSearchReportedMessages(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final JsonObject body = aContext.getBodyAsJson();
            final String appId = SessionUtils.getAppId(aContext.session());
            final String channelId = body.getString("channel_id");
            final String language = body.getString("language");
            final String userId = body.getString("user_id");
            Integer cursor = body.getInteger("cursor", 0);
            final Integer pageSize = body.getInteger("page_size");
            final String sort = body.getString("sort", "desc");
            final Long start = body.getLong("start");
            final Long end = body.getLong("end");

            return chatModerationService.searchMessages(appId, userId, channelId, language, cursor, pageSize, sort, start, end)
                    .thenApply(this::serializeReportedMessageSearchResult)
                    .thenApply(results -> {
                        WebUtils.doOK(aResponse, results);
                        return null;
                    });
        });
    }

    @AsyncRestController(path="/queries/reportedUsers", method= HttpMethod.POST, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onSearchReportedMessageUsers(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final JsonObject body = aContext.getBodyAsJson();
            final String appId = SessionUtils.getAppId(aContext.session());
            final String channelId = body.getString("channel_id");
            final String language = body.getString("language");
            final Integer resultLimit = body.getInteger("limit");
            final Long start = body.getLong("start");
            final Long end = body.getLong("end");

            return chatModerationService.searchUsers(appId, channelId, language, resultLimit, start, end)
                    .thenApply(this::serializeReportedUsers)
                    .thenApply(results -> {
                        WebUtils.doOK(aResponse, results);
                        return null;
                    });
        });
    }

    @AsyncRestController(path="/queries/reportedChannels", method= HttpMethod.POST, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onSearchReportedMessageChannels(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final JsonObject body = aContext.getBodyAsJson();
            final String appId = SessionUtils.getAppId(aContext.session());
            final String language = body.getString("language");
            final Integer resultLimit = body.getInteger("limit");
            final Long start = body.getLong("start");
            final Long end = body.getLong("end");

            return chatModerationService.searchChannels(appId, language, resultLimit, start, end)
                    .thenApply(this::serializeReportedChannels)
                    .thenApply(results -> {
                        WebUtils.doOK(aResponse, results);
                        return null;
                    });
        });
    }

    private JsonObject serializeReportedMessageSearchResult(final ReportedMessageSearchResult messages) {
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(ReportedMessageSearchResult.class, new ReportedMessagesSerializer())
                .create();
        return new JsonObject(gson.toJson(messages));
    }

    private JsonArray serializeReportedUsers(final List<ReportedUser> reportedUsers) {
        final JsonArray array = new JsonArray();
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(ReportedUser.class, new ReportedUserSerializer())
                .create();
        for (ReportedUser reportedUser: reportedUsers) {
            JsonObject json = new JsonObject(gson.toJson(reportedUser));
            array.add(json);
        }
        return array;
    }

    private JsonArray serializeReportedChannels(final List<ReportedChannel> reportedChannels) {
        final JsonArray array = new JsonArray();
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(ReportedChannel.class, new ReportedChannelSerializer())
                .create();
        for (ReportedChannel reportedChannel: reportedChannels) {
            JsonObject json = new JsonObject(gson.toJson(reportedChannel));
            array.add(json);
        }
        return array;
    }
}
