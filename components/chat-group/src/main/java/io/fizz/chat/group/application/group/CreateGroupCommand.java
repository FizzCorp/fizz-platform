package io.fizz.chat.group.application.group;

import io.fizz.chat.group.domain.group.GroupId;
import io.fizz.chat.group.domain.group.GroupMember;
import io.fizz.chat.group.domain.group.GroupProfile;
import io.fizz.chataccess.domain.role.RoleName;
import io.fizz.command.bus.AbstractCommand;
import io.fizz.common.Utils;
import io.fizz.common.domain.UserId;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class CreateGroupCommand implements AbstractCommand {
    private final GroupId groupId;
    private final GroupProfile profile;
    private final Set<GroupMemberData> members = new HashSet<>();

    public CreateGroupCommand(final GroupId aGroupId,
                              final String aCreatedBy,
                              final String aTitle,
                              final String aImageURL,
                              final String aDescription,
                              final String aType) {
        Utils.assertRequiredArgument(aGroupId, "invalid_group_id");

        final UserId createdBy = new UserId(aCreatedBy);

        this.groupId = aGroupId;
        this.profile = new GroupProfile(createdBy, aTitle, aImageURL, aDescription, aType);
    }

    @Override
    public byte[] key() {
        return groupId.qualifiedValue().getBytes(StandardCharsets.UTF_8);
    }

    public void add(final UserId aMemberId, final RoleName aRole, final GroupMember.State aState) {
        Utils.assertRequiredArgument(aMemberId, "invalid_member_id");
        Utils.assertRequiredArgument(aRole, "invalid_member_role");
        Utils.assertRequiredArgument(aState, "invalid_member_state");

        members.add(new GroupMemberData(aMemberId, aRole, aState));
    }

    public GroupId groupId() {
        return groupId;
    }

    public GroupProfile profile() {
        return profile;
    }

    public Set<GroupMember> members() {
        Set<GroupMember> outMembers = new HashSet<>();

        for (GroupMemberData data: members) {
            final GroupMember member = new GroupMember(data.id(), groupId, data.state(), data.role(), new Date());
            outMembers.add(member);
        }

        return outMembers;
    }
}
