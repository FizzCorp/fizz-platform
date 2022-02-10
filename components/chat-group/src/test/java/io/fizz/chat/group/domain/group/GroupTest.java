package io.fizz.chat.group.domain.group;

import io.fizz.chat.domain.Permissions;
import io.fizz.chat.domain.channel.ChannelMessageId;
import io.fizz.chat.group.domain.user.User;
import io.fizz.chat.group.infrastructure.ConfigService;
import io.fizz.chataccess.domain.role.Role;
import io.fizz.chataccess.domain.role.RoleName;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

public class GroupTest {
    private static final int GROUP_MEMBER_MAX = ConfigService.config().getNumber("chat.group.member.max").intValue();

    private static GroupId gid;
    private static final String validTitle = StringUtils.repeat("-", 128);
    private static final String validImageURL = StringUtils.repeat("*", 1024);
    private static final String validDescription = "desc";
    private static final String validType = "noType";
    private static final User admin = new User(
            new UserId("admin"),
            new Role(
                    new RoleName("admin"),
                    1,
                    Permissions.PUBLISH_MESSAGES.value(),
                    Permissions.READ_MESSAGES.value(),
                    GroupPermissions.EDIT_GROUP_PROFILE.value(),
                    GroupPermissions.INVITE_MEMBERS.value(),
                    GroupPermissions.ADD_MEMBERS.value(),
                    GroupPermissions.REMOVE_ANY_MEMBER.value(),
                    GroupPermissions.EDIT_MEMBER_ROLES.value()
            )
    );

    static {
        try {
            ApplicationId appId = new ApplicationId("appA");
            gid = new GroupId(appId, "groupA");
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Test
    @DisplayName("it should allow a valid group to be created")
    public void createGroupTest() {
        UserId creator = new UserId("userA");
        Date now = new Date();
        Group group = new Group(gid, validProfile(creator), null, now);

        Assertions.assertEquals(gid, group.id());
        Assertions.assertEquals(validTitle, group.title());
        Assertions.assertEquals(validImageURL, group.imageURL());
        Assertions.assertEquals(creator, group.createdBy());
        Assertions.assertEquals(now, group.createdOn());
        Assertions.assertEquals(now, group.updatedOn());

        Assertions.assertEquals(0, group.events().size());
    }

    @Test
    @DisplayName("it should allow a valid group to be created with members")
    public void createGroupWithMembersTest() {
        UserId creator = new UserId("creator");
        Date now = new Date();

        UserId joinedUserId = new UserId("joinedUser");
        UserId invitedUserId = new UserId("invitedUser");

        Set<GroupMember> members = new HashSet<GroupMember>() {{
            add(new GroupMember(joinedUserId, gid, GroupMember.State.JOINED, GroupRoles.ROLE_NAME_MEMBER, new Date()));
            add(new GroupMember(invitedUserId, gid, GroupMember.State.PENDING, GroupRoles.ROLE_NAME_MEMBER, new Date())); }};
        Group group = new Group(gid, validProfile(creator), members, now);

        Assertions.assertEquals(gid, group.id());
        Assertions.assertEquals(validTitle, group.title());
        Assertions.assertEquals(validImageURL, group.imageURL());
        Assertions.assertEquals(creator, group.createdBy());
        Assertions.assertEquals(now, group.createdOn());

        Assertions.assertEquals(2, group.members().size());
        Assertions.assertNotNull(group.member(joinedUserId));
        Assertions.assertNotNull(group.member(invitedUserId));
        Assertions.assertEquals(GroupMember.State.JOINED, group.member(joinedUserId).state());
        Assertions.assertEquals(GroupMember.State.PENDING, group.member(invitedUserId).state());
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MEMBER, group.member(joinedUserId).role());
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MEMBER, group.member(invitedUserId).role());

        Assertions.assertEquals(2, group.events().size());

        Assertions.assertEquals(GroupMemberAdded.TYPE, group.events().get(0).type());
        GroupMemberAdded event1 = ((GroupMemberAdded) group.events().get(0));
        if (joinedUserId.equals(event1.memberId())) {
            Assertions.assertEquals(joinedUserId, event1.memberId());
            Assertions.assertEquals(GroupMember.State.JOINED, event1.state());

            Assertions.assertEquals(GroupMemberAdded.TYPE, group.events().get(1).type());
            GroupMemberAdded event2 = ((GroupMemberAdded) group.events().get(1));
            Assertions.assertEquals(invitedUserId, event2.memberId());
            Assertions.assertEquals(GroupMember.State.PENDING, event2.state());
        }
        else if (invitedUserId.equals(event1.memberId())) {
            Assertions.assertEquals(invitedUserId, event1.memberId());
            Assertions.assertEquals(GroupMember.State.PENDING, event1.state());

            Assertions.assertEquals(GroupMemberAdded.TYPE, group.events().get(1).type());
            GroupMemberAdded event2 = ((GroupMemberAdded) group.events().get(1));
            Assertions.assertEquals(joinedUserId, event2.memberId());
            Assertions.assertEquals(GroupMember.State.JOINED, event2.state());
        }
    }

