package io.fizz.gateway.http.controllers.ingestion;

import com.google.gson.Gson;
import io.fizz.chatcommon.infrastructure.WebUtils;
import io.fizz.common.Utils;
import io.fizz.common.domain.*;
import io.fizz.common.domain.events.*;
import io.fizz.gateway.http.controllers.AbstractRestController;
import io.fizz.gateway.http.services.handler.eventstream.AbstractEventStreamClientHandler;
import io.fizz.gateway.services.GeoLocationService;
import io.fizz.gateway.services.USDConversionService;
import io.fizz.logger.application.service.LoggerService;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractEventsController extends AbstractRestController {
    protected final AbstractEventStreamClientHandler eventStreamHandler;
    protected final USDConversionService conversionService;
    protected final LoggerService loggerService;

    AbstractEventsController(final Vertx aVertx,
                             final AbstractEventStreamClientHandler aEventStreamHandler,
                             final USDConversionService aConversionService,
                             final LoggerService aLoggerService) {
        super(aVertx);

        if (Objects.isNull(aEventStreamHandler)) {
            throw new IllegalArgumentException("Invalid event stream handler specified.");
        }
        if (Objects.isNull(aConversionService)) {
            throw new IllegalArgumentException("Invalid currency conversion service specified.");
        }
        if (Objects.isNull(aLoggerService)) {
            throw new IllegalArgumentException("Invalid logger service specified.");
        }
        eventStreamHandler = aEventStreamHandler;
        conversionService = aConversionService;
        loggerService = aLoggerService;
    }


     CompletableFuture<Void> submitEvents(final RoutingContext aContext, final HttpServerResponse aResponse, final ApplicationId aAppId) {
        return Utils.async(() -> {
            final List<AbstractDomainEvent> events = new ArrayList<>();
            final List<WebUtils.NotificationError> notification = new ArrayList<>();
            processEvents(aContext, events, notification, aAppId);

            return flushEvents(events)
            .thenApply(v -> {
                if (notification.size() > 0) {
                    WebUtils.doErrorWithReason(
                            aResponse,
                            WebUtils.STATUS_BAD_REQUEST,
                            "invalid_event_data",
                            notification
                    );
                    postEventErrorLog(aAppId, aContext.getBodyAsString(), notification);
                }
                else {
                    WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, "{}");
                }
                return null;
            });
        });
    }

    private void postEventErrorLog(final ApplicationId aAppId,
                                   final @Nullable String aBody,
                                   final List<WebUtils.NotificationError> aNotification) {
        JsonObject loggerResponse = new JsonObject();
        loggerResponse.put("appId", aAppId.value());
        loggerResponse.put("error", new Gson().toJson(new WebUtils.Error("invalid_event_data", aNotification)));
        loggerResponse.put("events", aBody);

        WebUtils.logError(loggerResponse.toString());
    }

    private CompletableFuture<Integer> flushEvents(List<AbstractDomainEvent> events) {
        if (events.size() <= 0) {
            return CompletableFuture.completedFuture(0);
        }

        return eventStreamHandler.put(events);
    }

    private void processEvents(final RoutingContext aContext,
                               final List<AbstractDomainEvent> aOutEvents,
                               final List<WebUtils.NotificationError> aOutNotification,
                               final ApplicationId aAppId) {
        String ipAddress = getClientIP(aContext.request());
        final CountryCode countryCode = GeoLocationService.instance().getCountryCode(ipAddress);

        final JsonArray events = aContext.getBodyAsJsonArray();

        for (int ii = 0; ii < events.size(); ii++) {
            try {
                final AbstractDomainEvent event = parseEvent(events.getJsonObject(ii), aAppId, countryCode);
                if (Objects.isNull(event)) {
                    aOutNotification.add(new WebUtils.NotificationError(ii, "invalid_event_type"));
                }
                else {
                    aOutEvents.add(event);
                }
            }
            catch (DomainErrorException ex) {
                aOutNotification.add(new WebUtils.NotificationError(ii, ex.error().reason()));
            }
            catch (IllegalArgumentException ex) {
                aOutNotification.add(new WebUtils.NotificationError(ii, ex.getMessage()));
            }
        }
    }

    private String getClientIP(HttpServerRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if(Objects.isNull(ipAddress) || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if(Objects.isNull(ipAddress) || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");

        }
        if(Objects.isNull(ipAddress) || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.remoteAddress().host();
        }
        return ipAddress;
    }

    private AbstractDomainEvent parseEvent(final JsonObject aEventBody,
                                           final ApplicationId aAppId,
                                           CountryCode countryCode) throws DomainErrorException {
        final String type = aEventBody.getString(EventTokens.TYPE.value());
        if (Objects.isNull(type)) {
            return null;
        }

        switch (type) {
            case "session_started":
                return parseSessionStarted(aEventBody, aAppId, countryCode);
            case "session_ended":
                return parseSessionEnded(aEventBody, aAppId, countryCode);
            case "text_msg_sent":
                return parseTextMessageSent(aEventBody, aAppId, countryCode);
            case "product_purchased":
                return parseProductPurchased(aEventBody, aAppId, countryCode);
        }
        return null;
    }

    private AbstractDomainEvent parseSessionStarted(final JsonObject aEventBody,
                                                    final ApplicationId aAppId,
                                                    CountryCode countryCode) throws DomainErrorException {
        final SessionStarted.Builder builder = new SessionStarted.Builder();

        parseAbstractDomainEvent(builder, aEventBody, aAppId, countryCode);

        return builder.get();
    }

    private AbstractDomainEvent parseSessionEnded(final JsonObject aEventBody,
                                                  final ApplicationId aAppId,
                                                  CountryCode countryCode) throws DomainErrorException {
        final SessionEnded.Builder builder = new SessionEnded.Builder()
                .setDuration(aEventBody.getInteger(EventTokens.DURATION.value()));

        parseAbstractDomainEvent(builder, aEventBody, aAppId, countryCode);

        return builder.get();
    }

    private AbstractDomainEvent parseTextMessageSent(final JsonObject aEventBody,
                                                     final ApplicationId aAppId,
                                                     CountryCode countryCode) throws DomainErrorException {
        final TextMessageSent.Builder builder = new TextMessageSent.Builder()
                .setContent(aEventBody.getString(EventTokens.CONTENT.value()))
                .setChannelId(aEventBody.getString(EventTokens.CHANNEL_ID.value()))
                .setNick(aEventBody.getString(EventTokens.NICK.value()));

        parseAbstractDomainEvent(builder, aEventBody, aAppId, countryCode);

        return builder.get();
    }

    private AbstractDomainEvent parseProductPurchased(final JsonObject aEventBody,
                                                      final ApplicationId aAppId,
                                                      CountryCode countryCode) throws DomainErrorException {
        if (!aEventBody.containsKey(EventTokens.AMOUNT.value())) {
            throw ProductPurchased.ERROR_INVALID_AMOUNT;
        }

        final double amount = aEventBody.getDouble(EventTokens.AMOUNT.value());
        final String productId = aEventBody.getString(EventTokens.PRODUCT_ID.value());
        final String receipt = aEventBody.getString(EventTokens.RECEIPT.value());
        final CurrencyCode currency = new CurrencyCode(aEventBody.getString(EventTokens.CURRENCY.value()));
        final int amountInCents = conversionService.toCents(currency, amount);

        final ProductPurchased.Builder builder = new ProductPurchased.Builder()
                .setAmountInCents(amountInCents)
                .setProductId(productId)
                .setReceipt(receipt);

        parseAbstractDomainEvent(builder, aEventBody, aAppId, countryCode);

        return builder.get();
    }

    private void parseAbstractDomainEvent(final AbstractDomainEvent.Builder aBuilder,
                                          final JsonObject aEventBody,
                                          final ApplicationId aAppId,
                                          CountryCode countryCode) throws DomainErrorException {
        aBuilder
                .setId(UUID.randomUUID().toString())
                .setAppId(aAppId)
                .setCountryCode(countryCode)
                .setUserId(parseUserId(aEventBody))
                .setVersion(parseVersion(aEventBody))
                .setSessionId(parseSessionId(aEventBody))
                .setOccurredOn(parseOccurredOn(aEventBody))
                .setPlatform(parsePlatform(aEventBody))
                .setBuild(parseBuild(aEventBody))
                .setCustom01(parseCustomSegment01(aEventBody))
                .setCustom02(parseCustomSegment02(aEventBody))
                .setCustom03(parseCustomSegment03(aEventBody));
    }

    private UserId parseUserId(final JsonObject aBody) throws DomainErrorException {
        if (!aBody.containsKey(EventTokens.USER_ID.value())) {
            return null;
        }

        return new UserId(aBody.getString(EventTokens.USER_ID.value()));
    }

    private Integer parseVersion(final JsonObject aBody) {
        return aBody.getInteger(EventTokens.VERSION.value());
    }

    private String parseSessionId(final JsonObject aBody) {
        return aBody.getString(EventTokens.SESSION_ID.value());
    }

    private Long parseOccurredOn(final JsonObject aBody) {
        return aBody.getLong(EventTokens.TIME.value());
    }

    private Platform parsePlatform(final JsonObject aBody) throws DomainErrorException {
        if (!aBody.containsKey(EventTokens.PLATFORM.value())) {
            return null;
        }

        return new Platform(aBody.getString(EventTokens.PLATFORM.value()));
    }

    private String parseBuild(final JsonObject aBody) {
        if (!aBody.containsKey(EventTokens.BUILD.value())) {
            return null;
        }
        return aBody.getString(EventTokens.BUILD.value());
    }

    private String parseCustomSegment01(final JsonObject aBody) {
        if (!aBody.containsKey(EventTokens.CUSTOM_01.value())) {
            return null;
        }
        return aBody.getString(EventTokens.CUSTOM_01.value());
    }

    private String parseCustomSegment02(final JsonObject aBody) {
        if (!aBody.containsKey(EventTokens.CUSTOM_02.value())) {
            return null;
        }
        return aBody.getString(EventTokens.CUSTOM_02.value());
    }

    private String parseCustomSegment03(final JsonObject aBody) {
        if (!aBody.containsKey(EventTokens.CUSTOM_03.value())) {
            return null;
        }
        return aBody.getString(EventTokens.CUSTOM_03.value());
    }
}
