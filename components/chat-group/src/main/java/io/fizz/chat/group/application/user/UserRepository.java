package io.fizz.chat.group.application.user;

import io.fizz.chat.group.domain.group.GroupId;
import io.fizz.chat.group.domain.user.User;
import io.fizz.chataccess.domain.AbstractAuthorizationService;
import io.fizz.chataccess.domain.role.Role;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;

import java.util.concurrent.CompletableFuture;

public class UserRepository {
    private final AbstractAuthorizationService authService;

    public UserRepository(final AbstractAuthorizationService aAuthService) {
        Utils.assertRequiredArgument(aAuthService, "invalid auth service");

        this.authService = aAuthService;
    }

    public CompletableFuture<User> get(final ApplicationId aAppId, final UserId aUserId) {
        final CompletableFuture<Role> authFetched = authService.fetchAppRole(aAppId, aUserId);

        return authFetched.thenApply(aRole -> new User(aUserId, aRole));
    }

    public CompletableFuture<User> get(final UserId aUserId, final GroupId aGroupId) {
        final CompletableFuture<Role> authFetched = authService.fetchContextRole(aGroupId.authContextId(), aUserId);

        return authFetched.thenApply(aRole -> new User(aUserId, aRole));
    }
}
