package io.fizz.chat.group.domain.group;

import io.fizz.common.domain.ApplicationId;

import java.util.concurrent.CompletableFuture;

public interface AbstractGroupRepository {
    GroupId identity(final ApplicationId aAppId);
    CompletableFuture<Boolean> put(Group aGroup);
    CompletableFuture<Group> get(GroupId aGroupId);
}
