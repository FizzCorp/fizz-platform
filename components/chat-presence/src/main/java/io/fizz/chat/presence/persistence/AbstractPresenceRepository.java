package io.fizz.chat.presence.persistence;

import io.fizz.chat.presence.Presence;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;

import java.util.concurrent.CompletableFuture;

public interface AbstractPresenceRepository {
    CompletableFuture<Presence> get(ApplicationId aAppId, UserId aUserId);
    CompletableFuture<Void> put(ApplicationId aAppId, UserId aUserId, Presence aPresence);
}
