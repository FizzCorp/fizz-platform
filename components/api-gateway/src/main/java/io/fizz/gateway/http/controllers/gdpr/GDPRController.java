package io.fizz.gateway.http.controllers.gdpr;

import io.fizz.chatcommon.infrastructure.WebUtils;
import io.fizz.common.Utils;
import io.fizz.common.domain.QueryRange;
import io.fizz.gateway.http.annotations.*;
import io.fizz.gateway.http.auth.AuthenticatedUser;
import io.fizz.gateway.http.controllers.AbstractRestController;
import io.fizz.gdpr.application.service.GDPRService;
import io.fizz.gdpr.domain.GDPRRequest;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class GDPRController extends AbstractRestController {
    private final GDPRService gdprService;
    private final int GDPR_REQUESTS_PAGE_MAX = 100;

    public GDPRController(final Vertx aVertx, final GDPRService aGDPRService) {
        super(aVertx);

        if (Objects.isNull(aGDPRService)) {
            throw new IllegalArgumentException("invalid gdpr service specified.");
        }

        gdprService = aGDPRService;
    }

    @AsyncRestController(path="/privacy/gdpr", method= HttpMethod.POST, auth= AuthScheme.DIGEST)
    public CompletableFuture<Void> onCreateGDPRRequest(final RoutingContext aContext,
                                                        final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final User user = aContext.user();
            final String requesterId = routeParam(aContext, "requesterId");
            final JsonObject body = aContext.getBodyAsJson();
            final String userId = body.getString("user_id");
            final boolean clearMessageData = body.containsKey("message_clear_data") ?
                    body.getBoolean("message_clear_data") : false;

            return gdprService.createGDPRRequest(AuthenticatedUser.appId(user), userId, clearMessageData, requesterId)
                    .thenApply(requestId -> {
                        final JsonObject resultJson = new JsonObject();
                        resultJson.put("request_id", requestId);

                        WebUtils.doOK(aResponse, resultJson);
                        return null;
                    });
        });
    }

    @AsyncRestController(
            path="/privacy/gdpr/:requestId",
            method=HttpMethod.DELETE,
            auth=AuthScheme.DIGEST
    )
    public CompletableFuture<Void> onCancelGDPRRequest(final RoutingContext aContext,
                                                        final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            final User user = aContext.user();
            final String requestId = routeParam(aContext, "requestId");
            final String requesterId = routeParam(aContext, "requesterId");

            return gdprService.cancelGDPRRequest(AuthenticatedUser.appId(user), requestId, requesterId)
                    .thenApply(V -> {
                        WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, "{}");
                        return null;
                    });
        });
    }

    @SyncRestController(path="/privacy/gdpr", method=HttpMethod.GET, auth=AuthScheme.DIGEST)
    public void queryGDPRRequests(final RoutingContext aContext, final HttpServerResponse aResponse) throws IOException {
        try {
            final User user = aContext.user();
            final String appId = AuthenticatedUser.appId(user);
            final HttpServerRequest request = aContext.request();
            final Integer cursor = Objects.isNull(request.getParam("cursor")) ?
                    0 : Integer.parseInt(request.getParam("cursor"));
            final Integer pageSize = Objects.isNull(request.getParam("page_size")) ?
                    GDPR_REQUESTS_PAGE_MAX : Integer.parseInt(request.getParam("page_size"));
            final String userId = request.getParam("user_id");
            final String requestedBy = request.getParam("requested_by");
            final String cancelledBy = request.getParam("cancelled_by");
            final String status = request.getParam("status");
            final String requestId = request.getParam("request_id");
            final long startTs = Objects.isNull(request.getParam("start")) ?
                    0 : Long.parseLong(request.getParam("start"));
            final long endTs = Objects.isNull(request.getParam("end")) ?
                    QueryRange.MAX_TIMESTAMP : Long.parseLong(request.getParam("end"));

            gdprService.searchRequests(appId, requestId, userId, requestedBy, cancelledBy, status, cursor, pageSize, startTs, endTs)
                    .thenAccept(result -> {
                        JsonArray requests = new JsonArray();
                        for (GDPRRequest gdprRequest : result.gdprRequests()) {
                            JsonObject requestJson = new JsonObject();
                            requestJson.put("request_id", gdprRequest.id());
                            requestJson.put("user_id", gdprRequest.userId().value());
                            requestJson.put("message_clear_data", gdprRequest.clearMessageData());
                            requestJson.put("status", gdprRequest.status().value());
                            requestJson.put("requested_by", gdprRequest.requestedBy().value());
                            requestJson.put("created", gdprRequest.created());
                            if (Objects.nonNull(gdprRequest.cancelledBy())) {
                                requestJson.put("cancelled_by", gdprRequest.cancelledBy().value());
                            }
                            if (gdprRequest.updated() != 0) {
                                requestJson.put("updated", gdprRequest.updated());
                            }
                            requests.add(requestJson);
                        }

                        JsonObject resultJson = new JsonObject();
                        resultJson.put("requests", requests);
                        resultJson.put("total_requests", result.resultSize());

                        WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, resultJson.toString());
                    });
        } catch (Exception ex) {
            WebUtils.doErrorWithReason(aResponse, WebUtils.STATUS_BAD_REQUEST, ex.getMessage(), null);
        }
    }

    private String routeParam(final RoutingContext aContext, final String aParam) {
        return aContext.request().getParam(aParam);
    }
}
