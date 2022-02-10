package io.fizz.gateway.http.controllers;

import io.fizz.chat.pubsub.domain.subscriber.AbstractSubscriberRepository;
import io.fizz.chataccess.domain.AbstractAuthorizationService;
import io.fizz.chataccess.domain.role.RoleName;
import io.fizz.chatcommon.domain.LanguageCode;
import io.fizz.chatcommon.domain.PublicChannelId;
import io.fizz.chatcommon.infrastructure.WebUtils;
import io.fizz.common.ConfigService;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.common.domain.UserId;
import io.fizz.gateway.http.annotations.AsyncRestController;
import io.fizz.gateway.http.annotations.AuthScheme;
import io.fizz.session.SessionUtils;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.sstore.SessionStore;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class AuthController extends AbstractRestController {
    private static final int SESSION_CHANNELS_SIZE_MAX = ConfigService.instance().getNumber("session.channels.size.max").intValue();

    private final SessionStore store;
    private final AbstractSubscriberRepository subscriberRepo;
    private final AbstractAuthorizationService authorizationService;
    private final RoleName adminRole;
    private final RoleName mutedRole;
    private final long sessionTimeoutMS;

    public AuthController(final Vertx aVertx,
                          final AbstractSubscriberRepository aSubscriberRepo,
                          final AbstractAuthorizationService aAuthService,
                          final RoleName aAdminRole,
                          final RoleName aMutedRole,
                          final SessionStore aStore,
                          final long aTimeoutMS) {
        super(aVertx);

        Utils.assertRequiredArgument(aStore, "invalid session store specified.");
        Utils.assertRequiredArgument(aAuthService, "invalid auth service specified.");
        Utils.assertRequiredArgument(aAdminRole, "invalid admin role specified.");
        Utils.assertRequiredArgument(aMutedRole, "invalid muted role specified.");
        Utils.assertRequiredArgument(aSubscriberRepo, "invalid subscriber repo specified.");

        if (aTimeoutMS < 0) {
            throw new IllegalArgumentException("invalid session timeout specified.");
        }

        store = aStore;
        subscriberRepo = aSubscriberRepo;
        authorizationService = aAuthService;
        adminRole = aAdminRole;
        mutedRole = aMutedRole;
        sessionTimeoutMS = aTimeoutMS;
    }

    @AsyncRestController(path="/sessions", method=HttpMethod.POST, auth=AuthScheme.DIGEST)
    CompletableFuture<Void> onCreateSession(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            try {
                final JsonObject body = aContext.getBodyAsJson();
                final ApplicationId appId = new ApplicationId(body.getString("app_id"));
                final UserId userId = new UserId(body.getString("user_id"));
                final Session session = store.createSession(sessionTimeoutMS);
                final String subscriberId = subscriberRepo.nextIdentity().value();
                final String channels = channels(appId, body.getJsonArray("channels"));

                SessionUtils.setAppId(session, appId.value());
                SessionUtils.setUserId(session, userId.value());
                SessionUtils.setSubscriberId(session, subscriberId);

                if (body.containsKey("locale")) {
                    final LanguageCode locale = LanguageCode.fromValue(body.getString("locale"));
                    SessionUtils.setLocale(session, locale.value());
                }
                else {
                    SessionUtils.setLocale(session, LanguageCode.ENGLISH.value());
                }

                SessionUtils.setChannelFilter(session, channels);

                return persistSession(aResponse, session, subscriberId);
            }
            catch (DomainErrorException ex) {
                return Utils.failedFuture(ex);
            }
        });
    }

    @AsyncRestController(path="/sessions/:userId", method=HttpMethod.POST, auth=AuthScheme.DIGEST)
    CompletableFuture<Void> onUpdateSession(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            CompletableFuture<Void> updated = new CompletableFuture<>();
            final String userId = aContext.request().getParam("userId");
            final JsonObject body = aContext.getBodyAsJson();
            String token = body.getString("token");

            store.get(token, aResult -> {
                if (aResult.succeeded()) {
                    try {
                        Session session = aResult.result();
                        if (Objects.isNull(session)) {
                            WebUtils.doErrorWithReason(
                                    aResponse,
                                    WebUtils.STATUS_NOT_FOUND,
                                    "session_expired",
                                    null
                            );
                            return;
                        }

                        if (Objects.isNull(userId) || !userId.equals(SessionUtils.getUserId(session))) {
                            Utils.failFuture(updated, new SecurityException("can't update other user's session"));
                            return;
                        }

                        ApplicationId appId = new ApplicationId(SessionUtils.getAppId(session));
                        if (body.containsKey("channels"))
                        {
                            final String channels = channels(appId, body.getJsonArray("channels"));
                            SessionUtils.setChannelFilter(session, channels);
                        }
                        updateSession(updated, aResponse, session);
                    } catch (DomainErrorException ex) {
                        Utils.failFuture(updated, ex);
                    }
                } else {
                    Utils.failFuture(updated, aResult.cause());
                }
            });
            return updated;
        });
    }

    @AsyncRestController(path="/admins", method=HttpMethod.POST, auth=AuthScheme.DIGEST)
    CompletableFuture<Void> onAddAdmin(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            try {
                final JsonObject body = aContext.getBodyAsJson();
                final ApplicationId appId = new ApplicationId(body.getString("app_id"));
                final UserId adminId = new UserId(body.getString("user_id"));

                return authorizationService.assignAppRole(appId, adminId, adminRole, Utils.TIME_END)
                        .thenApply(v -> {
                            WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, "{}");
                            return null;
                        });
            }
            catch (DomainErrorException ex) {
                return Utils.failedFuture(ex);
            }
        });
    }

    @AsyncRestController(path="/admins", method=HttpMethod.DELETE, auth=AuthScheme.DIGEST)
    CompletableFuture<Void> onRemoveAdmins(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            try {
                final JsonObject body = aContext.getBodyAsJson();
                final ApplicationId appId = new ApplicationId(body.getString("app_id"));
                final UserId adminId = new UserId(body.getString("user_id"));

                return authorizationService.removeAppRole(appId, adminId, adminRole)
                        .thenApply(v -> {
                            WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, "{}");
                            return null;
                        });
            }
            catch (DomainErrorException ex) {
                return Utils.failedFuture(ex);
            }
        });
    }

    @AsyncRestController(path="/mutes", method=HttpMethod.POST, auth=AuthScheme.DIGEST)
    CompletableFuture<Void> onAddAppMute(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            try {
                final JsonObject body = aContext.getBodyAsJson();
                final ApplicationId appId = new ApplicationId(body.getString("app_id"));
                final UserId userIdToMute = new UserId(body.getString("user_id"));
                final Date ends = parseRoleEnd(body);

                return authorizationService.assignAppRole(appId, userIdToMute, mutedRole, ends)
                        .thenApply(v -> {
                            WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, "{}");
                            return null;
                        });
            }
            catch (DomainErrorException ex) {
                return Utils.failedFuture(ex);
            }
        });
    }

    @AsyncRestController(path="/mutes", method=HttpMethod.DELETE, auth=AuthScheme.DIGEST)
    CompletableFuture<Void> onRemoveAppMute(final RoutingContext aContext, final HttpServerResponse aResponse) {
        return Utils.async(() -> {
            try {
                final JsonObject body = aContext.getBodyAsJson();
                final ApplicationId appId = new ApplicationId(body.getString("app_id"));
                final UserId mutedUserId = new UserId(body.getString("user_id"));

                return authorizationService.removeAppRole(appId, mutedUserId, mutedRole)
                        .thenApply(v -> {
                            WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, "{}");
                            return null;
                        });
            }
            catch (DomainErrorException ex) {
                return Utils.failedFuture(ex);
            }
        });
    }

    private CompletableFuture<Void> persistSession(final HttpServerResponse aResponse,
                                                   final Session aSession,
                                                   final String aSubscriberId) {
        final CompletableFuture<Void> persisted = new CompletableFuture<>();

        store.put(aSession, aResult -> {
            if (aResult.succeeded()) {
                WebUtils.doOK(
                    aResponse,
                    new JsonObject()
                        .put("token", aSession.id())
                        .put("subscriber_id", aSubscriberId)
                        .put("now_ts", new Date().getTime())
                );
                persisted.complete(null);
            }
            else {
                Utils.failFuture(persisted, aResult.cause());
            }
        });

        return persisted;
    }

    private void updateSession(final CompletableFuture<Void> resultFuture,
                               final HttpServerResponse aResponse,
                               final Session aSession) {
        store.put(aSession, aResult -> {
            if (aResult.succeeded()) {
                WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, "{}");
                resultFuture.complete(null);
            }
            else {
                Utils.failFuture(resultFuture, aResult.cause());
            }
        });
    }

    private String channels(final ApplicationId aAppId, final JsonArray aChannels) {
        // All Channels
        final String DEFAULT_CHANNELS = ".*";
        if (Objects.isNull(aChannels)) {
            return DEFAULT_CHANNELS;
        }

        Utils.assertArgumentRange(aChannels.size(), 0, SESSION_CHANNELS_SIZE_MAX, "invalid_channels_array_size");

        // Regex
        final String REGEX_STARTS_WITH = "%s.*";
        final String REGEX_MATCH = "%s";
        final String REGEX_JOIN_OPERATOR = "|";

        // Channel filter rules keys
        final String KEY_RULE_MATCHES = "matches";
        final String KEY_RULE_STARTS_WITH = "startsWith";

        StringBuilder channelFilterBuilder = new StringBuilder();
        for (int i = 0; i < aChannels.size(); i++) {
            final JsonObject customChannelRule = aChannels.getJsonObject(i);
            String channelFilter = null;
            if (customChannelRule.containsKey(KEY_RULE_MATCHES)) {
                PublicChannelId validChannel = new PublicChannelId(aAppId, customChannelRule.getString(KEY_RULE_MATCHES));
                channelFilter = String.format(REGEX_MATCH, validChannel.value());
            } else if (customChannelRule.containsKey(KEY_RULE_STARTS_WITH)) {
                PublicChannelId validChannel = new PublicChannelId(aAppId, customChannelRule.getString(KEY_RULE_STARTS_WITH));
                channelFilter = String.format(REGEX_STARTS_WITH, validChannel.value());
            }
            if (!Objects.isNull(channelFilter)) {
                if (channelFilterBuilder.length() != 0) {
                    channelFilterBuilder.append(REGEX_JOIN_OPERATOR);
                }
                channelFilterBuilder.append(channelFilter);
            }
        }

        return channelFilterBuilder.toString();
    }

    private Date parseRoleEnd(final JsonObject aBody) {
        if (aBody.containsKey("duration")) {
            return new Date(Utils.now() + aBody.getLong("duration")*1000L);
        }
        else {
            return Utils.TIME_END;
        }
    }
}
