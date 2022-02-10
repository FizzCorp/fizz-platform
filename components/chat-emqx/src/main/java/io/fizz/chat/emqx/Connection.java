package io.fizz.chat.emqx;

import io.fizz.chat.presence.Presence;
import io.fizz.chat.presence.persistence.AbstractPresenceRepository;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;
import io.vertx.core.Vertx;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class Connection {
    public interface Listener {
        void onDisconnected();
        void onPresenceUpdated(boolean aIsOnline);
        void onError(Throwable aCause);
    }

    private static final long OFFLINE_EPSILON_MS = 100;

    private final Vertx vertx;
    private final UserId userId;
    private final ApplicationId appId;
    private final AbstractPresenceRepository presenceRepo;
    private final long heartbeatDurationMs;
    private boolean connected = true;
    private long tickTimer;
    private Listener listener;

    public Connection(final Vertx aVertx,
                      final UserId aUserId,
                      final ApplicationId aAppId,
                      final AbstractPresenceRepository aPresenceRepo,
                      final long aHeartbeatDurationMs) {
        this.vertx = aVertx;
        this.userId = aUserId;
        this.appId = aAppId;
        this.presenceRepo = aPresenceRepo;
        this.heartbeatDurationMs = aHeartbeatDurationMs;
    }

    public CompletableFuture<Void> open(final Listener aListener) {
        if (Objects.isNull(aListener)) {
            return Utils.failedFuture(new IllegalArgumentException("invalid listener"));
        }

        listener = aListener;

        CompletableFuture<Presence> presenceFetched = presenceRepo.get(appId, userId);

        CompletableFuture<Void> updated =
            presenceFetched.thenCompose(aPresence -> {
                boolean isOnline = Presence.isOnline(aPresence, heartbeatDurationMs);
                if (!isOnline) {
                    listener.onPresenceUpdated(true);
                }

                return presenceRepo.put(appId, userId, new Presence(new Date().getTime()));
            });

        return updated.thenApply(aVoid -> {
            tickTimer = vertx.setTimer(heartbeatDurationMs, this::onTick);
            return null;
        });

    }

    public void connected() {
        connected = true;
    }

    public void disconnected() {
        connected = false;
    }

    public void close() {
        vertx.cancelTimer(tickTimer);
    }

    private void onTick(long aTimerId) {
        if (connected) {
            onConnectedTick();
        }
        else {
            onDisconnectedTick();
        }
    }

    private void onConnectedTick() {
        presenceRepo.put(appId, userId, new Presence(new Date().getTime()));
        tickTimer = vertx.setTimer(heartbeatDurationMs, this::onTick);
    }

    private void onDisconnectedTick() {
        presenceRepo.get(appId, userId).thenApply(aPresence -> {
            if (!Presence.isOnline(aPresence, heartbeatDurationMs)) {
                listener.onPresenceUpdated(false);
            }

            listener.onDisconnected();

            return null;
        });
    }
}
