package io.fizz.chat.group.application.group;

import io.fizz.chat.group.domain.group.GroupId;
import io.fizz.command.bus.AbstractCommand;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class RemoveUserGroupsCommand implements AbstractCommand {
    private final ApplicationId appId;
    private final UserId operatorId;
    private final UserId userId;
    private final Set<GroupId> groupIds = new HashSet<>();

    public RemoveUserGroupsCommand(final ApplicationId aAppId, final UserId aOperatorId, final UserId aUserId) {
        Utils.assertRequiredArgument(aAppId, "invalid_application_id");
        Utils.assertRequiredArgument(aOperatorId, "invalid_operator_id");
        Utils.assertRequiredArgument(aUserId, "invalid_user_id");

        this.appId = aAppId;
        this.operatorId = aOperatorId;
        this.userId = aUserId;
    }

    @Override
    public byte[] key() {
        return userId.qualifiedValue(appId).getBytes(StandardCharsets.UTF_8);
    }

    public ApplicationId appId() {
        return appId;
    }

    public UserId operatorId() {
        return operatorId;
    }

    public UserId userId() {
        return userId;
    }

    public Set<GroupId> groupIds() {
        return groupIds;
    }

    public RemoveUserGroupsCommand add(final GroupId aGroupId) {
        Utils.assertRequiredArgument(aGroupId, "invalid_group_id");

        groupIds.add(aGroupId);

        return this;
    }
}