package io.fizz.chat.presence.infrastructure.presence;

import io.fizz.chat.presence.AbstractUserPresenceService;
import io.fizz.chat.presence.ConfigService;
import io.fizz.chat.presence.Presence;
import io.fizz.chat.presence.persistence.AbstractPresenceRepository;
import io.fizz.common.LoggingService;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;
import io.vertx.core.Vertx;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class UserPresenceService implements AbstractUserPresenceService {
    private static final LoggingService.Log log = LoggingService.getLogger(UserPresenceService.class);

    private static final long HEARTBEAT_DURATION = ConfigService.config().getNumber("chat.emqx.heartbeat.ms").intValue();

    private final AbstractPresenceRepository presenceRepo;


    public UserPresenceService(final Vertx aVertx, final AbstractPresenceRepository aPresenceRepo) {
        if (Objects.isNull(aVertx)) {
            throw new IllegalArgumentException("invalid vertx instance specified.");
        }

        this.presenceRepo = aPresenceRepo;
    }

    @Override
    public CompletableFuture<Boolean> get(ApplicationId aAppId, UserId aUserId) {
        final CompletableFuture<Boolean> result = new CompletableFuture<>();

        this.onGetPresence(aAppId, aUserId)
                .thenApply(res -> result.complete(res));
        return result;
    }

    @Override
    public CompletableFuture<Set<UserId>> getOfflineUsers(final ApplicationId aAppId, final Set<UserId> aUserIds) {
        final CompletableFuture<Set<UserId>> resultFuture = new CompletableFuture<>();

        final List<UserId> requestedUsers = new ArrayList<>(aUserIds);
        final List<CompletableFuture<Boolean>> requestedFutures = new ArrayList<>();
        for (UserId userId: requestedUsers) {
            requestedFutures.add(get(aAppId, userId));
        }

        CompletableFuture.allOf(requestedFutures.toArray(new CompletableFuture<?>[0]))
                .handle((v, error) -> {
                    Set<UserId> offlineUsers = new HashSet<>();
                    for (int i = 0; i < requestedFutures.size(); i++) {
                        CompletableFuture<Boolean> future = requestedFutures.get(i);
                        if (!future.join()) {
                            UserId userId = requestedUsers.get(i);
                            offlineUsers.add(userId);
                        }
                    }
                    resultFuture.complete(offlineUsers);
                    return null;
                });

        return resultFuture;
    }

    private CompletableFuture<Boolean> onGetPresence(ApplicationId appId, UserId userId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        try {
            CompletableFuture<Presence> fetched = presenceRepo.get(appId, userId);

            fetched.handle((aPresence, err) -> {
                if (Objects.nonNull(err)) {
                     future.complete(false);
                } else if (Objects.isNull(aPresence)) {
                    future.complete(false);
                } else {
                    future.complete(Presence.isOnline(aPresence, HEARTBEAT_DURATION));
                }
                return null;
            });
        }
        catch (IllegalArgumentException ex) {
            future.complete(false);
        }
        return future;
    }

}
