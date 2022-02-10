package io.fizz.chat.presence.infrastructure.presence;

import io.fizz.chat.presence.AbstractUserPresenceService;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class MockUserPresenceService implements AbstractUserPresenceService {

    private static final UserId USER_A = new UserId("userA");
    private static final UserId USER_OFFLINE = new UserId("user_offline");

    @Override
    public CompletableFuture<Boolean> get(ApplicationId aAppId, UserId aUserId) {
        boolean isOnline = true;
        if (aUserId.equals(USER_OFFLINE)) {
            isOnline = false;
        }
        return CompletableFuture.completedFuture(isOnline);
    }

    @Override
    public CompletableFuture<Set<UserId>> getOfflineUsers(final ApplicationId aAppId, final Set<UserId> aUserIds) {
        final Set<UserId> onlineUsers = new HashSet<>();
        if (aUserIds.contains(USER_OFFLINE)) {
            onlineUsers.add(USER_OFFLINE);
        }
        return CompletableFuture.completedFuture(onlineUsers);
    }
}