    @Test
    @DisplayName("it should not create a group for invalid values")
    public void invalidGroupTest() {
        UserId creator = new UserId("userA");
        Date now = new Date();
        GroupProfile profile = validProfile(creator);

        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new Group(null, profile, null, now)
        );
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new Group(gid, null, null, now)
        );
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            new Group(gid, profile, null, null)
        );
    }

    @Test
    @DisplayName("it should update the group profile")
    public void updateProfileTest() {
        String newTitle = "new group";
        String newImageURL = "image:com";
        String newDescription = "description";
        String newType = "newType";

        UserId joinedUserId = new UserId("joinedUser");
        UserId invitedUserId = new UserId("invitedUser");

        Set<GroupMember> members = new HashSet<GroupMember>() {{
            add(new GroupMember(joinedUserId, gid, GroupMember.State.JOINED, GroupRoles.ROLE_NAME_MEMBER, new Date()));
            add(new GroupMember(invitedUserId, gid, GroupMember.State.PENDING, GroupRoles.ROLE_NAME_MEMBER, new Date())); }};
        Group group = new Group(gid, validProfile(admin.id()), members, new Date());

        Assertions.assertEquals(validTitle, group.title());
        Assertions.assertEquals(validImageURL, group.imageURL());
        Assertions.assertEquals(validDescription, group.description());
        Assertions.assertEquals(validType, group.type());
        Assertions.assertEquals(2, group.events().size());

        group.updateProfile(admin, newTitle, null, null, null);
        Assertions.assertEquals(newTitle, group.title());
        Assertions.assertEquals(validImageURL, group.imageURL());
        Assertions.assertEquals(validDescription, group.description());
        Assertions.assertEquals(validType, group.type());
        Assertions.assertEquals(3, group.events().size());
        Assertions.assertEquals(GroupProfileUpdated.TYPE, group.events().get(2).type());
        GroupProfileUpdated profileUpdatedEvent1 = (GroupProfileUpdated) group.events().get(2);
        Assertions.assertEquals(1, profileUpdatedEvent1.members().size());
        Assertions.assertTrue(profileUpdatedEvent1.members().contains(joinedUserId));
        Assertions.assertFalse(profileUpdatedEvent1.members().contains(invitedUserId));

        group.updateProfile(admin, null, newImageURL, null, null);
        Assertions.assertEquals(newTitle, group.title());
        Assertions.assertEquals(newImageURL, group.imageURL());
        Assertions.assertEquals(validDescription, group.description());
        Assertions.assertEquals(validType, group.type());
        Assertions.assertEquals(4, group.events().size());
        Assertions.assertEquals(GroupProfileUpdated.TYPE, group.events().get(3).type());
        GroupProfileUpdated profileUpdatedEvent2 = (GroupProfileUpdated) group.events().get(3);
        Assertions.assertEquals(1, profileUpdatedEvent2.members().size());
        Assertions.assertTrue(profileUpdatedEvent2.members().contains(joinedUserId));
        Assertions.assertFalse(profileUpdatedEvent2.members().contains(invitedUserId));

        group.updateProfile(admin, null, null, newDescription, null);
        Assertions.assertEquals(newTitle, group.title());
        Assertions.assertEquals(newImageURL, group.imageURL());
        Assertions.assertEquals(newDescription, group.description());
        Assertions.assertEquals(validType, group.type());
        Assertions.assertEquals(5, group.events().size());
        Assertions.assertEquals(GroupProfileUpdated.TYPE, group.events().get(4).type());
        GroupProfileUpdated profileUpdatedEvent3 = (GroupProfileUpdated) group.events().get(4);
        Assertions.assertEquals(1, profileUpdatedEvent3.members().size());
        Assertions.assertTrue(profileUpdatedEvent3.members().contains(joinedUserId));
        Assertions.assertFalse(profileUpdatedEvent3.members().contains(invitedUserId));

        group.updateProfile(admin, null, null, null, newType);
        Assertions.assertEquals(newTitle, group.title());
        Assertions.assertEquals(newImageURL, group.imageURL());
        Assertions.assertEquals(newDescription, group.description());
        Assertions.assertEquals(newType, group.type());
        Assertions.assertEquals(6, group.events().size());
        Assertions.assertEquals(GroupProfileUpdated.TYPE, group.events().get(5).type());
        GroupProfileUpdated profileUpdatedEvent4 = (GroupProfileUpdated) group.events().get(5);
        Assertions.assertEquals(1, profileUpdatedEvent4.members().size());
        Assertions.assertTrue(profileUpdatedEvent4.members().contains(joinedUserId));
        Assertions.assertFalse(profileUpdatedEvent4.members().contains(invitedUserId));
    }

    @Test
    @DisplayName("it should not allow moderators or members to update profile")
    public void ownerUpdateProfileTest() {
        final User member = new User(new UserId("member"), Role.NULL);
        Group memberGroup = createValidGroupWithMember(admin.id(), member.id(), GroupRoles.ROLE_NAME_MEMBER);
        Assertions.assertEquals(1, memberGroup.events().size());
        Assertions.assertEquals(GroupMemberAdded.TYPE, memberGroup.events().get(0).type());
        Assertions.assertEquals(member.id(), ((GroupMemberAdded) memberGroup.events().get(0)).memberId());
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MEMBER, ((GroupMemberAdded) memberGroup.events().get(0)).role());

        Assertions.assertThrows(SecurityException.class, () ->
            memberGroup.updateProfile(member, "Test", null, validDescription, validType)
        );

        final User moderator = new User(new UserId("mod"), Role.NULL);
        Group modGroup = createValidGroupWithMember(admin.id(), moderator.id(), GroupRoles.ROLE_NAME_MODERATOR);
        Assertions.assertEquals(1, modGroup.events().size());
        Assertions.assertEquals(GroupMemberAdded.TYPE, modGroup.events().get(0).type());
        Assertions.assertEquals(moderator.id(), ((GroupMemberAdded) modGroup.events().get(0)).memberId());
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MODERATOR, ((GroupMemberAdded) modGroup.events().get(0)).role());

        Assertions.assertThrows(SecurityException.class, () ->
            modGroup.updateProfile(moderator, "test", null, validDescription, validType)
        );
    }

    @Test
    @DisplayName("it should not allow a member to add other pending members")
    public void memberAddPendingTest() {
        User member = new User(new UserId("member"), Role.NULL);
        Group group = createValidGroupWithMember(admin.id(), member.id(), GroupRoles.ROLE_NAME_MEMBER);
        Assertions.assertEquals(1, group.events().size());
        Assertions.assertEquals(GroupMemberAdded.TYPE, group.events().get(0).type());
        Assertions.assertEquals(member.id(), ((GroupMemberAdded) group.events().get(0)).memberId());
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MEMBER, ((GroupMemberAdded) group.events().get(0)).role());

        UserId memberA = new UserId("userA");
        Assertions.assertThrows(
                SecurityException.class,
                () -> group.add(member, memberA, GroupRoles.ROLE_NAME_MEMBER, GroupMember.State.PENDING)
        );

        UserId mod = new UserId("userB");
        Assertions.assertThrows(
                SecurityException.class,
                () -> group.add(member, mod, GroupRoles.ROLE_NAME_MODERATOR, GroupMember.State.PENDING)
        );
    }

    @Test
    @DisplayName("it should not allow a member to add other joined members")
    public void memberAddJoinedTest() {
        User member = new User(new UserId("member"), Role.NULL);
        Group group = createValidGroupWithMember(admin.id(), member.id(), GroupRoles.ROLE_NAME_MEMBER);
        Assertions.assertEquals(1, group.events().size());
        Assertions.assertEquals(GroupMemberAdded.TYPE, group.events().get(0).type());
        Assertions.assertEquals(member.id(), ((GroupMemberAdded) group.events().get(0)).memberId());
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MEMBER, ((GroupMemberAdded) group.events().get(0)).role());

        UserId memberA = new UserId("userA");
        Assertions.assertThrows(
                SecurityException.class,
                () -> group.add(member, memberA, GroupRoles.ROLE_NAME_MEMBER, GroupMember.State.JOINED)
        );

        UserId mod = new UserId("userB");
        Assertions.assertThrows(
                SecurityException.class,
                () -> group.add(member, mod, GroupRoles.ROLE_NAME_MODERATOR, GroupMember.State.JOINED)
        );
    }

    @Test
    @DisplayName("it should not allow an moderator to add other pending members")
    public void modAddPendingTest() {
        User moderator = new User(new UserId("mod"), Role.NULL);
        Group group = createValidGroupWithMember(admin.id(), moderator.id(), GroupRoles.ROLE_NAME_MODERATOR);
        Assertions.assertEquals(1, group.events().size());
        Assertions.assertEquals(GroupMemberAdded.TYPE, group.events().get(0).type());
        Assertions.assertEquals(moderator.id(), ((GroupMemberAdded) group.events().get(0)).memberId());
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MODERATOR, ((GroupMemberAdded) group.events().get(0)).role());

        UserId memberA = new UserId("userA");
        Assertions.assertThrows(
                SecurityException.class,
                () -> group.add(moderator, memberA, GroupRoles.ROLE_NAME_MEMBER, GroupMember.State.PENDING)
        );

        UserId mod = new UserId("userB");
        Assertions.assertThrows(
                SecurityException.class,
                () -> group.add(moderator, mod, GroupRoles.ROLE_NAME_MODERATOR, GroupMember.State.PENDING)
        );
    }

    @Test
    @DisplayName("it should not allow a moderator to add other joined members")
    public void modAddJoinedTest() {
        User moderator = new User(new UserId("mod"), Role.NULL);
        Group group = createValidGroupWithMember(admin.id(), moderator.id(), GroupRoles.ROLE_NAME_MODERATOR);
        Assertions.assertEquals(1, group.events().size());
        Assertions.assertEquals(GroupMemberAdded.TYPE, group.events().get(0).type());
        Assertions.assertEquals(moderator.id(), ((GroupMemberAdded) group.events().get(0)).memberId());
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MODERATOR, ((GroupMemberAdded) group.events().get(0)).role());

        UserId memberA = new UserId("userA");
        Assertions.assertThrows(
                SecurityException.class,
                () -> group.add(moderator, memberA, GroupRoles.ROLE_NAME_MEMBER, GroupMember.State.JOINED)
        );

        UserId mod = new UserId("userB");
        Assertions.assertThrows(
                SecurityException.class,
                () -> group.add(moderator, mod, GroupRoles.ROLE_NAME_MODERATOR, GroupMember.State.JOINED)
        );
    }

    @Test
    @DisplayName("it should be possible to add both pending and joined members")
    public void addMembersTest() {
        UserId joinedUserId = new UserId("joinedUser");
        UserId invitedUserId = new UserId("invitedUser");

        Set<GroupMember> members = new HashSet<GroupMember>() {{
            add(new GroupMember(joinedUserId, gid, GroupMember.State.JOINED, GroupRoles.ROLE_NAME_MEMBER, new Date()));
            add(new GroupMember(invitedUserId, gid, GroupMember.State.PENDING, GroupRoles.ROLE_NAME_MEMBER, new Date())); }};
        Group group = new Group(gid, validProfile(admin.id()), members, new Date());

        Assertions.assertEquals(validTitle, group.title());
        Assertions.assertEquals(validImageURL, group.imageURL());
        Assertions.assertEquals(validDescription, group.description());
        Assertions.assertEquals(validType, group.type());

        Assertions.assertEquals(2, group.members().size());
        Assertions.assertEquals(2, group.events().size());

        UserId memberA = new UserId("userA");
        group.add(admin, memberA, GroupRoles.ROLE_NAME_MEMBER, GroupMember.State.PENDING);
        Assertions.assertEquals(3, group.members().size());
        Assertions.assertEquals(3, group.events().size());
        Assertions.assertEquals(GroupMemberAdded.TYPE, group.events().get(2).type());
        Assertions.assertEquals(memberA, ((GroupMemberAdded) group.events().get(2)).memberId());
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MEMBER, ((GroupMemberAdded) group.events().get(2)).role());
        Assertions.assertEquals(GroupMember.State.PENDING, ((GroupMemberAdded) group.events().get(2)).state());
        Assertions.assertEquals(2, ((GroupMemberAdded) group.events().get(2)).memberIds().size());
        Assertions.assertTrue(((GroupMemberAdded) group.events().get(2)).memberIds().contains(joinedUserId));
        Assertions.assertTrue(((GroupMemberAdded) group.events().get(2)).memberIds().contains(memberA));
        Assertions.assertFalse(((GroupMemberAdded) group.events().get(2)).memberIds().contains(invitedUserId));

        UserId moderatorA = new UserId("userB");
        group.add(admin, moderatorA, GroupRoles.ROLE_NAME_MODERATOR, GroupMember.State.PENDING);
        Assertions.assertEquals(4, group.members().size());
        Assertions.assertEquals(4, group.events().size());
        Assertions.assertEquals(GroupMemberAdded.TYPE, group.events().get(3).type());
        Assertions.assertEquals(moderatorA, ((GroupMemberAdded) group.events().get(3)).memberId());
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MODERATOR, ((GroupMemberAdded) group.events().get(3)).role());
        Assertions.assertEquals(GroupMember.State.PENDING, ((GroupMemberAdded) group.events().get(3)).state());
        Assertions.assertEquals(2, ((GroupMemberAdded) group.events().get(3)).memberIds().size());
        Assertions.assertTrue(((GroupMemberAdded) group.events().get(3)).memberIds().contains(joinedUserId));
        Assertions.assertTrue(((GroupMemberAdded) group.events().get(3)).memberIds().contains(moderatorA));
        Assertions.assertFalse(((GroupMemberAdded) group.events().get(3)).memberIds().contains(invitedUserId));
        Assertions.assertFalse(((GroupMemberAdded) group.events().get(3)).memberIds().contains(memberA));

        UserId memberB = new UserId("userC");
        group.add(admin, memberB, GroupRoles.ROLE_NAME_MEMBER, GroupMember.State.JOINED);
        Assertions.assertEquals(5, group.members().size());
        Assertions.assertEquals(5, group.events().size());
        Assertions.assertEquals(GroupMemberAdded.TYPE, group.events().get(4).type());
        Assertions.assertEquals(memberB, ((GroupMemberAdded) group.events().get(4)).memberId());
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MEMBER, ((GroupMemberAdded) group.events().get(4)).role());
        Assertions.assertEquals(GroupMember.State.JOINED, ((GroupMemberAdded) group.events().get(4)).state());
        Assertions.assertEquals(2, ((GroupMemberAdded) group.events().get(4)).memberIds().size());
        Assertions.assertTrue(((GroupMemberAdded) group.events().get(4)).memberIds().contains(joinedUserId));
        Assertions.assertTrue(((GroupMemberAdded) group.events().get(4)).memberIds().contains(memberB));
        Assertions.assertFalse(((GroupMemberAdded) group.events().get(4)).memberIds().contains(invitedUserId));
        Assertions.assertFalse(((GroupMemberAdded) group.events().get(4)).memberIds().contains(memberA));
        Assertions.assertFalse(((GroupMemberAdded) group.events().get(4)).memberIds().contains(moderatorA));

        UserId moderatorB = new UserId("userD");
        group.add(admin, moderatorB, GroupRoles.ROLE_NAME_MODERATOR, GroupMember.State.JOINED);
        Assertions.assertEquals(6, group.members().size());
        Assertions.assertEquals(6, group.events().size());
        Assertions.assertEquals(GroupMemberAdded.TYPE, group.events().get(5).type());
        Assertions.assertEquals(moderatorB, ((GroupMemberAdded) group.events().get(5)).memberId());
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MODERATOR, ((GroupMemberAdded) group.events().get(5)).role());
        Assertions.assertEquals(GroupMember.State.JOINED, ((GroupMemberAdded) group.events().get(5)).state());
        Assertions.assertEquals(3, ((GroupMemberAdded) group.events().get(5)).memberIds().size());
        Assertions.assertTrue(((GroupMemberAdded) group.events().get(5)).memberIds().contains(joinedUserId));
        Assertions.assertTrue(((GroupMemberAdded) group.events().get(5)).memberIds().contains(memberB));
        Assertions.assertTrue(((GroupMemberAdded) group.events().get(5)).memberIds().contains(moderatorB));
        Assertions.assertFalse(((GroupMemberAdded) group.events().get(5)).memberIds().contains(invitedUserId));
        Assertions.assertFalse(((GroupMemberAdded) group.events().get(5)).memberIds().contains(memberA));
        Assertions.assertFalse(((GroupMemberAdded) group.events().get(5)).memberIds().contains(moderatorA));
    }

    @Test
    @DisplayName("it should not allow existing members to be added")
    public void addExistingMembersTest() {
        Group group = createValidGroup(admin.id());

        UserId member = new UserId("userA");
        group.add(admin, member, GroupRoles.ROLE_NAME_MEMBER, GroupMember.State.PENDING);

        Assertions.assertEquals(1, group.members().size());
        Assertions.assertEquals(1, group.events().size());

        Assertions.assertThrows(
                IllegalStateException.class,
                () -> group.add(admin, member, GroupRoles.ROLE_NAME_MEMBER, GroupMember.State.PENDING),
                "already_member"
        );
    }

    @Test
    @DisplayName("it should be possible to remove all kinds of members")
    public void removeMemberTest() {
        Group group = createValidGroup(admin.id());

        UserId memberPending = new UserId("userA");
        group.add(admin, memberPending, GroupRoles.ROLE_NAME_MEMBER, GroupMember.State.PENDING);
        Assertions.assertEquals(1, group.events().size());
        Assertions.assertEquals(GroupMemberAdded.TYPE, group.events().get(0).type());

        UserId memberJoined = new UserId("userC");
        group.add(admin, memberJoined, GroupRoles.ROLE_NAME_MEMBER, GroupMember.State.JOINED);
        Assertions.assertEquals(2, group.events().size());
        Assertions.assertEquals(GroupMemberAdded.TYPE, group.events().get(1).type());

        UserId moderatorPending = new UserId("userB");
        group.add(admin, moderatorPending, GroupRoles.ROLE_NAME_MODERATOR, GroupMember.State.PENDING);
        Assertions.assertEquals(3, group.events().size());
        Assertions.assertEquals(GroupMemberAdded.TYPE, group.events().get(2).type());

        UserId moderatorJoined = new UserId("userD");
        group.add(admin, moderatorJoined, GroupRoles.ROLE_NAME_MODERATOR, GroupMember.State.JOINED);
        Assertions.assertEquals(4, group.events().size());
        Assertions.assertEquals(GroupMemberAdded.TYPE, group.events().get(3).type());

        Assertions.assertEquals(4, group.members().size());
        Assertions.assertEquals(4, group.events().size());

        group.remove(admin, memberPending);
        Assertions.assertEquals(3, group.members().size());
        Assertions.assertEquals(5, group.events().size());
        Assertions.assertEquals(GroupMemberRemoved.TYPE, group.events().get(4).type());
        Assertions.assertEquals(memberPending, ((GroupMemberRemoved) group.events().get(4)).memberId());
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MEMBER, ((GroupMemberRemoved) group.events().get(4)).role());
        Assertions.assertEquals(GroupMember.State.PENDING, ((GroupMemberRemoved) group.events().get(4)).state());
        Assertions.assertEquals(3, ((GroupMemberRemoved) group.events().get(4)).memberIds().size());
        Assertions.assertTrue(((GroupMemberRemoved) group.events().get(4)).memberIds().contains(memberPending));
        Assertions.assertTrue(((GroupMemberRemoved) group.events().get(4)).memberIds().contains(memberJoined));
        Assertions.assertTrue(((GroupMemberRemoved) group.events().get(4)).memberIds().contains(moderatorJoined));
        Assertions.assertFalse(((GroupMemberRemoved) group.events().get(4)).memberIds().contains(moderatorPending));

        group.remove(admin, memberJoined);
        Assertions.assertEquals(2, group.members().size());
        Assertions.assertEquals(6, group.events().size());
        Assertions.assertEquals(GroupMemberRemoved.TYPE, group.events().get(5).type());
        Assertions.assertEquals(memberJoined, ((GroupMemberRemoved) group.events().get(5)).memberId());
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MEMBER, ((GroupMemberRemoved) group.events().get(5)).role());
        Assertions.assertEquals(GroupMember.State.JOINED, ((GroupMemberRemoved) group.events().get(5)).state());
        Assertions.assertEquals(2, ((GroupMemberRemoved) group.events().get(5)).memberIds().size());
        Assertions.assertTrue(((GroupMemberRemoved) group.events().get(5)).memberIds().contains(memberJoined));
        Assertions.assertTrue(((GroupMemberRemoved) group.events().get(5)).memberIds().contains(moderatorJoined));
        Assertions.assertFalse(((GroupMemberRemoved) group.events().get(5)).memberIds().contains(moderatorPending));
        Assertions.assertFalse(((GroupMemberRemoved) group.events().get(5)).memberIds().contains(memberPending));

        group.remove(admin, moderatorPending);
        Assertions.assertEquals(1, group.members().size());
        Assertions.assertEquals(7, group.events().size());
        Assertions.assertEquals(GroupMemberRemoved.TYPE, group.events().get(6).type());
        Assertions.assertEquals(moderatorPending, ((GroupMemberRemoved) group.events().get(6)).memberId());
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MODERATOR, ((GroupMemberRemoved) group.events().get(6)).role());
        Assertions.assertEquals(GroupMember.State.PENDING, ((GroupMemberRemoved) group.events().get(6)).state());
        Assertions.assertEquals(2, ((GroupMemberRemoved) group.events().get(6)).memberIds().size());
        Assertions.assertTrue(((GroupMemberRemoved) group.events().get(6)).memberIds().contains(moderatorPending));
        Assertions.assertTrue(((GroupMemberRemoved) group.events().get(6)).memberIds().contains(moderatorJoined));
        Assertions.assertFalse(((GroupMemberRemoved) group.events().get(6)).memberIds().contains(memberPending));
        Assertions.assertFalse(((GroupMemberRemoved) group.events().get(6)).memberIds().contains(memberJoined));

        group.remove(admin, moderatorJoined);
        Assertions.assertEquals(0, group.members().size());
        Assertions.assertEquals(8, group.events().size());
        Assertions.assertEquals(GroupMemberRemoved.TYPE, group.events().get(7).type());
        Assertions.assertEquals(moderatorJoined, ((GroupMemberRemoved) group.events().get(7)).memberId());
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MODERATOR, ((GroupMemberRemoved) group.events().get(7)).role());
        Assertions.assertEquals(GroupMember.State.JOINED, ((GroupMemberRemoved) group.events().get(7)).state());
        Assertions.assertEquals(1, ((GroupMemberRemoved) group.events().get(7)).memberIds().size());
        Assertions.assertTrue(((GroupMemberRemoved) group.events().get(7)).memberIds().contains(moderatorJoined));
        Assertions.assertFalse(((GroupMemberRemoved) group.events().get(7)).memberIds().contains(memberPending));
        Assertions.assertFalse(((GroupMemberRemoved) group.events().get(7)).memberIds().contains(memberJoined));
        Assertions.assertFalse(((GroupMemberRemoved) group.events().get(7)).memberIds().contains(moderatorPending));
    }

    @Test
    @DisplayName("it should allow a member to remove themselves")
    public void selfRemovePendingTest() {
        Group group = createValidGroup(admin.id());
        Assertions.assertEquals(0, group.members().size());
        Assertions.assertEquals(0, group.events().size());

        User memberP = new User(new UserId("userA"), Role.NULL);
        group.add(admin, memberP.id(), GroupRoles.ROLE_NAME_MEMBER, GroupMember.State.PENDING);
        Assertions.assertEquals(1, group.members().size());
        Assertions.assertEquals(1, group.events().size());

        User memberJ = new User(new UserId("userC"), Role.NULL);
        group.add(admin, memberJ.id(), GroupRoles.ROLE_NAME_MEMBER, GroupMember.State.JOINED);
        Assertions.assertEquals(2, group.members().size());
        Assertions.assertEquals(2, group.events().size());

        User moderatorP = new User(new UserId("userB"), Role.NULL);
        group.add(admin, moderatorP.id(), GroupRoles.ROLE_NAME_MODERATOR, GroupMember.State.PENDING);
        Assertions.assertEquals(3, group.members().size());
        Assertions.assertEquals(3, group.events().size());

        User moderatorJ = new User(new UserId("userD"), Role.NULL);
        group.add(admin, moderatorJ.id(), GroupRoles.ROLE_NAME_MODERATOR, GroupMember.State.JOINED);
        Assertions.assertEquals(4, group.members().size());
        Assertions.assertEquals(4, group.events().size());

        group.remove(memberP, memberP.id());
        Assertions.assertEquals(3, group.members().size());
        Assertions.assertEquals(5, group.events().size());
        Assertions.assertEquals(GroupMemberRemoved.TYPE, group.events().get(4).type());
        Assertions.assertEquals(memberP.id(), ((GroupMemberRemoved) group.events().get(4)).memberId());
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MEMBER, ((GroupMemberRemoved) group.events().get(4)).role());
        Assertions.assertEquals(GroupMember.State.PENDING, ((GroupMemberRemoved) group.events().get(4)).state());
        Assertions.assertEquals(3, ((GroupMemberRemoved) group.events().get(4)).memberIds().size());
        Assertions.assertTrue(((GroupMemberRemoved) group.events().get(4)).memberIds().contains(memberP.id()));
        Assertions.assertTrue(((GroupMemberRemoved) group.events().get(4)).memberIds().contains(memberJ.id()));
        Assertions.assertTrue(((GroupMemberRemoved) group.events().get(4)).memberIds().contains(moderatorJ.id()));
        Assertions.assertFalse(((GroupMemberRemoved) group.events().get(4)).memberIds().contains(moderatorP.id()));

        group.remove(memberJ, memberJ.id());
        Assertions.assertEquals(2, group.members().size());
        Assertions.assertEquals(6, group.events().size());
        Assertions.assertEquals(GroupMemberRemoved.TYPE, group.events().get(5).type());
        Assertions.assertEquals(memberJ.id(), ((GroupMemberRemoved) group.events().get(5)).memberId());
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MEMBER, ((GroupMemberRemoved) group.events().get(5)).role());
        Assertions.assertEquals(GroupMember.State.JOINED, ((GroupMemberRemoved) group.events().get(5)).state());
        Assertions.assertEquals(2, ((GroupMemberRemoved) group.events().get(5)).memberIds().size());
        Assertions.assertTrue(((GroupMemberRemoved) group.events().get(5)).memberIds().contains(memberJ.id()));
        Assertions.assertTrue(((GroupMemberRemoved) group.events().get(5)).memberIds().contains(moderatorJ.id()));
        Assertions.assertFalse(((GroupMemberRemoved) group.events().get(5)).memberIds().contains(moderatorP.id()));
        Assertions.assertFalse(((GroupMemberRemoved) group.events().get(5)).memberIds().contains(memberP.id()));

        group.remove(moderatorP, moderatorP.id());
        Assertions.assertEquals(1, group.members().size());
        Assertions.assertEquals(7, group.events().size());
        Assertions.assertEquals(GroupMemberRemoved.TYPE, group.events().get(6).type());
        Assertions.assertEquals(moderatorP.id(), ((GroupMemberRemoved) group.events().get(6)).memberId());
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MODERATOR, ((GroupMemberRemoved) group.events().get(6)).role());
        Assertions.assertEquals(GroupMember.State.PENDING, ((GroupMemberRemoved) group.events().get(6)).state());
        Assertions.assertEquals(2, ((GroupMemberRemoved) group.events().get(6)).memberIds().size());
        Assertions.assertTrue(((GroupMemberRemoved) group.events().get(6)).memberIds().contains(moderatorP.id()));
        Assertions.assertTrue(((GroupMemberRemoved) group.events().get(6)).memberIds().contains(moderatorJ.id()));
        Assertions.assertFalse(((GroupMemberRemoved) group.events().get(6)).memberIds().contains(memberP.id()));
        Assertions.assertFalse(((GroupMemberRemoved) group.events().get(6)).memberIds().contains(memberJ.id()));

        group.remove(moderatorJ, moderatorJ.id());
        Assertions.assertEquals(0, group.members().size());
        Assertions.assertEquals(8, group.events().size());
        Assertions.assertEquals(GroupMemberRemoved.TYPE, group.events().get(7).type());
        Assertions.assertEquals(moderatorJ.id(), ((GroupMemberRemoved) group.events().get(7)).memberId());
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MODERATOR, ((GroupMemberRemoved) group.events().get(7)).role());
        Assertions.assertEquals(GroupMember.State.JOINED, ((GroupMemberRemoved) group.events().get(7)).state());
        Assertions.assertEquals(1, ((GroupMemberRemoved) group.events().get(7)).memberIds().size());
        Assertions.assertTrue(((GroupMemberRemoved) group.events().get(7)).memberIds().contains(moderatorJ.id()));
        Assertions.assertFalse(((GroupMemberRemoved) group.events().get(7)).memberIds().contains(memberP.id()));
        Assertions.assertFalse(((GroupMemberRemoved) group.events().get(7)).memberIds().contains(memberJ.id()));
        Assertions.assertFalse(((GroupMemberRemoved) group.events().get(7)).memberIds().contains(moderatorP.id()));
    }

    @Test
    @DisplayName("it should allow only joined members to publish a message")
    public void publishMessage() {
        Group group = createValidGroup(admin.id());
        Assertions.assertEquals(0, group.events().size());

        group.publishMessage(admin);

        User moderator = new User(new UserId("mod"), Role.NULL);
        Assertions.assertThrows(SecurityException.class, () -> group.publishMessage(moderator));

        group.add(admin, moderator.id(), GroupRoles.ROLE_NAME_MODERATOR, GroupMember.State.PENDING);
        Assertions.assertEquals(1, group.members().size());
        Assertions.assertNotNull(group.member(moderator.id()));
        Assertions.assertEquals(GroupMember.State.PENDING, group.member(moderator.id()).state());
        Assertions.assertEquals(1, group.events().size());
        Assertions.assertEquals(GroupMemberAdded.TYPE, group.events().get(0).type());
        Assertions.assertEquals(moderator.id(), ((GroupMemberAdded) group.events().get(0)).memberId());
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MODERATOR, ((GroupMemberAdded) group.events().get(0)).role());
        Assertions.assertEquals(GroupMember.State.PENDING, ((GroupMemberAdded) group.events().get(0)).state());
        Assertions.assertEquals(1, ((GroupMemberAdded) group.events().get(0)).memberIds().size());
        Assertions.assertTrue(((GroupMemberAdded) group.events().get(0)).memberIds().contains(moderator.id()));

        Assertions.assertThrows(SecurityException.class, () -> group.publishMessage(moderator));

        group.changeMemberState(moderator.id(), GroupMember.State.JOINED);
        Assertions.assertEquals(1, group.members().size());
        Assertions.assertNotNull(group.member(moderator.id()));
        Assertions.assertEquals(GroupMember.State.JOINED, group.member(moderator.id()).state());
        Assertions.assertEquals(2, group.events().size());
        Assertions.assertEquals(GroupMemberUpdated.TYPE, group.events().get(1).type());
        Assertions.assertEquals(moderator.id(), ((GroupMemberUpdated) group.events().get(1)).memberId());
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MODERATOR, ((GroupMemberUpdated) group.events().get(1)).role());
        Assertions.assertEquals(GroupMember.State.JOINED, ((GroupMemberUpdated) group.events().get(1)).state());
        Assertions.assertEquals(1, ((GroupMemberUpdated) group.events().get(1)).memberIds().size());
        Assertions.assertTrue(((GroupMemberUpdated) group.events().get(1)).memberIds().contains(moderator.id()));

        group.publishMessage(moderator);

        User member = new User(new UserId("member"), Role.NULL);
        Assertions.assertThrows(SecurityException.class, () -> group.publishMessage(member));

        group.add(admin, member.id(), GroupRoles.ROLE_NAME_MEMBER, GroupMember.State.PENDING);
        Assertions.assertEquals(2, group.members().size());
        Assertions.assertNotNull(group.member(moderator.id()));
        Assertions.assertNotNull(group.member(member.id()));
        Assertions.assertEquals(GroupMember.State.JOINED, group.member(moderator.id()).state());
        Assertions.assertEquals(GroupMember.State.PENDING, group.member(member.id()).state());
        Assertions.assertEquals(3, group.events().size());
        Assertions.assertEquals(GroupMemberAdded.TYPE, group.events().get(2).type());
        Assertions.assertEquals(member.id(), ((GroupMemberAdded) group.events().get(2)).memberId());
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MEMBER, ((GroupMemberAdded) group.events().get(2)).role());
        Assertions.assertEquals(GroupMember.State.PENDING, ((GroupMemberAdded) group.events().get(2)).state());
        Assertions.assertEquals(2, ((GroupMemberAdded) group.events().get(2)).memberIds().size());
        Assertions.assertTrue(((GroupMemberAdded) group.events().get(2)).memberIds().contains(member.id()));
        Assertions.assertTrue(((GroupMemberAdded) group.events().get(2)).memberIds().contains(moderator.id()));

        Assertions.assertThrows(SecurityException.class, () -> group.publishMessage(member));

        group.changeMemberState(member.id(), GroupMember.State.JOINED);
        Assertions.assertEquals(2, group.members().size());
        Assertions.assertNotNull(group.member(moderator.id()));
        Assertions.assertNotNull(group.member(member.id()));
        Assertions.assertEquals(GroupMember.State.JOINED, group.member(moderator.id()).state());
        Assertions.assertEquals(GroupMember.State.JOINED, group.member(member.id()).state());
        Assertions.assertEquals(4, group.events().size());
        Assertions.assertEquals(GroupMemberUpdated.TYPE, group.events().get(3).type());
        Assertions.assertEquals(member.id(), ((GroupMemberUpdated) group.events().get(3)).memberId());
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MEMBER, ((GroupMemberUpdated) group.events().get(3)).role());
        Assertions.assertEquals(GroupMember.State.JOINED, ((GroupMemberUpdated) group.events().get(3)).state());
        Assertions.assertEquals(2, ((GroupMemberUpdated) group.events().get(3)).memberIds().size());
        Assertions.assertTrue(((GroupMemberUpdated) group.events().get(3)).memberIds().contains(moderator.id()));
        Assertions.assertTrue(((GroupMemberUpdated) group.events().get(3)).memberIds().contains(member.id()));

        group.publishMessage(member);
    }

    @Test
    @DisplayName("it should allow only joined members to query messages")
    public void queryMessages() {
        Group group = createValidGroup(admin.id());
        Assertions.assertEquals(0, group.events().size());

        group.queryMessages(admin);

        User moderator = new User(new UserId("mod"), Role.NULL);
        Assertions.assertThrows(SecurityException.class, () -> group.queryMessages(moderator));

        group.add(admin, moderator.id(), GroupRoles.ROLE_NAME_MODERATOR, GroupMember.State.PENDING);
        Assertions.assertEquals(1, group.members().size());
        Assertions.assertNotNull(group.member(moderator.id()));
        Assertions.assertEquals(GroupMember.State.PENDING, group.member(moderator.id()).state());
        Assertions.assertEquals(1, group.events().size());
        Assertions.assertEquals(GroupMemberAdded.TYPE, group.events().get(0).type());
        Assertions.assertEquals(moderator.id(), ((GroupMemberAdded) group.events().get(0)).memberId());
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MODERATOR, ((GroupMemberAdded) group.events().get(0)).role());
        Assertions.assertEquals(GroupMember.State.PENDING, ((GroupMemberAdded) group.events().get(0)).state());
        Assertions.assertEquals(1, ((GroupMemberAdded) group.events().get(0)).memberIds().size());
        Assertions.assertTrue(((GroupMemberAdded) group.events().get(0)).memberIds().contains(moderator.id()));

        Assertions.assertThrows(SecurityException.class, () -> group.queryMessages(moderator));

        group.changeMemberState(moderator.id(), GroupMember.State.JOINED);
        Assertions.assertEquals(1, group.members().size());
        Assertions.assertNotNull(group.member(moderator.id()));
        Assertions.assertEquals(GroupMember.State.JOINED, group.member(moderator.id()).state());
        Assertions.assertEquals(2, group.events().size());
        Assertions.assertEquals(GroupMemberUpdated.TYPE, group.events().get(1).type());
        Assertions.assertEquals(moderator.id(), ((GroupMemberUpdated) group.events().get(1)).memberId());
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MODERATOR, ((GroupMemberUpdated) group.events().get(1)).role());
        Assertions.assertEquals(GroupMember.State.JOINED, ((GroupMemberUpdated) group.events().get(1)).state());
        Assertions.assertEquals(1, ((GroupMemberUpdated) group.events().get(1)).memberIds().size());
        Assertions.assertTrue(((GroupMemberUpdated) group.events().get(1)).memberIds().contains(moderator.id()));

        group.queryMessages(moderator);

        User member = new User(new UserId("member"), Role.NULL);
        Assertions.assertThrows(SecurityException.class, () -> group.queryMessages(member));

        group.add(admin, member.id(), GroupRoles.ROLE_NAME_MEMBER, GroupMember.State.PENDING);
        Assertions.assertEquals(2, group.members().size());
        Assertions.assertNotNull(group.member(moderator.id()));
        Assertions.assertNotNull(group.member(member.id()));
        Assertions.assertEquals(GroupMember.State.JOINED, group.member(moderator.id()).state());
        Assertions.assertEquals(GroupMember.State.PENDING, group.member(member.id()).state());
        Assertions.assertEquals(3, group.events().size());
        Assertions.assertEquals(GroupMemberAdded.TYPE, group.events().get(2).type());
        Assertions.assertEquals(member.id(), ((GroupMemberAdded) group.events().get(2)).memberId());
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MEMBER, ((GroupMemberAdded) group.events().get(2)).role());
        Assertions.assertEquals(GroupMember.State.PENDING, ((GroupMemberAdded) group.events().get(2)).state());
        Assertions.assertEquals(2, ((GroupMemberAdded) group.events().get(2)).memberIds().size());
        Assertions.assertTrue(((GroupMemberAdded) group.events().get(2)).memberIds().contains(member.id()));
        Assertions.assertTrue(((GroupMemberAdded) group.events().get(2)).memberIds().contains(moderator.id()));

        Assertions.assertThrows(SecurityException.class, () -> group.queryMessages(member));

        group.changeMemberState(member.id(), GroupMember.State.JOINED);
        Assertions.assertEquals(2, group.members().size());
        Assertions.assertNotNull(group.member(moderator.id()));
        Assertions.assertNotNull(group.member(member.id()));
        Assertions.assertEquals(GroupMember.State.JOINED, group.member(moderator.id()).state());
        Assertions.assertEquals(GroupMember.State.JOINED, group.member(member.id()).state());
        Assertions.assertEquals(4, group.events().size());
        Assertions.assertEquals(GroupMemberUpdated.TYPE, group.events().get(3).type());
        Assertions.assertEquals(member.id(), ((GroupMemberUpdated) group.events().get(3)).memberId());
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MEMBER, ((GroupMemberUpdated) group.events().get(3)).role());
        Assertions.assertEquals(GroupMember.State.JOINED, ((GroupMemberUpdated) group.events().get(3)).state());
        Assertions.assertEquals(2, ((GroupMemberUpdated) group.events().get(3)).memberIds().size());
        Assertions.assertTrue(((GroupMemberUpdated) group.events().get(3)).memberIds().contains(moderator.id()));
        Assertions.assertTrue(((GroupMemberUpdated) group.events().get(3)).memberIds().contains(member.id()));

        group.queryMessages(member);
    }

    @Test
    @DisplayName("it should allow only joined member's roles to be updated")
    public void roleUpdateTest() {
        UserId joinedUserId = new UserId("joinedUser");
        UserId invitedUserId = new UserId("invitedUser");

        Set<GroupMember> members = new HashSet<GroupMember>() {{
            add(new GroupMember(joinedUserId, gid, GroupMember.State.JOINED, GroupRoles.ROLE_NAME_MEMBER, new Date()));
            add(new GroupMember(invitedUserId, gid, GroupMember.State.PENDING, GroupRoles.ROLE_NAME_MEMBER, new Date())); }};
        Group group = new Group(gid, validProfile(admin.id()), members, new Date());

        Assertions.assertEquals(2, group.members().size());
        Assertions.assertNotNull(group.member(joinedUserId));
        Assertions.assertNotNull(group.member(invitedUserId));
        Assertions.assertEquals(GroupMember.State.JOINED, group.member(joinedUserId).state());
        Assertions.assertEquals(GroupMember.State.PENDING, group.member(invitedUserId).state());
        Assertions.assertEquals(2, group.events().size());

        UserId moderator = new UserId("mod");
        group.add(admin, moderator, GroupRoles.ROLE_NAME_MEMBER, GroupMember.State.PENDING);
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MEMBER, group.member(moderator).role());
        Assertions.assertEquals(3, group.members().size());
        Assertions.assertEquals(3, group.events().size());
        Assertions.assertEquals(GroupMemberAdded.TYPE, group.events().get(2).type());
        Assertions.assertEquals(moderator, ((GroupMemberAdded) group.events().get(2)).memberId());
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MEMBER, ((GroupMemberAdded) group.events().get(2)).role());
        Assertions.assertEquals(GroupMember.State.PENDING, ((GroupMemberAdded) group.events().get(2)).state());
        Assertions.assertEquals(2, ((GroupMemberAdded) group.events().get(2)).memberIds().size());
        Assertions.assertTrue(((GroupMemberAdded) group.events().get(2)).memberIds().contains(joinedUserId));
        Assertions.assertTrue(((GroupMemberAdded) group.events().get(2)).memberIds().contains(moderator));
        Assertions.assertFalse(((GroupMemberAdded) group.events().get(2)).memberIds().contains(invitedUserId));

        Assertions.assertThrows(
                IllegalStateException.class,
                () -> group.changeMemberRole(admin, moderator, GroupRoles.ROLE_NAME_MEMBER)
        );

        group.changeMemberState(moderator, GroupMember.State.JOINED);
        Assertions.assertEquals(3, group.members().size());
        Assertions.assertNotNull(group.member(moderator));
        Assertions.assertEquals(GroupMember.State.JOINED, group.member(moderator).state());
        Assertions.assertEquals(4, group.events().size());
        Assertions.assertEquals(GroupMemberUpdated.TYPE, group.events().get(3).type());
        Assertions.assertEquals(moderator, ((GroupMemberUpdated) group.events().get(3)).memberId());
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MEMBER, ((GroupMemberUpdated) group.events().get(3)).role());
        Assertions.assertEquals(GroupMember.State.JOINED, ((GroupMemberUpdated) group.events().get(3)).state());
        Assertions.assertEquals(2, ((GroupMemberUpdated) group.events().get(3)).memberIds().size());
        Assertions.assertTrue(((GroupMemberUpdated) group.events().get(3)).memberIds().contains(joinedUserId));
        Assertions.assertTrue(((GroupMemberUpdated) group.events().get(3)).memberIds().contains(moderator));
        Assertions.assertFalse(((GroupMemberUpdated) group.events().get(3)).memberIds().contains(invitedUserId));

        group.changeMemberRole(admin, moderator, GroupRoles.ROLE_NAME_MODERATOR);
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MODERATOR, group.member(moderator).role());
        Assertions.assertEquals(3, group.members().size());
        Assertions.assertNotNull(group.member(moderator));
        Assertions.assertEquals(GroupMember.State.JOINED, group.member(moderator).state());
        Assertions.assertEquals(5, group.events().size());
        Assertions.assertEquals(GroupMemberUpdated.TYPE, group.events().get(4).type());
        Assertions.assertEquals(moderator, ((GroupMemberUpdated) group.events().get(4)).memberId());
        Assertions.assertEquals(GroupRoles.ROLE_NAME_MODERATOR, ((GroupMemberUpdated) group.events().get(4)).role());
        Assertions.assertEquals(GroupMember.State.JOINED, ((GroupMemberUpdated) group.events().get(4)).state());
        Assertions.assertEquals(2, ((GroupMemberUpdated) group.events().get(4)).memberIds().size());
        Assertions.assertTrue(((GroupMemberUpdated) group.events().get(4)).memberIds().contains(joinedUserId));
        Assertions.assertTrue(((GroupMemberUpdated) group.events().get(4)).memberIds().contains(moderator));
        Assertions.assertFalse(((GroupMemberUpdated) group.events().get(4)).memberIds().contains(invitedUserId));
    }

    @Test
    @DisplayName("it should not create a group with more than max allowed members")
    public void invalidCreateGroupMemberLimitTest() {
        UserId creator = new UserId("userA");
        Date now = new Date();
        GroupProfile profile = validProfile(creator);

        Set<GroupMember> members = new HashSet<>();
        for (int mi=0; mi < GROUP_MEMBER_MAX+1; mi++) {
            members.add(new GroupMember(new UserId("member"+(mi+1)), gid, GroupMember.State.JOINED, GroupRoles.ROLE_NAME_MEMBER, new Date()));
        }

        Assertions.assertThrows(
                IllegalStateException.class,
                () -> new Group(gid, profile, members, now),
                "max_member_limit_reached"
        );
    }

    @Test
    @DisplayName("it should not create a group with more than max allowed members")
    public void invalidAddGroupMemberLimitTest() {
        Date now = new Date();
        GroupProfile profile = validProfile(admin.id());

        Set<GroupMember> members = new HashSet<>();
        for (int mi=0; mi < GROUP_MEMBER_MAX-1; mi++) {
            members.add(new GroupMember(new UserId("member"+(mi+1)), gid, GroupMember.State.JOINED, GroupRoles.ROLE_NAME_MEMBER, new Date()));
        }

        Group group = new Group(gid, profile, members, now);
        group.add(admin, new UserId("member100"), GroupRoles.ROLE_NAME_MEMBER, GroupMember.State.JOINED);
        Assertions.assertThrows(
                IllegalStateException.class,
                () -> group.add(admin, new UserId("member101"), GroupRoles.ROLE_NAME_MEMBER, GroupMember.State.JOINED),
                "max_member_limit_reached"
        );
    }

    @Test
    @DisplayName("it should update member's last read message")
    public void updateMemberLastReadMessage() {
        UserId memberId = new UserId("userB");
        Group group = createValidGroupWithMember(new UserId("userA"), memberId, new RoleName("member"));
        GroupMember member = group.member(memberId);

        group.updateLastReadMessage(memberId, new ChannelMessageId(0L));
        Assertions.assertEquals(Long.valueOf(0L), member.lastReadMessageId());

        group.updateLastReadMessage(memberId, new ChannelMessageId(10L));
        Assertions.assertEquals(Long.valueOf(10L), member.lastReadMessageId());
    }

    @Test
    @DisplayName("it should not update last read message for non-members")
    public void nonMemberUpdateLastReadMessage() {
        Group group = createValidGroup(new UserId("userA"));
        Assertions.assertThrows(
                IllegalStateException.class,
                () -> group.updateLastReadMessage(new UserId("nonMember"), new ChannelMessageId(0L))
        );
    }

    @Test
    @DisplayName("it should not update last read message for invalid member")
    public void invalidMemeberUpdateLastReadMessage() {
        Group group = createValidGroupWithMember(
                new UserId("userA"), new UserId("userB"), new RoleName("member")
        );
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> group.updateLastReadMessage(null, new ChannelMessageId(0L))
        );
    }

    private Group createValidGroup(final UserId aCreator) {
        return new Group(gid, validProfile(aCreator),null, new Date());
    }

    private Group createValidGroupWithMember(final UserId aCreator,
                                             final UserId aMemberId,
                                             final RoleName aMemberRole) {
        Set<GroupMember> members = new HashSet<GroupMember>() {
            {
                add(new GroupMember(aMemberId, gid, GroupMember.State.JOINED, aMemberRole, new Date()));
            }
        };
        return new Group(gid, validProfile(aCreator), members, new Date());
    }

    private GroupProfile validProfile(final UserId aCreator) {
        return new GroupProfile(aCreator, validTitle, validImageURL, validDescription, validType);
    }
}
