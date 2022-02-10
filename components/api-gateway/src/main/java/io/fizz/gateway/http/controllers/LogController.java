package io.fizz.gateway.http.controllers;

import com.google.gson.JsonArray;
import io.fizz.chat.domain.channel.ChannelMessage;
import io.fizz.chatcommon.infrastructure.WebUtils;
import io.fizz.common.Utils;
import io.fizz.gateway.http.annotations.AsyncRestController;
import io.fizz.gateway.http.annotations.AuthScheme;
import io.fizz.logger.application.service.LoggerService;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class LogController extends AbstractRestController  {
    private final LoggerService loggerService;

    public LogController(Vertx aVertx, final LoggerService aLoggerService) {
        super(aVertx);

        if (Objects.isNull(aLoggerService)) {
            throw new IllegalArgumentException("invalid logger service specified.");
        }

        loggerService = aLoggerService;
    }

    @AsyncRestController(path="/apps/:appId/logs/:logId", method=HttpMethod.POST, auth= AuthScheme.DIGEST)
    public CompletableFuture<Void> onWriteLog(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final String appId = appId(aContext);
            final String logId = logId(aContext);

            final JsonObject params = aContext.getBodyAsJson();
            final String logItem = params.getString("log_item");

            return loggerService.write(appId, logId, logItem)
                    .thenApply(v -> {
                        WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, "{}");
                        return null;
                    });
        });
    }

    @AsyncRestController(path="/apps/:appId/logs/:logId", method= HttpMethod.GET, auth=AuthScheme.DIGEST)
    public CompletableFuture<Void> onFetchLog(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final String appId = appId(aContext);
            final String logId = logId(aContext);
            final Integer count = count(aContext);

            return loggerService.read(appId, logId, count)
                    .thenApply(aMessages -> {

                        JsonArray array = new JsonArray();

                        for (final ChannelMessage message : aMessages) {
                            array.add(message.body());
                        }

                        WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, array.toString());
                        return null;
                    });
        });
    }

    private String appId(final RoutingContext aContext) {
        return aContext.request().getParam("appId");
    }

    private String logId(final RoutingContext aContext) {
        return aContext.request().getParam("logId");
    }

    private Integer count(final RoutingContext aContext) {
        final HttpServerRequest req = aContext.request();
        return Objects.isNull(req.getParam("count")) ? null : Integer.parseInt(req.getParam("count"));
    }
}
