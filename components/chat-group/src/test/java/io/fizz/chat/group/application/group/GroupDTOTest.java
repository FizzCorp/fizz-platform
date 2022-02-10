package io.fizz.chat.group.application.group;

import io.fizz.chat.group.domain.group.Group;
import io.fizz.chat.group.domain.group.GroupId;
import io.fizz.chat.group.domain.group.GroupMember;
import io.fizz.chat.group.domain.group.GroupProfile;
import io.fizz.chataccess.domain.role.Role;
import io.fizz.chataccess.domain.role.RoleName;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

public class GroupDTOTest {
    @Test
    @DisplayName("it should create a DTO from an aggregate correctly")
    void constructionTest() throws Throwable {
        final RoleName memberRole = new RoleName("member");
        final GroupId id = new GroupId(new ApplicationId("appA"), "groupA");
        final UserId createdBy = new UserId("createdBy");
        final GroupProfile profile = new GroupProfile(
                createdBy, "test", "image.com", "this is a group", "test"
        );
        final Set<GroupMember> members = new HashSet<GroupMember>() {
            {
                add(new GroupMember(new UserId("userA"), id, GroupMember.State.JOINED, memberRole, new Date()));
                add(new GroupMember(new UserId("userB"), id, GroupMember.State.JOINED, memberRole, new Date()));
            }
        };
        final Group group = new Group(id, profile, members, new Date());
        final GroupDTO dto = new GroupDTO(group);

        Assertions.assertEquals(group.id().value(), dto.id());
        Assertions.assertEquals(group.title(), dto.title());
        Assertions.assertEquals(group.imageURL(), dto.imageURL());
        Assertions.assertEquals(group.description(), dto.description());
        Assertions.assertEquals(group.type(), dto.type());
        Assertions.assertEquals(group.members().size(), dto.members().length);

        int mi = 0;
        for (GroupMember member: group.members()) {
            assertEqual(member, dto.members()[mi++]);
        }
    }

    @Test
    @DisplayName("it should not create a DTO with an invalid aggregate")
    void invalidAggregateTest () {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new GroupDTO(null));
    }

    private void assertEqual(final GroupMember aMember, final GroupMemberDTO aDTO) {
        Assertions.assertEquals(aMember.userId().value(), aDTO.userId());
        Assertions.assertEquals(aMember.role().value(), aDTO.role());
        Assertions.assertEquals(aMember.state().value(), aDTO.state());
        Assertions.assertEquals(aMember.createdOn(), aDTO.createdOn());
    }
}
