package io.fizz.chat.emqx.infrastructure.service;

import com.google.common.cache.Cache;
import io.fizz.chat.emqx.Connection;
import io.fizz.chat.emqx.HTTPVerticle;
import io.fizz.chat.emqx.infrastructure.ConfigService;
import io.fizz.chat.pubsub.application.AbstractTopicMessagePublisher;
import io.fizz.chat.pubsub.infrastructure.messaging.EMQXTopicMessagePublisher;
import io.fizz.chat.pubsub.infrastructure.serde.JsonTopicMessageSerde;
import io.fizz.chat.user.application.ProfileNotificationService;
import io.fizz.chatcommon.infrastructure.WebUtils;
import io.fizz.client.hbase.client.AbstractHBaseClient;
import io.fizz.common.Config;
import io.fizz.common.LoggingService;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;
import io.fizz.session.HBaseSessionStore;
import io.fizz.session.SessionUtils;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.sstore.SessionStore;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class AuthService {

    private static final LoggingService.Log log = LoggingService.getLogger(HTTPVerticle.class);

    private final Vertx vertx;

    private final AbstractHBaseClient hbaseClient;
    private final Cache<String, Session> cachedSessions;

    private SessionStore sessionStore;

    public AuthService(Vertx aVertx,
                       final AbstractHBaseClient hbaseClient,
                       final Cache<String, Session> cachedSessions) {
        this.hbaseClient = hbaseClient;
        this.cachedSessions = cachedSessions;
        this.vertx = aVertx;

        initSessionStores();

    }

    public CompletableFuture<Boolean> onClientAuthenticate(String clientId, String username, String password) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        sessionStore.get(password, aResult -> {
            if (aResult.succeeded()) {
                if (validate(aResult.result(), username, clientId)) {
                    cachedSessions.put(clientId, aResult.result());
                    log.debug("allowed");
                    future.complete(true);
                }
                else {
                    future.complete(false);
                }
            }
            else {
                future.complete(false);
            }
        });

        return future;
    }

    private boolean validate(final Session aSession, final String aUsername, final String aClientId) {
        final String userId = SessionUtils.getUserId(aSession);
        if (Objects.isNull(userId) || !userId.equals(aUsername)) {
            return false;
        }

        final String subscriberId = SessionUtils.getSubscriberId(aSession);

        return !Objects.isNull(subscriberId) && subscriberId.equals(aClientId);
    }

    private void initSessionStores() {
        sessionStore = new HBaseSessionStore(vertx, hbaseClient);
    }
}
