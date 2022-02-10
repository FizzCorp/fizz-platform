package io.fizz.chataccess.domain.role;

import io.fizz.common.Utils;
import io.fizz.common.domain.UserId;

import java.util.Date;
import java.util.Objects;

public class GroupMembership {
    private final UserId memberId;
    private final GroupName groupName;
    private final long ends;

    public GroupMembership(final UserId aMemberId, final GroupName aGroupName) {
        this(aMemberId, aGroupName, Utils.TIME_END);
    }

    public GroupMembership(final UserId aMemberId, final GroupName aGroupName, final Date aEnds) {
        Utils.assertRequiredArgument(aMemberId, "invalid_group_member");
        Utils.assertRequiredArgument(aGroupName, "invalid_group_name");
        Utils.assertRequiredArgument(aEnds, "invalid_end_time");
        Utils.assertArgumentRange(
            aEnds.getTime(), Utils.TIME_BEGIN.getTime(), Utils.TIME_END.getTime(), "invalid_end_time"
        );

        memberId = aMemberId;
        groupName = aGroupName;
        ends = aEnds.getTime();
    }

    public UserId memberId() {
        return memberId;
    }

    public GroupName groupName() {
        return groupName;
    }

    public Date ends() {
        return new Date(ends);
    }

    public boolean isEffective() {
        return ends().after(new Date());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupMembership that = (GroupMembership) o;
        return ends == that.ends &&
                memberId.equals(that.memberId) &&
                groupName.equals(that.groupName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId, groupName, ends);
    }
}
