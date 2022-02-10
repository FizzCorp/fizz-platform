package io.fizz.chat.emqx.infrastructure.listener;

import com.google.common.cache.Cache;
import com.newrelic.api.agent.NewRelic;
import io.fizz.chat.emqx.Connection;
import io.fizz.chat.emqx.infrastructure.EMQXConnectedEvent;
import io.fizz.chat.emqx.infrastructure.EMQXDisconnectedEvent;
import io.fizz.chat.presence.ConfigService;
import io.fizz.chat.presence.persistence.AbstractPresenceRepository;
import io.fizz.chat.pubsub.application.AbstractTopicMessagePublisher;
import io.fizz.chat.pubsub.infrastructure.messaging.EMQXTopicMessagePublisher;
import io.fizz.chat.pubsub.infrastructure.serde.JsonTopicMessageSerde;
import io.fizz.chat.user.application.ProfileNotificationService;
import io.fizz.chatcommon.domain.events.AbstractDomainEvent;
import io.fizz.chatcommon.domain.events.AbstractEventListener;
import io.fizz.chatcommon.domain.events.DomainEventType;
import io.fizz.client.hbase.client.AbstractHBaseClient;
import io.fizz.common.LoggingService;
import io.fizz.common.NewRelicErrorPriority;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;
import io.fizz.session.SessionUtils;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Session;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class EMQXConnectionEventListener implements AbstractEventListener {
    private static final LoggingService.Log logger = LoggingService.getLogger(EMQXConnectionEventListener.class);
    public void DEBUG(String fn, Object req) {
        System.out.println(fn + ", request: " + req);
    }
    private static final long HEARTBEAT_DURATION = ConfigService.config().getNumber("chat.emqx.heartbeat.ms").intValue();

    private static final DomainEventType[] events = new DomainEventType[] {
            EMQXConnectedEvent.TYPE,
            EMQXDisconnectedEvent.TYPE
    };

    private final Vertx vertx;

    private final Cache<String, Session> cachedSessions;
    private final Map<String, Connection> connections;
    private final AbstractPresenceRepository presenceRepo;

    private ProfileNotificationService notificationService;

    public EMQXConnectionEventListener(Vertx aVertx,
                                       final Cache<String, Session> cachedSessions,
                                       final AbstractPresenceRepository presenceRepo,
                                       final Map<String, Connection> connections) {
        this.cachedSessions = cachedSessions;
        this.presenceRepo = presenceRepo;
        this.connections = connections;
        this.vertx = aVertx;

        initProfileNotificationService();

    }

    @Override
    public DomainEventType[] listensTo() {
        return events;
    }

    @Override
    public CompletableFuture<Void> handleEvent(AbstractDomainEvent aEvent) {
        CompletableFuture<Void> future;
        DEBUG("onClientAuthenticate", aEvent);
        if (aEvent.type().equals(EMQXConnectedEvent.TYPE)) {
            future = on((EMQXConnectedEvent)aEvent);
        }else if (aEvent.type().equals(EMQXDisconnectedEvent.TYPE)) {
            future = on((EMQXDisconnectedEvent)aEvent);
        }else {
            future = CompletableFuture.completedFuture(null);
        }

        return future.handle((result, error) -> {
            if (Objects.nonNull(error)) {
                logger.error(error);
                NewRelic.noticeError(error,
                        Utils.buildNewRelicErrorMap(NewRelicErrorPriority.HIGH,
                                "ChannelEventListener::handleEvent"));
            }
            return null;
        });
    }

    private CompletableFuture<Void> on(final EMQXDisconnectedEvent aEvent) {
        return onDisconnected(aEvent.getClientId());
    }

    public CompletableFuture<Void> onDisconnected(String aClientId) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        logger.debug(aClientId + " client_disconnected");
        if (connections.containsKey(aClientId)) {
            logger.debug(aClientId + " client_disconnected2");
            connections.get(aClientId).disconnected();
        }
        future.complete(null);
        return future;
    }

    private CompletableFuture<Void> on(final EMQXConnectedEvent aEvent) {
        return onConnected(aEvent.getClientId(), aEvent.getUsername());
    }

    public CompletableFuture<Void> onConnected(String aClientId, String aUsername) {
        logger.debug(aClientId + " client_connected");
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (connections.containsKey(aClientId)) {
            connections.get(aClientId).connected();
            future.complete(null);
        }

        logger.debug("creating client");

        Session session = cachedSessions.getIfPresent(aClientId);
        if (Objects.isNull(session)) {
            logger.error("trying to access non-existent session for: " + aClientId);
            future.complete(null);
        }

        try {
            final UserId userId = new UserId(aUsername);
            final ApplicationId appId = new ApplicationId(SessionUtils.getAppId(session));

            return createConnection(appId, userId, aClientId)
                    .thenApply(aResult -> aResult);
        }
        catch (Exception ex) {
            logger.error(ex.getMessage());
            future.complete(null);
        }
        return future;
    }

    public CompletableFuture<Void> createConnection(final ApplicationId aAppId, final UserId aUserId, final String aClientId) {
        Connection connection = new Connection(
                vertx, aUserId, aAppId, presenceRepo, HEARTBEAT_DURATION
        );
        CompletableFuture<Void> future = new CompletableFuture<>();

        connections.put(aClientId, connection);
        connection.open(new Connection.Listener() {
            @Override
            public void onDisconnected() {
                connection.close();
                connections.remove(aClientId);
                future.complete(null);
            }

            @Override
            public void onPresenceUpdated(boolean aIsOnline) {
                publish(aAppId, aUserId, aIsOnline);

                if (aIsOnline) {
                    logger.debug(aUserId.value() + " has come online!");
                }
                else {
                    logger.debug(aUserId.value() + " has gone offline!");
                }
            }

            @Override
            public void onError(Throwable aCause) {
                logger.fatal(aCause.getMessage());
            }
        })
                .handle((aVoid, aError) -> {
                    if (Objects.nonNull(aError)) {
                        logger.fatal(aError.getMessage());
                        connection.close();
                        connections.remove(aClientId);
                        future.complete(null);
                    }
                    future.complete(null);
                    return null;
                });
        return future;
    }

    private void publish(final ApplicationId aAppId, final UserId aUserId, boolean isOnline) {
        notificationService.publishOnlineStatus(aAppId, aUserId, isOnline)
                .handle((aVoid, aError) -> {
                    if (Objects.nonNull(aError)) {
                        logger.error(aError.getMessage());
                    }

                    return null;
                });
    }


    private void initProfileNotificationService() {
        final AbstractTopicMessagePublisher publisher = new EMQXTopicMessagePublisher(vertx, new JsonTopicMessageSerde());
        notificationService = new ProfileNotificationService(publisher);
    }
}