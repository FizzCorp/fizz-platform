package io.fizz.gateway.http.controllers.group;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.fizz.chat.domain.channel.ChannelMessage;
import io.fizz.chat.group.application.group.*;
import io.fizz.chat.group.domain.group.GroupId;
import io.fizz.chat.group.domain.group.GroupMember;
import io.fizz.chataccess.domain.role.RoleName;
import io.fizz.chatcommon.infrastructure.WebUtils;
import io.fizz.command.bus.AbstractCommandBus;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;
import io.fizz.gateway.http.annotations.AsyncRestController;
import io.fizz.gateway.http.annotations.AuthScheme;
import io.fizz.gateway.http.controllers.AbstractRestController;
import io.fizz.gateway.http.controllers.chat.MessageSerializer;
import io.fizz.gateway.http.controllers.chat.SessionContext;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class GroupController extends AbstractRestController {
    private final GroupApplicationService groupService;
    private final AbstractCommandBus commandBus;

    public GroupController(final Vertx aVertx,
                           final GroupApplicationService aGroupService,
                           final AbstractCommandBus aCommandBus) {
        super(aVertx);

        Utils.assertRequiredArgument(aGroupService, "invalid group service");
        Utils.assertRequiredArgument(aCommandBus, "invalid command bus");

        this.groupService = aGroupService;
        this.commandBus = aCommandBus;
    }

    @AsyncRestController(path="/groups", method= HttpMethod.POST, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onCreateGroup(final RoutingContext aContext, final HttpServerResponse aResponse) {
        final JsonObject body = aContext.getBodyAsJson();
        final SessionContext sc = new SessionContext(aContext.session());
        final GroupId groupId = groupService.groupRepo().identity(appId(sc.appId()));
        final String title = body.getString("title");
        final String imageURL = body.getString("image_url");
        final String description = body.getString("description");
        final String type = body.getString("type");

        final CreateGroupCommand cmd = new CreateGroupCommand(
                groupId, sc.userId(), title, imageURL, description, type
        );

        final JsonArray members = body.getJsonArray("members");
        if (Objects.nonNull(members)) {
            for (int ei = 0; ei < members.size(); ei++) {
                final JsonObject member = members.getJsonObject(ei);
                final UserId memberId = new UserId(member.getString("id"));
                final RoleName role = new RoleName(member.getString("role"));
                final GroupMember.State state = GroupMember.State.fromValue(member.getString("state"));

                cmd.add(memberId, role, state);
            }
        }

        return commandBus.<GroupDTO>execute(cmd)
                .thenApply(aGroup -> {
                    emit(aGroup, aResponse);
                    return null;
                });
    }

    @AsyncRestController(path="/groups/:groupId", method= HttpMethod.POST, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onUpdateGroup(final RoutingContext aContext,
                                                 final HttpServerResponse aResponse) {
        final SessionContext sc = new SessionContext(aContext.session());
        final JsonObject body = aContext.getBodyAsJson();
        final String groupId = aContext.pathParam("groupId");
        final String newTitle = body.getString("title");
        final String newImageURL = body.getString("image_url");
        final String newDescription = body.getString("description");
        final String newType = body.getString("type");

        final UpdateGroupProfileCommand cmd = new UpdateGroupProfileCommand(
                sc.userId(),
                sc.appId(),
                groupId,
                newTitle,
                newImageURL,
                newDescription,
                newType
        );

        return commandBus.<Boolean>execute(cmd)
                .thenApply(aUpdated -> {
                    if (!aUpdated) {
                        throw new IllegalStateException("stale_state");
                    }
                    else {
                        WebUtils.doOK(aResponse, "");
                    }
                    return null;
                });
    }

    @AsyncRestController(path="/groups/:groupId", method= HttpMethod.GET, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onFetchGroup(final RoutingContext aContext, final HttpServerResponse aResponse) {
        final SessionContext sc = new SessionContext(aContext.session());
        final String groupId = aContext.pathParam("groupId");

        return groupService.fetch(new GroupId(appId(sc.appId()), groupId))
                .thenApply(aGroup -> {
                    if (Objects.isNull(aGroup)) {
                        throw new IllegalStateException("missing_group");
                    }

                    emit(aGroup, aResponse);

                    return null;
                });
    }

    @AsyncRestController(path="/groups/:groupId/members", method= HttpMethod.POST, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onAddMembers(final RoutingContext aContext, final HttpServerResponse aResponse) {
        final SessionContext sc = new SessionContext(aContext.session());
        final String groupId = aContext.pathParam("groupId");
        final AddGroupMembersCommand cmd = new AddGroupMembersCommand(sc.userId(), sc.appId(), groupId);
        final JsonArray members = aContext.getBodyAsJsonArray();

        Utils.assertRequiredArgument(members, "invalid_members");

        for (int ei = 0; ei < members.size(); ei++) {
            final JsonObject member = members.getJsonObject(ei);
            final UserId memberId = new UserId(member.getString("id"));
            final RoleName role = new RoleName(member.getString("role"));
            final GroupMember.State state = GroupMember.State.fromValue(member.getString("state"));

            cmd.add(memberId, role, state);
        }

        return commandBus.<Boolean>execute(cmd)
                .thenApply(aAdded -> {
                    if (!aAdded) {
                        throw  new IllegalStateException("stale_state");
                    }
                    else {
                        WebUtils.doOK(aResponse, "");
                    }
                    return null;
                });
    }

    @AsyncRestController(path="/groups/:groupId/members", method= HttpMethod.GET, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onFetchMembers(final RoutingContext aContext, final HttpServerResponse aResponse) {
        final SessionContext sc = new SessionContext(aContext.session());
        final String groupId = aContext.pathParam("groupId");

        return groupService.fetch(new GroupId(appId(sc.appId()), groupId))
                .thenApply(aGroup -> {
                    if (Objects.isNull(aGroup)) {
                        throw new IllegalStateException("missing_group");
                    }

                    final JsonArray members = new JsonArray();
                    for (GroupMemberDTO member: aGroup.members()) {
                        JsonObject body = new JsonObject();
                        body.put("id", member.userId());
                        body.put("role", member.role());
                        body.put("state", member.state());
                        body.put("created", member.createdOn().getTime());
                        members.add(body);
                    }
                    WebUtils.doOK(aResponse, members);

                    return null;
                });
    }

    @AsyncRestController(path="/groups/:groupId/members/:memberId", method= HttpMethod.POST, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onUpdateMember(final RoutingContext aContext, final HttpServerResponse aResponse) {
        final SessionContext sc = new SessionContext(aContext.session());
        final JsonObject body = aContext.getBodyAsJson();
        final UpdateGroupMemberCommand cmd = new UpdateGroupMemberCommand(
                sc.appId(),
                sc.userId(),
                aContext.pathParam("groupId"),
                aContext.pathParam("memberId")
        );

        if (body.containsKey("role")) {
            cmd.setNewRole(body.getString("role"));
        }

        return commandBus.<Boolean>execute(cmd)
                .thenApply(aUpdated -> {
                    if (!aUpdated) {
                        throw new IllegalStateException("stale_state");
                    }
                    else {
                        WebUtils.doOK(aResponse, "");
                    }
                    return null;
                });
    }

    @AsyncRestController(path="/groups/:groupId/members/:memberId", method= HttpMethod.DELETE, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onRemoveMember(final RoutingContext aContext, final HttpServerResponse aResponse) {
        final SessionContext sc = new SessionContext(aContext.session());
        final ApplicationId appId = appId(sc.appId());
        final GroupId groupId = new GroupId(appId, aContext.pathParam("groupId"));
        final UserId operatorId = new UserId(sc.userId());
        final UserId memberId = new UserId(aContext.pathParam("memberId"));
        final RemoveGroupMembersCommand cmd = new RemoveGroupMembersCommand(operatorId, groupId)
                                                        .add(memberId);

        return commandBus.<Boolean>execute(cmd)
                .thenApply(aDeleted -> {
                    if (!aDeleted) {
                        throw new IllegalStateException("stale_state");
                    }
                    else {
                        WebUtils.doOK(aResponse, "");
                    }
                    return null;
                });
    }

    @AsyncRestController(path="/groups/:groupId/messages", method= HttpMethod.POST, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onPublishMessage(final RoutingContext aContext, final HttpServerResponse aResponse) {
        final SessionContext sc = new SessionContext(aContext.session());
        final JsonObject body = aContext.getBodyAsJson();
        final String groupId = aContext.pathParam("groupId");
        final String nick = body.getString("nick");
        final String msgBody = body.getString("body");
        final String data = body.getString("data");
        final String locale = body.getString("locale");
        final Boolean translate = body.getBoolean("translate");
        final Boolean filter = body.getBoolean("filter");
        final Boolean persist = body.getBoolean("persist");

        final PublishGroupMessageCommand cmd = new PublishGroupMessageCommand(
                sc.appId(), groupId, sc.userId(), nick, msgBody, data, locale, translate, filter, persist, false
        );

        return commandBus.<Void>execute(cmd)
                .thenApply(aVoid -> {
                    WebUtils.doOK(aResponse, "");

                    return null;
                });
    }

    @AsyncRestController(path="/groups/:groupId/messages", method= HttpMethod.GET, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onQueryMessages(final RoutingContext aContext, final HttpServerResponse aResponse) {
        final SessionContext sc = new SessionContext(aContext.session());
        final String groupId = aContext.pathParam("groupId");
        final HttpServerRequest req = aContext.request();
        final int messageCount = Integer.parseInt(req.getParam("count"));
        final Long beforeId = parseLong(req, "before_id");
        final Long afterId = parseLong(req, "after_id");
        final GroupMessagesQuery query = new GroupMessagesQuery(
                sc.appId(), groupId, sc.userId(), messageCount, beforeId, afterId
        );

        return groupService.queryMessages(query)
                .thenApply(aMessages -> {
                    emit(aMessages, aResponse);

                    return null;
                });
    }

    @AsyncRestController(path="/groups/:groupId/mutes", method=HttpMethod.POST, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onMuteUser(final RoutingContext aContext, final HttpServerResponse aResponse) {
        final SessionContext sc = new SessionContext(aContext.session());
        final ApplicationId appId = appId(sc.appId());
        final GroupId groupId = new GroupId(appId, aContext.pathParam("groupId"));
        final UserId operatorId = new UserId(sc.userId());
        final JsonObject body = aContext.getBodyAsJson();
        final UserId userToMuteId = new UserId(body.getString("id"));
        final Date ends = parseRoleEnd(body);

        return groupService.muteUser(groupId, operatorId, userToMuteId, ends)
                .thenApply(v -> {
                    WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, "{}");
                    return null;
                });
    }

    @AsyncRestController(path="/groups/:groupId/mutes/:memberId", method=HttpMethod.DELETE, auth=AuthScheme.SESSION_TOKEN)
    public CompletableFuture<Void> onUnMuteUser(final RoutingContext aContext,
                                                        final HttpServerResponse aResponse) {
        final SessionContext sc = new SessionContext(aContext.session());
        final ApplicationId appId = appId(sc.appId());
        final GroupId groupId = new GroupId(appId, aContext.pathParam("groupId"));
        final UserId operatorId = new UserId(sc.userId());
        final UserId userToUnMuteId = new UserId(aContext.pathParam("memberId"));

        return groupService.unmuteUser(groupId, operatorId, userToUnMuteId)
                .thenApply(v -> {
                    WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, "{}");
                    return null;
                });
    }

    private Long parseLong(final HttpServerRequest aRequest, final String aKey) {
        final String param = aRequest.getParam(aKey);

        return Objects.isNull(param) ? null : Long.parseLong(param);
    }

    private void emit(List<ChannelMessage> aMessages, final HttpServerResponse aResponse) {
        final Gson serde = new GsonBuilder()
                .registerTypeAdapter(ChannelMessage.class, new MessageSerializer())
                .create();
        final StringBuilder builder = new StringBuilder();

        builder.append("[");
        for (int mi = 0; mi < aMessages.size(); mi++) {
            if (mi > 0) {
                builder.append(",");
            }
            final ChannelMessage message = aMessages.get(mi);
            builder.append(serde.toJson(message));
        }
        builder.append("]");

        WebUtils.doOK(aResponse, WebUtils.CONTENT_JSON, builder.toString());
    }

    private void emit(final GroupDTO aGroup, final HttpServerResponse aResponse) {
        final JsonObject body = new JsonObject();

        body.put("id", aGroup.id());
        body.put("created_by", aGroup.createdBy());
        body.put("title", aGroup.title());
        body.put("image_url", aGroup.imageURL());
        body.put("description", aGroup.description());
        body.put("type", aGroup.type());
        body.put("channel_id", aGroup.channel());
        body.put("created", aGroup.createdOn().getTime());
        body.put("updated", aGroup.updatedOn().getTime());

        WebUtils.doOK(aResponse, body);
    }

    private ApplicationId appId(String aAppId) {
        try {
            return new ApplicationId(aAppId);
        }
        catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    private Date parseRoleEnd(final JsonObject aBody) {
        if (aBody.containsKey("duration")) {
            return new Date(Utils.now() + aBody.getLong("duration")*1000L);
        }
        else {
            return Utils.TIME_END;
        }
    }
}
