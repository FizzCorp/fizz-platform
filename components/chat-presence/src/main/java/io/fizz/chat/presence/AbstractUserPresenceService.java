package io.fizz.chat.presence;

import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface AbstractUserPresenceService {
    CompletableFuture<Boolean> get(final ApplicationId aAppId, final UserId aUserId);
    CompletableFuture<Set<UserId>> getOfflineUsers(final ApplicationId aAppId, final Set<UserId> aUserIds);
}
