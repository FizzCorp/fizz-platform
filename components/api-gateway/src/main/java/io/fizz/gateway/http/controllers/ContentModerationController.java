package io.fizz.gateway.http.controllers;

import io.fizz.chat.moderation.application.service.ContentModerationService;
import io.fizz.chatcommon.infrastructure.WebUtils;
import io.fizz.common.Utils;
import io.fizz.gateway.http.annotations.AsyncRestController;
import io.fizz.gateway.http.annotations.AuthScheme;
import io.fizz.session.SessionUtils;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ContentModerationController extends AbstractRestController  {
    private final ContentModerationService moderationService;

    public ContentModerationController(Vertx aVertx, final ContentModerationService aModerationService) {
        super(aVertx);

        if (Objects.isNull(aModerationService)) {
            throw new IllegalArgumentException("invalid content moderation service specified.");
        }

        moderationService = aModerationService;
    }

    @AsyncRestController(path="/moderatedTexts", method= HttpMethod.POST, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onContentModeration(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {

            final List<String> texts = texts(aContext);
            final String appId = SessionUtils.getAppId(aContext.session());
            return moderationService.filter(appId, texts)
                    .thenApply(filteredTexts -> {
                        JsonArray resultArr = new JsonArray();
                        for (String text : filteredTexts) {
                            resultArr.add(text);
                        }
                        WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, resultArr.toString());
                        return null;
                    });
        });
    }

    private List<String> texts(final RoutingContext aContext) {
        JsonArray textArr = aContext.getBodyAsJsonArray();
        List<String> texts = new ArrayList<>();
        for (int ii=0; ii<textArr.size(); ii++) {
            texts.add(textArr.getString(ii));
        }
        return texts;
    }

}
