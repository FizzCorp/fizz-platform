package io.fizz.chat.group.domain.group;

import io.fizz.chat.domain.channel.ChannelMessageId;
import io.fizz.chataccess.domain.role.RoleName;
import io.fizz.common.Utils;
import io.fizz.common.domain.UserId;

import java.util.Date;
import java.util.Objects;

public class GroupMember {
    public enum State {
        PENDING("pending"),
        JOINED("joined");

        private final String value;
        State(String aValue) {
            value = aValue;
        }

        public String value() {
            return value;
        }

        public static State fromValue(String aValue) {
            for (State s: values()) {
                if (aValue.equals(s.value())) {
                    return s;
                }
            }

            throw new IllegalArgumentException("invalid state value");
        }
    }

    private final UserId userId;
    private final GroupId groupId;
    private State state;
    private RoleName role;
    private ChannelMessageId lastReadMessageId;
    private final Date createdOn;

    public GroupMember(final UserId aUserId,
                       final GroupId aGroupId,
                       final State aState,
                       final RoleName aRole,
                       final Date aCreatedOn) {
        this(aUserId, aGroupId, aState, aRole, null, aCreatedOn);
    }

    public GroupMember(final UserId aUserId,
                       final GroupId aGroupId,
                       final State aState,
                       final RoleName aRole,
                       final ChannelMessageId aLastReadMessageId,
                       final Date aCreatedOn) {
        this.userId = aUserId;
        this.groupId = aGroupId;
        this.setLastReadMessageId(aLastReadMessageId);
        this.setState(aState);
        this.setRole(aRole);
        this.createdOn = aCreatedOn;
    }

    public UserId userId() {
        return userId;
    }

    public GroupId groupId() {
        return groupId;
    }

    public RoleName role() {
        return role;
    }

    public State state() {
        return state;
    }

    public Long lastReadMessageId() {
        return Objects.nonNull(lastReadMessageId) ? lastReadMessageId.value() : null;
    }

    public Date createdOn() {
        return createdOn;
    }

    public void setRole(RoleName aName) {
        Utils.assertRequiredArgument(aName, "invalid_role_name");

        role = aName;
    }

    public void setState(State aState) {
        Utils.assertRequiredArgument(aState, "invalid_member_state");

        state = aState;
    }

    public void setLastReadMessageId(ChannelMessageId aLastReadMessageId) {
        lastReadMessageId = aLastReadMessageId;
    }
}
