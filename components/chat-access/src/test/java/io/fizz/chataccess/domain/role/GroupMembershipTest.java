package io.fizz.chataccess.domain.role;

import io.fizz.chataccess.domain.context.AuthContextId;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.common.domain.UserId;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GroupMembershipTest {
    private static AuthContextId contextId;
    private static UserId uid;
    private static GroupName name;

    static {
        try {
            contextId = new AuthContextId(new ApplicationId("appA"), "contextA");
            uid = new UserId("userA");
            name = new GroupName(contextId, new RoleName("mutes"));
        }
        catch (DomainErrorException ex) { System.out.println(ex.getMessage()); }
    }

    @Test
    @DisplayName("it should create valid group member")
    void validGroupMemberTest() {
        final GroupMembership member = new GroupMembership(uid, name);
        final GroupMembership member2 = new GroupMembership(uid, name);
        final GroupMembership member3 = new GroupMembership(new UserId("userB"), name);
        final GroupMembership member4 = new GroupMembership(uid, new GroupName(contextId, new RoleName("bans")));

        Assertions.assertEquals(member.memberId(), uid);
        Assertions.assertEquals(member.groupName(), name);
        Assertions.assertEquals(member, member2);
        Assertions.assertNotEquals(member, null);
        Assertions.assertNotEquals(member, member3);
        Assertions.assertNotEquals(member, member4);
    }

    @Test
    @DisplayName("it should not create a group member with invalid input")
    void invalidGroupMemberTest() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new GroupMembership(null, name),
                "invalid_user_id"
        );

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new GroupMembership(uid, null),
                "invalid_group_name"
        );
    }
}
