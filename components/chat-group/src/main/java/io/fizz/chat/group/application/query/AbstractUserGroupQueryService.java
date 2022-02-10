package io.fizz.chat.group.application.query;

import io.fizz.chat.group.domain.group.GroupId;
import io.fizz.chat.group.domain.group.GroupMember;
import io.fizz.chataccess.domain.role.RoleName;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;

import java.util.Date;
import java.util.concurrent.CompletableFuture;

public interface AbstractUserGroupQueryService {
    CompletableFuture<Void> put(final UserId aUserId,
                                final GroupId aGroupId,
                                final GroupMember.State aState,
                                final RoleName aRole,
                                final Date aCreatedOn);

    CompletableFuture<Void> update(final UserId aUserId, final GroupId aGroupId, final Long aLastReadMessageId);

    CompletableFuture<Void> delete(final UserId aUserId, final GroupId aGroupId);

    CompletableFuture<QueryResult<UserGroupDTO>> query(final ApplicationId aAppId,
                                                       final UserId aUserId,
                                                       final String aPageCursor,
                                                       final int aPageSize);
}
