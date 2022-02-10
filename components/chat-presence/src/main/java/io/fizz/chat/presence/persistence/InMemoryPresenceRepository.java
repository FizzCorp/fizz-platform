package io.fizz.chat.presence.persistence;

import io.fizz.chat.presence.Presence;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPresenceRepository implements AbstractPresenceRepository {
    private final Map<String, Presence> repo = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<Presence> get(final ApplicationId aAppId, final UserId aUserId) {
        if (Objects.isNull(aAppId) || Objects.isNull(aUserId)) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.completedFuture(repo.get(aUserId.qualifiedValue(aAppId)));
    }

    @Override
    public CompletableFuture<Void> put(final ApplicationId aAppId, final UserId aUserId, final Presence aPresence) {
        if (Objects.isNull(aAppId) || Objects.isNull(aUserId) || Objects.isNull(aPresence)) {
            return CompletableFuture.completedFuture(null);
        }

        repo.put(aUserId.qualifiedValue(aAppId), new Presence(aPresence.lastSeen()));

        return CompletableFuture.completedFuture(null);
    }
}
