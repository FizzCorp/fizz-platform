package io.fizz.chat.user.application.service;

import io.fizz.chat.user.application.UserDTO;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;

import java.util.concurrent.CompletableFuture;

public interface AbstractUserApplicationService {
    CompletableFuture<UserDTO> fetchUser(final ApplicationId aAppId, final UserId aUserId);
}
