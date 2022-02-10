package io.fizz.chat.domain.channel;

import io.fizz.chat.domain.Permissions;
import io.fizz.chataccess.domain.Authorization;
import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.common.domain.UserId;

import java.util.Date;
import java.util.concurrent.CompletableFuture;

public interface AbstractChannelAuthorizationService {
    CompletableFuture<Void> muteUser(final ChannelId aChannelId, final UserId aUserId, final Date aEnds);
    CompletableFuture<Void> unmuteUser(final ChannelId aChannelId, final UserId aUserId);
    CompletableFuture<Void> addUserBan(final ChannelId aChannelId, final UserId aUserId, final Date aEnds);
    CompletableFuture<Void> removeUserBan(final ChannelId aChannelId, final UserId aUserId);

    CompletableFuture<Void> assertOperateOwned(final ChannelId aChannelId,
                                               final UserId aOwnerId,
                                               final UserId aOperatorId,
                                               final Permissions aPermission);

    CompletableFuture<Void> assertOperateNotOwned(final ChannelId aChannelId,
                                                  final UserId aOwnerId,
                                                  final UserId aOperatorId,
                                                  final Permissions aPermission);

    CompletableFuture<Authorization> buildAuthorization(final ChannelId aChannelId,
                                                        final UserId aOwnerId,
                                                        final UserId aOperatorId);
}
