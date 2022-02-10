package io.fizz.chat.application;

import io.fizz.chat.domain.Permissions;
import io.fizz.chat.domain.channel.AbstractChannelAuthorizationService;
import io.fizz.chataccess.domain.AbstractAuthorizationService;
import io.fizz.chataccess.domain.Authorization;
import io.fizz.chataccess.domain.context.AuthContextId;
import io.fizz.chataccess.domain.role.RoleName;
import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.common.Utils;
import io.fizz.common.domain.UserId;

import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class ChannelAuthorizationService implements AbstractChannelAuthorizationService {
    private final AbstractAuthorizationService authService;
    private final RoleName muteRole;
    private final RoleName banRole;

    public ChannelAuthorizationService(final AbstractAuthorizationService aAuthService,
                                       final RoleName aMuteRole,
                                       final RoleName aBanRole) {
        Utils.assertRequiredArgument(aAuthService, "invalid auth service specified");
        Utils.assertRequiredArgument(aMuteRole, "invalid mute role specified");
        Utils.assertRequiredArgument(aBanRole, "invalid ban role specified");

        authService = aAuthService;
        muteRole = aMuteRole;
        banRole = aBanRole;
    }

    @Override
    public CompletableFuture<Void> muteUser(final ChannelId aChannelId, final UserId aUserId, final Date aEnds) {
        final AuthContextId contextId = toContextId(aChannelId);
        return authService.assignContextRole(contextId, aUserId, muteRole, aEnds);
    }

    @Override
    public CompletableFuture<Void> unmuteUser(final ChannelId aChannelId, final UserId aUserId) {
        final AuthContextId contextId = toContextId(aChannelId);
        return authService.removeContextRole(contextId, aUserId, muteRole);
    }

    @Override
    public CompletableFuture<Void> addUserBan(final ChannelId aChannelId, final UserId aUserId, final Date aEnds) {
        final AuthContextId contextId = toContextId(aChannelId);
        return authService.assignContextRole(contextId, aUserId, banRole, aEnds);
    }

    @Override
    public CompletableFuture<Void> removeUserBan(ChannelId aChannelId, UserId aUserId) {
        final AuthContextId contextId = toContextId(aChannelId);
        return authService.removeContextRole(contextId, aUserId, banRole);
    }

    @Override
    public CompletableFuture<Void> assertOperateOwned(final ChannelId aChannelId,
                                                      final UserId aOwnerId,
                                                      final UserId aOperatorId,
                                                      final Permissions aPermission) {
        return authService.assertOperateOwned(toContextId(aChannelId), aOwnerId, aOperatorId, aPermission.value());
    }

    @Override
    public CompletableFuture<Void> assertOperateNotOwned(final ChannelId aChannelId,
                                                         final UserId aOwnerId,
                                                         final UserId aOperatorId,
                                                         final Permissions aPermission) {
        return authService.assertOperateNotOwned(toContextId(aChannelId), aOwnerId, aOperatorId, aPermission.value());
    }

    @Override
    public CompletableFuture<Authorization> buildAuthorization(final ChannelId aChannelId,
                                                               final UserId aOwnerId,
                                                               final UserId aOperatorId) {
        return authService.buildAuthorization(toContextId(aChannelId), aOwnerId, aOperatorId);
    }

    private AuthContextId toContextId(final ChannelId aChannelId) {
        return new AuthContextId(aChannelId.appId(), aChannelId.value());
    }
}
