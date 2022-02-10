package io.fizz.chataccess.domain;

import io.fizz.chataccess.domain.context.AuthContextId;
import io.fizz.chataccess.domain.role.Role;
import io.fizz.chataccess.domain.role.RoleName;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;

import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class MockAuthorizationService implements AbstractAuthorizationService {
    private static class MockAuthorization extends Authorization {
        MockAuthorization(UserId aOwnerId,
                                 UserId aOperatorId) {
            super(
                    aOwnerId,
                    new Role(new RoleName("member"), 1),
                    aOperatorId,
                    new Role(new RoleName("member"), 1)
            );
        }

        @Override
        public Authorization assertOperateOwned(String aPermission) {
            return this;
        }

        @Override
        public Authorization assertOperateNotOwned(String aPermission) {
            return this;
        }

        @Override
        public void validate() {
            super.validate();
        }
    }

    @Override
    public CompletableFuture<Authorization> buildAuthorization(AuthContextId aContextId,
                                                               UserId aOwnerId,
                                                               UserId aOperatorId) {
        return CompletableFuture.completedFuture(new MockAuthorization(aOwnerId, aOperatorId));
    }

    @Override
    public CompletableFuture<Void> assertOperateOwned(AuthContextId authContextId, UserId aOwnerId, UserId aOperatorId, String aPermission) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> assertOperateNotOwned(AuthContextId authContextId, UserId aOwnerId, UserId aOperatorId, String aPermission) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> assignAppRole(ApplicationId aAppId, UserId aUserId, RoleName aRoleName, Date aEnds) {
        return null;
    }

    @Override
    public CompletableFuture<Void> removeAppRole(ApplicationId aAppId, UserId aUserId, RoleName aRoleName) {
        return null;
    }

    @Override
    public CompletableFuture<Role> fetchAppRole(ApplicationId aAppId, UserId aUserId) {
        return null;
    }

    @Override
    public CompletableFuture<Void> assignContextRole(AuthContextId aContextId, UserId aUserId, RoleName aRoleName, Date aEnds) {
        return null;
    }

    @Override
    public CompletableFuture<Void> removeContextRole(AuthContextId aContextId, UserId aUserId, RoleName aRoleName) {
        return null;
    }

    @Override
    public CompletableFuture<Role> fetchContextRole(AuthContextId aContextId, UserId aUserId) {
        return null;
    }
}
