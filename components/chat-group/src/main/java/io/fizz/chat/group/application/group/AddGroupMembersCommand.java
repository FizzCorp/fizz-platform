package io.fizz.chat.group.application.group;

import io.fizz.chat.group.domain.group.GroupId;
import io.fizz.chat.group.domain.group.GroupMember;
import io.fizz.chataccess.domain.role.RoleName;
import io.fizz.command.bus.AbstractCommand;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class AddGroupMembersCommand implements AbstractCommand {
    private static final int MAX_MEMBERS = 100;

    private final UserId operatorId;
    private final GroupId groupId;
    private final Set<GroupMemberData> members = new HashSet<>();

    public AddGroupMembersCommand(final String aOperatorId, final String aAppId, final String aGroupId) {
        try {
            this.operatorId = new UserId(aOperatorId);
            this.groupId = new GroupId(new ApplicationId(aAppId), aGroupId);
        }
        catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
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

    public Set<GroupMemberData> members() {
        return members;
    }

    public void add(final UserId aMemberId, final RoleName aMemberRole, final GroupMember.State aState) {
        Utils.assertRequiredArgument(aMemberId, "invalid_member_id");
        Utils.assertRequiredArgument(aMemberRole, "invalid_member_role");
        Utils.assertRequiredArgument(aState, "invalid_member_state");

        if (members.size() >= MAX_MEMBERS) {
            throw new IllegalStateException("max_member_limit_reached");
        }

        members.add(new GroupMemberData(aMemberId, aMemberRole, aState));
    }
}
