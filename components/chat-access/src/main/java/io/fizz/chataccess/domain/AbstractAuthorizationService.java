package io.fizz.chataccess.domain;

import io.fizz.chataccess.domain.context.AuthContextId;
import io.fizz.chataccess.domain.role.Role;
import io.fizz.chataccess.domain.role.RoleName;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;

import java.util.Date;
import java.util.concurrent.CompletableFuture;

public interface AbstractAuthorizationService {
    CompletableFuture<Authorization> buildAuthorization(final AuthContextId aContextId,
                                                        final UserId aOwnerId,
                                                        final UserId aOperatorId);

    CompletableFuture<Void> assertOperateOwned(final AuthContextId authContextId,
                                               final UserId aOwnerId,
                                               final UserId aOperatorId,
                                               final String aPermission);

    CompletableFuture<Void> assertOperateNotOwned(final AuthContextId authContextId,
                                                  final UserId aOwnerId,
                                                  final UserId aOperatorId,
                                                  final String aPermission);


    CompletableFuture<Void> assignAppRole(final ApplicationId aAppId,
                                          final UserId aUserId,
                                          final RoleName aRoleName,
                                          final Date aEnds);
    CompletableFuture<Void> removeAppRole(final ApplicationId aAppId, final UserId aUserId, final RoleName aRoleName);
    CompletableFuture<Role> fetchAppRole(final ApplicationId aAppId, final UserId aUserId);

    CompletableFuture<Void> assignContextRole(final AuthContextId aContextId,
                                              final UserId aUserId,
                                              final RoleName aRoleName,
                                              final Date aEnds);
    CompletableFuture<Void> removeContextRole(final AuthContextId aContextId,
                                              final UserId aUserId,
                                              final RoleName aRoleName);
    CompletableFuture<Role> fetchContextRole(final AuthContextId aContextId, final UserId aUserId);
}
