package io.fizz.gateway.http.controllers.ingestion;

import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.gateway.http.annotations.AuthScheme;
import io.fizz.gateway.http.annotations.SyncRestController;
import io.fizz.gateway.http.services.handler.eventstream.AbstractEventStreamClientHandler;
import io.fizz.gateway.services.USDConversionService;
import io.fizz.logger.application.service.LoggerService;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

import java.util.concurrent.CompletableFuture;

public class DigestEventsController extends SessionEventsController {
    public DigestEventsController(Vertx aVertx,
                                  AbstractEventStreamClientHandler aEventStreamHandler,
                                  USDConversionService aConversionService,
                                  final LoggerService aLoggerService) {
        super(aVertx, aEventStreamHandler, aConversionService, aLoggerService);
    }

    @SyncRestController(path="/apps/:appId/events", method= HttpMethod.POST, auth=AuthScheme.DIGEST)
    public CompletableFuture<Void> submitEvents(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> application(aContext)
                .thenCompose(appId -> submitEvents(aContext, aResponse, appId)));
    }

    private CompletableFuture<ApplicationId> application(final RoutingContext aContext) {
        try {
            final ApplicationId appId = new ApplicationId(appId(aContext));
            return CompletableFuture.completedFuture(appId);
        } catch (DomainErrorException e) {
            return Utils.failedFuture(e);
        }
    }

    private String appId(final RoutingContext aContext) {
        return aContext.request().getParam("appId");
    }
}
