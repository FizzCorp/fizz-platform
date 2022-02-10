package io.fizz.chataccess.application;

import io.fizz.chataccess.domain.AbstractAuthorizationService;
import io.fizz.chataccess.domain.Authorization;
import io.fizz.chataccess.domain.DomainServicesRegistry;
import io.fizz.chataccess.domain.context.*;
import io.fizz.chataccess.domain.role.*;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class AuthorizationService implements AbstractAuthorizationService {
    private final DomainServicesRegistry servicesRegistry;
    private final Role appDefaultRole;
    private final Role contextDefaultRole;

    public AuthorizationService(final AbstractUserMembershipsRepository aUserMembershipsRepo,
                                final AbstractRoleRepository aRoleRepo,
                                final Role aAppDefaultRole,
                                final Role aContextDefaultRole) {
        Utils.assertRequiredArgument(aAppDefaultRole, "invalid app default role specified");
        Utils.assertRequiredArgument(aContextDefaultRole, "invalid context default role specified.");

        servicesRegistry = new DomainServicesRegistry(aUserMembershipsRepo, aRoleRepo);
        appDefaultRole = aAppDefaultRole;
        contextDefaultRole = aContextDefaultRole;
    }

    @Override
    public CompletableFuture<Authorization> buildAuthorization(final AuthContextId aContextId,
                                                               final UserId aOwnerId,
                                                               final UserId aOperatorId) {

        final CompletableFuture<Role> ownerRoleFetched = fetchContextRole(aContextId, aOwnerId);
        final CompletableFuture<Role> operatorRoleFetched = fetchContextRole(aContextId, aOperatorId);

        return ownerRoleFetched.thenCombine(
                operatorRoleFetched,
                (aOwnerRole, aOperatorRole) -> new Authorization(aOwnerId, aOwnerRole, aOperatorId, aOperatorRole)
        );
    }

    @Override
    public CompletableFuture<Void> assertOperateOwned(final AuthContextId authContextId,
                                                      final UserId aOwnerId,
                                                      final UserId aOperatorId,
                                                      final String aPermission) {
        return buildAuthorization(authContextId, aOwnerId, aOperatorId)
                .thenCompose(aAuth -> {
                    aAuth.assertOperateOwned(aPermission).validate();
                    return CompletableFuture.completedFuture(null);
                });
    }

    @Override
    public CompletableFuture<Void> assertOperateNotOwned(final AuthContextId authContextId,
                                                         final UserId aOwnerId,
                                                         final UserId aOperatorId,
                                                         final String aPermission) {
        return buildAuthorization(authContextId, aOwnerId, aOperatorId)
                .thenCompose(aAuth -> {
                    aAuth.assertOperateNotOwned(aPermission).validate();
                    return CompletableFuture.completedFuture(null);
                });
    }

    @Override
    public CompletableFuture<Void> assignAppRole(final ApplicationId aAppId,
                                                 final UserId aUserId,
                                                 final RoleName aRoleName,
                                                 final Date aEnds) {
        return assignContextRole(new AuthContextId(aAppId), aUserId, aRoleName, aEnds);
    }

    @Override
    public CompletableFuture<Void> removeAppRole(final ApplicationId aAppId,
                                                 final UserId aUserId,
                                                 final RoleName aRoleName) {
        return removeContextRole(new AuthContextId(aAppId), aUserId, aRoleName);
    }

    @Override
    public CompletableFuture<Role> fetchAppRole(final ApplicationId aAppId, final UserId aUserId) {
        return fetchRole(new AuthContextId(aAppId), aUserId, appDefaultRole);
    }

    @Override
    public CompletableFuture<Void> assignContextRole(final AuthContextId aContextId,
                                                     final UserId aUserId,
                                                     final RoleName aRoleName,
                                                     final Date aEnds) {
        return validateRole(aRoleName)
                .thenCompose(
                    aVoid ->
                        fetchMemberships(aContextId, aUserId)
                            .thenCompose(
                                aMemberships -> {
                                    aMemberships.add(aRoleName, aEnds);

                                    return saveMemberships(aMemberships);
                                }
                            )
                );
    }

    @Override
    public CompletableFuture<Void> removeContextRole(final AuthContextId aContextId,
                                                     final UserId aUserId,
                                                     final RoleName aRoleName) {
        return validateRole(aRoleName)
                .thenCompose(
                    aVoid ->
                        fetchMemberships(aContextId, aUserId)
                            .thenCompose(aMemberships -> {
                                aMemberships.remove(aRoleName);

                                return saveMemberships(aMemberships);
                            })
                );
    }

    private CompletableFuture<Void> validateRole(final RoleName aRoleName) {
        return servicesRegistry.roleRepo().fetch(aRoleName)
                .thenApply(aRole -> {
                    Utils.assertRequiredArgument(aRole, "invalid_role");

                    return null;
                });
    }

    @Override
    public CompletableFuture<Role> fetchContextRole(final AuthContextId aContextId, final UserId aUserId) {
        return fetchRole(aContextId, aUserId, contextDefaultRole);
    }

    private CompletableFuture<Role> fetchRole(final AuthContextId aContextId,
                                              final UserId aUserId,
                                              final Role aDefaultRole) {
        return role(aContextId, aUserId)
                .thenApply(aRole -> Objects.nonNull(aRole) ? aRole : aDefaultRole);
    }

    private CompletableFuture<Role> role(final AuthContextId aContextId, final UserId aUserId) {
        if (aContextId.isChild()) {
            return childContextRole(aContextId, aUserId);
        }
        else {
            return appRole(aContextId, aUserId);
        }
    }

    private CompletableFuture<Role> appRole(final AuthContextId aContextId, final UserId aUserId) {
        return servicesRegistry.userMembershipsRepo().fetch(new AuthContextId(aContextId.appId()), aUserId)
                .thenCompose(this::membershipsToRole);
    }

    private CompletableFuture<Role> childContextRole(final AuthContextId aContextId, final UserId aUserId) {
        final CompletableFuture<Role> userRoleFetched = appRole(aContextId, aUserId);

        final CompletableFuture<Role> memberRoleFetched = servicesRegistry.userMembershipsRepo().fetch(aContextId, aUserId)
                .thenCompose(this::membershipsToRole);

        return userRoleFetched.thenCombine(
                memberRoleFetched,
                (aUserRole, aMemberRole) -> Role.compose(new Role[]{aUserRole, aMemberRole})
        );
    }

    private CompletableFuture<Role> membershipsToRole(final UserMemberships aMemberships) {
        if (Objects.isNull(aMemberships)) {
            return CompletableFuture.completedFuture(null);
        }

        return aMemberships.appliedRole(servicesRegistry);
    }

    private CompletableFuture<Void> saveMemberships(final UserMemberships aMemberships) {
        return servicesRegistry.userMembershipsRepo().save(aMemberships)
                .thenApply(aSaved -> {
                    if (!aSaved) {
                        throw new IllegalStateException("concurrent_access");
                    }

                    return null;
                });
    }

    private CompletableFuture<UserMemberships> fetchMemberships(final AuthContextId aContextId, final UserId aUserId) {
        return servicesRegistry.userMembershipsRepo().fetch(aContextId, aUserId)
                .thenApply(aMemberhips -> {
                    UserMemberships memberships;

                    if (Objects.isNull(aMemberhips)) {
                        memberships = new UserMemberships(aContextId, aUserId);
                    }
                    else {
                        memberships = aMemberhips;
                    }

                    return memberships;
                });
    }
}
