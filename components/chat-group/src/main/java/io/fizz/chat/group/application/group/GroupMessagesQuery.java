package io.fizz.chat.group.application.group;

import io.fizz.chat.group.domain.group.GroupId;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;

public class GroupMessagesQuery {
    private final GroupId groupId;
    private final UserId requesterId;
    private final int count;
    private final Long before;
    private final Long after;

    public GroupMessagesQuery(final String aAppId,
                               final String aGroupId,
                               final String aRequesterId,
                               int aCount,
                               Long aBefore,
                               Long aAfter) {
        try {
            final ApplicationId appId = new ApplicationId(aAppId);

            groupId = new GroupId(appId, aGroupId);
            requesterId = new UserId(aRequesterId);
            count = aCount;
            before = aBefore;
            after = aAfter;
        }
        catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    public GroupId groupId() {
        return groupId;
    }

    public UserId requesterId() {
        return requesterId;
    }

    public int count() {
        return count;
    }

    public Long before() {
        return before;
    }

    public Long after() {
        return after;
    }
}
