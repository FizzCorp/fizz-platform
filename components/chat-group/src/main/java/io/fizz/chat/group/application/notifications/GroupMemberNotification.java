package io.fizz.chat.group.application.notifications;

import io.fizz.chat.group.domain.group.GroupId;
import io.fizz.chat.group.domain.group.GroupMember;
import io.fizz.chataccess.domain.role.RoleName;
import io.fizz.common.Utils;
import io.fizz.common.domain.UserId;

public class GroupMemberNotification extends GroupNotification {
    private final UserId memberId;
    private final GroupMember.State state;
    private final RoleName role;

    public GroupMemberNotification(final GroupId aGroupId,
                                   final UserId aMemberId,
                                   final GroupMember.State aState,
                                   final RoleName aRole) {
        super(aGroupId);

        Utils.assertRequiredArgument(aMemberId, "invalid_member_id");
        Utils.assertRequiredArgument(aState, "invalid_member_state");
        Utils.assertRequiredArgument(aRole, "invalid_member_role");

        this.memberId = aMemberId;
        this.state = aState;
        this.role = aRole;
    }

    public UserId memberId() {
        return memberId;
    }

    public GroupMember.State state() {
        return state;
    }

    public RoleName role() {
        return role;
    }
}
