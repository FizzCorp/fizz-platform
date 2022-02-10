package io.fizz.chat.group.application.group;

import io.fizz.chat.domain.channel.ChannelMessageId;
import io.fizz.chat.group.domain.group.GroupId;
import io.fizz.chat.group.domain.group.GroupMember;
import io.fizz.chataccess.domain.role.RoleName;
import io.fizz.command.bus.AbstractCommand;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;

import java.nio.charset.StandardCharsets;

public class UpdateGroupMemberCommand implements AbstractCommand {
    private final UserId operatorId;
    private final GroupId groupId;
    private final UserId memberId;
    private RoleName newRole;
    private GroupMember.State newState;
    private ChannelMessageId lastReadMessageId;

    public UpdateGroupMemberCommand(final String aAppId,
                                    final String aOperatorId,
                                    final String aGroupId,
                                    final String aMemberId) {
        try {
            ApplicationId appId = new ApplicationId(aAppId);
            this.operatorId = new UserId(aOperatorId);
            this.groupId = new GroupId(appId, aGroupId);
            this.memberId = new UserId(aMemberId);
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

    public UserId memberId() {
        return memberId;
    }

    public GroupMember.State newState() {
        return newState;
    }

    public RoleName newRole() {
        return newRole;
    }

    public ChannelMessageId lastReadMessageId() {
        return lastReadMessageId;
    }

    public void setNewState(final GroupMember.State aNewState) {
        this.newState = aNewState;
    }

    public void setNewRole(final String aNewRole) {
        this.newRole = new RoleName(aNewRole);
    }

    public void setLastReadMessageId(final Long aLastReadMessageId) {
        this.lastReadMessageId = new ChannelMessageId(aLastReadMessageId);
    }
}
