package io.fizz.chat.group.application.group;

import io.fizz.chat.group.domain.group.GroupId;
import io.fizz.command.bus.AbstractCommand;
import io.fizz.common.Utils;
import io.fizz.common.domain.UserId;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class RemoveGroupMembersCommand implements AbstractCommand {
    private final UserId operatorId;
    private final GroupId groupId;
    private final Set<UserId> members = new HashSet<>();

    public RemoveGroupMembersCommand(final UserId aOperatorId, final GroupId aGroupId) {
        Utils.assertRequiredArgument(aOperatorId, "invalid_operator_id");
        Utils.assertRequiredArgument(aGroupId, "invalid_group_id");

        this.operatorId = aOperatorId;
        this.groupId = aGroupId;
    }

    @Override
    public byte[] key() {
        return groupId.qualifiedValue().getBytes(StandardCharsets.UTF_8);
    }

    public UserId operatorId() {
        return operatorId;
    }

    public GroupId groupId() {
        return groupId;
    }

    public Set<UserId> members() {
        return members;
    }

    public RemoveGroupMembersCommand add(final UserId aMemberId) {
        Utils.assertRequiredArgument(aMemberId, "invalid_member_id");

        members.add(aMemberId);

        return this;
    }
}
