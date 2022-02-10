package io.fizz.gateway.http.controllers.group;

import io.fizz.chat.group.application.group.RemoveUserGroupsCommand;
import io.fizz.chat.group.application.query.AbstractUserGroupQueryService;
import io.fizz.chat.group.application.query.UserGroupDTO;
import io.fizz.chat.group.domain.group.GroupId;
import io.fizz.chataccess.domain.AbstractAuthorizationService;
import io.fizz.chataccess.domain.role.RoleName;
import io.fizz.chatcommon.infrastructure.WebUtils;
import io.fizz.command.bus.AbstractCommandBus;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.common.domain.UserId;
import io.fizz.gateway.http.annotations.AsyncRestController;
import io.fizz.gateway.http.annotations.AuthScheme;
import io.fizz.gateway.http.controllers.AbstractRestController;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class UserGroupControllerInternal extends AbstractRestController {
    private final AbstractUserGroupQueryService queryService;
    private final AbstractCommandBus commandBus;
    private final AbstractAuthorizationService authorizationService;
    private final RoleName adminRole;

    public UserGroupControllerInternal(final Vertx aVertx,
                                       final AbstractUserGroupQueryService aQueryService,
                                       final AbstractCommandBus aCommandBus,
                                       final AbstractAuthorizationService aAuthService,
                                       final RoleName aAdminRole) {
        super(aVertx);

        Utils.assertRequiredArgument(aQueryService, "invalid query service");
        Utils.assertRequiredArgument(aCommandBus, "invalid command bus");
        Utils.assertRequiredArgument(aAuthService, "invalid auth service specified.");
        Utils.assertRequiredArgument(aAdminRole, "invalid admin role specified.");

        this.queryService = aQueryService;
        this.commandBus = aCommandBus;
        authorizationService = aAuthService;
        adminRole = aAdminRole;
    }

    @AsyncRestController(path="/internal/9e5a3a19-b282-4237-bae0-89c6f835a922/apps/:appId/users/:userId/groups", method= HttpMethod.DELETE, auth=AuthScheme.NONE)
    public CompletableFuture<Void> onDeleteUserGroups(final RoutingContext aContext,
                                                     final HttpServerResponse aResponse) {
        try {
            final ApplicationId appId = new ApplicationId(aContext.pathParam("appId"));
            final UserId userId = new UserId(aContext.pathParam("userId"));
            final UserId requesterId = new UserId(aContext.request().getParam("requesterId"));

            CompletableFuture<Set<String>> queriedGroupIds = new CompletableFuture<>();
            queryAllUserGroups(appId, userId, null, 50, new HashSet<>(), queriedGroupIds);
            return queriedGroupIds.thenCompose(groupIds -> {
                final RemoveUserGroupsCommand cmd = new RemoveUserGroupsCommand(appId, requesterId, userId);
                for (String groupId : groupIds) {
                    cmd.add(new GroupId(appId, groupId));
                }

                return applyAdminRole(appId, requesterId)
                        .thenCompose(v -> commandBus.<Boolean>execute(cmd)
                        .thenApply(aRemoved -> {
                            if (aRemoved) {
                                WebUtils.doOK(aResponse, "");
                            } else {
                                throw new IllegalStateException("stale_state");
                            }
                            return null;
                        }));
            });
        } catch (DomainErrorException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    private CompletableFuture<Void> applyAdminRole(final ApplicationId aAppId, final UserId aAdminId) {
        return authorizationService.assignAppRole(aAppId, aAdminId, adminRole, Utils.TIME_END);
    }

    private void queryAllUserGroups(final ApplicationId aAppId,
                                    final UserId aUserId,
                                    final String aPageCursor,
                                    final int aPageSize,
                                    final Set<String> aGroupIds,
                                    CompletableFuture<Set<String>> aResultFuture) {
        queryService.query(aAppId, aUserId, aPageCursor, aPageSize)
                .handle(((result, error) -> {
                    if (Objects.nonNull(error)) {
                        aResultFuture.completeExceptionally(error);
                    }
                    else {
                        for (UserGroupDTO item : result.items()) {
                            aGroupIds.add(item.groupId().value());
                        }
                        if (result.hasNext()) {
                            queryAllUserGroups(aAppId, aUserId, result.nextPageCursor(), aPageSize, aGroupIds, aResultFuture);
                        }
                        else {
                            aResultFuture.complete(aGroupIds);
                        }
                    }
                    return null;
                }));
    }
}
