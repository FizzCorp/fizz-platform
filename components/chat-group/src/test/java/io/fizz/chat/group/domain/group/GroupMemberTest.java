package io.fizz.chat.group.domain.group;

import io.fizz.chataccess.domain.role.RoleName;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;

import java.util.Date;

public class GroupMemberTest {
    private final UserId memberId = new UserId("userA");
    private final RoleName role = new RoleName("member");
    private final ApplicationId app = appId();
    private final GroupId groupId = new GroupId(app, "groupA");

    @Test
    @DisplayName("it should create a valid member")
    public void memberCreationTest() {
        Date now = new Date();
        GroupMember member = new GroupMember(memberId, groupId, GroupMember.State.JOINED, role, now);

        Assertions.assertEquals(memberId, member.userId());
        Assertions.assertEquals(groupId, member.groupId());
        Assertions.assertEquals(role, member.role());
        Assertions.assertEquals(now, member.createdOn());
        Assertions.assertNull(member.lastReadMessageId());
    }

    @Test
    @DisplayName("it should not allow an invalid role to be set")
    public void setRole() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new GroupMember(memberId, groupId, GroupMember.State.JOINED, null, new Date())
        );

        Assertions.assertThrows(IllegalArgumentException.class, () -> create().setRole(null));
    }

    @Test
    @DisplayName("it should not allow an invalid state to be set")
    public void setState() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new GroupMember(memberId, groupId, null, role, new Date())
        );

        Assertions.assertThrows(IllegalArgumentException.class, () -> create().setState(null));
    }

    private GroupMember create() throws Throwable {
        GroupId groupId = new GroupId(new ApplicationId("appA"), "groupA");
        return new GroupMember(memberId, groupId, GroupMember.State.JOINED, role, new Date());
    }

    private ApplicationId appId() {
        try {
            return new ApplicationId("appA");
        }
        catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }
}
