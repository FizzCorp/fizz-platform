package io.fizz.chat.user.application.repository;

import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;
import io.fizz.chat.user.domain.User;

import java.util.concurrent.CompletableFuture;

public interface AbstractUserRepository {
    CompletableFuture<User> get(final ApplicationId aAppId, final UserId aActorId);
    CompletableFuture<Boolean> put(final User aProfile);
}
