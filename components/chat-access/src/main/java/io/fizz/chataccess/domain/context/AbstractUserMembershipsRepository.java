package io.fizz.chataccess.domain.context;

import io.fizz.common.domain.UserId;

import java.util.concurrent.CompletableFuture;

public interface AbstractUserMembershipsRepository {
    CompletableFuture<UserMemberships> fetch(final AuthContextId aContextId, final UserId aUserId);
    CompletableFuture<Boolean> save(final UserMemberships aMemberships);
}
