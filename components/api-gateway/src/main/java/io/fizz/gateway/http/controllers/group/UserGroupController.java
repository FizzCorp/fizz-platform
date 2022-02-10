package io.fizz.gateway.http.controllers.group;

import io.fizz.chat.group.application.group.RemoveGroupMembersCommand;
import io.fizz.chat.group.application.group.UpdateGroupMemberCommand;
import io.fizz.chat.group.application.query.AbstractUserGroupQueryService;
import io.fizz.chat.group.application.query.QueryResult;
import io.fizz.chat.group.application.query.UserGroupDTO;
import io.fizz.chat.group.domain.group.GroupId;
import io.fizz.chat.group.domain.group.GroupMember;
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
import io.fizz.gateway.http.controllers.chat.SessionContext;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class UserGroupController extends AbstractRestController {
    private final AbstractUserGroupQueryService queryService;
    private final AbstractCommandBus commandBus;
    private final AbstractAuthorizationService authorizationService;
    private final RoleName adminRole;

    public UserGroupController(final Vertx aVertx,
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

    @AsyncRestController(path="/users/:userId/groups", method= HttpMethod.GET, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onFetchUserGroups(final RoutingContext aContext,
                                                     final HttpServerResponse aResponse) {
        final SessionContext sc = new SessionContext(aContext.session());
        final UserId userId = userId(aContext);
        final String pageLink = aContext.request().getParam("page");
        final int pageSize = parsePageSize(aContext.request().getParam("page_size"));

        CompletableFuture<QueryResult<UserGroupDTO>> queried = queryService.query(
                appId(sc.appId()), userId, pageLink, pageSize
        );

        return queried.thenApply(aQueryResult -> {
            final JsonObject body = new JsonObject();

            final JsonObject links = new JsonObject();
            if (aQueryResult.hasNext()) {
                links.put("next", aQueryResult.nextPageCursor());
            }
            body.put("links", links);

            final JsonArray groups = new JsonArray();
            for (UserGroupDTO item: aQueryResult.items()) {
                groups.add(serialize(item));
            }
            body.put("groups", groups);

            WebUtils.doOK(aResponse, body);

            return null;
        });
    }

    @AsyncRestController(path="/users/:userId/groups/:groupId", method= HttpMethod.POST, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onUpdateGroup(final RoutingContext aContext, final HttpServerResponse aResponse) {
        final SessionContext sc = new SessionContext(aContext.session());
        final JsonObject body = aContext.getBodyAsJson();
        final UpdateGroupMemberCommand cmd = new UpdateGroupMemberCommand(
                sc.appId(),
                sc.userId(),
                aContext.pathParam("groupId"),
                aContext.pathParam("userId")
        );

        if (Objects.isNull(body) || body.isEmpty()) {
            cmd.setNewState(GroupMember.State.JOINED);
        }
        else {
            if (body.containsKey("state")) {
                cmd.setNewState(GroupMember.State.fromValue(body.getString("state")));
            }
            if (body.containsKey("last_read_message_id")) {
                cmd.setLastReadMessageId(body.getLong("last_read_message_id"));
            }
        }

        return commandBus.<Boolean>execute(cmd)
                .thenApply(aUpdated -> {
                    if (aUpdated) {
                        WebUtils.doOK(aResponse, "");
                    }
                    else {
                        throw new IllegalStateException("stale_state");
                    }
                    return null;
                });
    }

    @AsyncRestController(path="/users/:userId/groups/:groupId", method= HttpMethod.DELETE, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onLeaveGroup(final RoutingContext aContext, final HttpServerResponse aResponse) {
        final SessionContext sc = new SessionContext(aContext.session());
        final UserId userId = userId(aContext);
        final GroupId groupId = new GroupId(appId(sc.appId()), aContext.pathParam("groupId"));
        final RemoveGroupMembersCommand cmd = new RemoveGroupMembersCommand(userId, groupId)
                                                    .add(userId);

        return commandBus.<Boolean>execute(cmd)
                .thenApply(aRemoved -> {
                    if (aRemoved) {
                        WebUtils.doOK(aResponse, "");
                    }
                    else {
                        throw new IllegalStateException("stale_state");
                    }
                    return null;
                });
    }

    private JsonObject serialize(final UserGroupDTO aItem) {
        final JsonObject json = new JsonObject();

        json.put("group_id", aItem.groupId().value());
        json.put("state", aItem.state().value());
        json.put("role", aItem.role().value());
        json.put("created", aItem.createdOn().getTime());
        if (Objects.nonNull(aItem.lastReadMessageId())) {
            json.put("last_read_message_id", aItem.lastReadMessageId());
        }

        return json;
    }

    private ApplicationId appId(String aValue) {
        try {
            return new ApplicationId(aValue);
        }
        catch (Exception ex ){
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    private UserId userId(final RoutingContext aContext) {
        try {
            final SessionContext sc = new SessionContext(aContext.session());
            String pathUserId = aContext.pathParam("userId");
            if (Objects.isNull(pathUserId) || !pathUserId.equals(sc.userId())) {
                throw new DomainErrorException("invalid userId");
            }
            return new UserId(sc.userId());
        }
        catch (DomainErrorException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    private int parsePageSize(final String aValue) {
        return Objects.isNull(aValue) ? 50 : Integer.parseInt(aValue);
    }
}
