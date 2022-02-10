package io.fizz.chat.group.domain.group;

import io.fizz.chat.domain.Permissions;
import io.fizz.chat.domain.channel.ChannelMessageId;
import io.fizz.chat.group.domain.user.User;
import io.fizz.chat.group.infrastructure.ConfigService;
import io.fizz.chataccess.domain.Authorization;
import io.fizz.chataccess.domain.role.Role;
import io.fizz.chataccess.domain.role.RoleName;
import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.chatcommon.domain.DomainAggregate;
import io.fizz.chatcommon.domain.EventsOffset;
import io.fizz.chatcommon.domain.MutationId;
import io.fizz.common.Utils;
import io.fizz.common.domain.UserId;

import java.util.*;

public class Group extends DomainAggregate {

    private static final int GROUP_MEMBER_MAX = ConfigService.config().getNumber("chat.group.member.max").intValue();

    private static final Map<RoleName, Role> GROUP_ROLES = new HashMap<RoleName, Role>() {
        {
            put(GroupRoles.ROLE_NAME_OWNER, new Role(GroupRoles.ROLE_NAME_OWNER, 2000, GroupRoles.permissionsOwner()));
            put(GroupRoles.ROLE_NAME_MODERATOR, new Role(GroupRoles.ROLE_NAME_MODERATOR, 3000, GroupRoles.permissionsMod()));
            put(GroupRoles.ROLE_NAME_MEMBER, new Role(GroupRoles.ROLE_NAME_MEMBER, 6000, GroupRoles.permissionsMember()));
        }
    };

    private final GroupId id;
    private final GroupProfile profile;
    private final Map<UserId, GroupMember> members;
    private final Role defaultRole;
    private final Date createdOn;
    private Date updatedOn;

    public Group(final GroupId aId,
                 final GroupProfile aProfile,
                 final Set<GroupMember> aMembers,
                 final Date aCreatedOn) {
        this(
            aId,
            aProfile,
            null,
            aCreatedOn,
            aCreatedOn,
            new MutationId(),
            new EventsOffset()
        );

        if (Objects.nonNull(aMembers)) {
            for (final GroupMember member : aMembers) {
                add(member);
            }
        }
    }

    public Group(final GroupId aId,
                 final GroupProfile aProfile,
                 final Set<GroupMember> aMembers,
                 final Date aCreatedOn,
                 final Date aUpdatedOn,
                 final MutationId aMutationId,
                 final EventsOffset aEventsOffset) {
        super(aMutationId, aEventsOffset);

        Utils.assertRequiredArgument(aId, "invalid_group_id");
        Utils.assertRequiredArgument(aProfile, "invalid_group_profile");
        Utils.assertRequiredArgument(aCreatedOn, "invalid_created_on");

        this.id = aId;
        this.createdOn = aCreatedOn;
        this.updatedOn = aUpdatedOn;
        this.profile = aProfile;
        this.defaultRole = new Role(new RoleName("outcast"), 8000, GroupPermissions.ACCEPT_MEMBERSHIP.value());

        this.members = new HashMap<>();
        if (Objects.nonNull(aMembers)) {
            for (GroupMember member : aMembers) {
                Utils.assertRequiredArgument(member, "invalid_group_member");

                members.put(member.userId(), member);
            }
        }
    }

    public GroupId id() {
        return id;
    }

    public String title() {
        return profile.title();
    }

    public String imageURL() {
        return profile.imageURL();
    }

    public String description() {
        return profile.description();
    }

    public String type() {
        return profile.type();
    }

    public UserId createdBy() {
        return profile.createdBy();
    }

    public GroupProfile profile() {
        return profile;
    }

    public Set<GroupMember> members() {
        return new HashSet<>(members.values());
    }

    public Date createdOn() {
        return createdOn;
    }

    public Date updatedOn() {
        return updatedOn;
    }

    public ChannelId channelId() {
        return new ChannelId(id.appId(), "grp." + id.value());
    }

    public void updateProfile(final User aOperator,
                              final String aNewTitle,
                              final String aNewImageURL,
                              final String aNewDescription,
                              final String aNewType) {
        Utils.assertRequiredArgument(aOperator, "invalid_operator_id");

        final Role operatorRole = effective(aOperator, true);
        if (!operatorRole.has(GroupPermissions.EDIT_GROUP_PROFILE.value())) {
            throw new SecurityException("not_permitted");
        }

        profile.update(aNewTitle, aNewImageURL, aNewDescription, aNewType);
        updatedOn = new Date();
        emit(new GroupProfileUpdated(id, joinedMemberIds(), title(), imageURL(), description(), type(), updatedOn));
    }

    public void add(final User aOperator,
                    final UserId aMemberId,
                    final RoleName aMemberRoleName,
                    final GroupMember.State aState) {
        Utils.assertRequiredArgument(aOperator, "invalid_operator");
        Utils.assertRequiredArgument(aMemberId, "invalid_member_id");
        Utils.assertRequiredArgument(aMemberRoleName, "invalid_member_role");
        Utils.assertRequiredArgument(aState, "invalid_member_state");

        if (members.containsKey(aMemberId)) {
            throw new IllegalStateException("already_member");
        }

        final Role memberRole = role(aMemberRoleName);
        if (Objects.isNull(memberRole)) {
            throw new IllegalArgumentException("invalid_role_name");
        }

        final Role operatorRole = effective(aOperator, true);
        if (aState == GroupMember.State.PENDING && !operatorRole.has(GroupPermissions.INVITE_MEMBERS.value())) {
            throw new SecurityException("not_permitted");
        }
        if (aState == GroupMember.State.JOINED && !operatorRole.has(GroupPermissions.ADD_MEMBERS.value())) {
            throw new SecurityException("not_permitted");
        }

        if (!operatorRole.isRankedHigher(memberRole)) {
            throw new SecurityException("not_permitted");
        }

        final GroupMember newMember = new GroupMember(aMemberId, id, aState, aMemberRoleName, new Date());
        add(newMember);
    }

    public void remove(final User aOperator, final UserId aMemberId) {
        Utils.assertRequiredArgument(aOperator, "invalid_operator");
        Utils.assertRequiredArgument(aMemberId, "invalid_member_id");

        final boolean requireJoined = !aOperator.id().equals(aMemberId);
        final Role operatorRole = effective(aOperator, requireJoined);
        final GroupMember member = validateAndGetMember(aMemberId);
        final Role memberRole = effective(role(member.role()), defaultRole);
        final Authorization auth = new Authorization(aMemberId, memberRole, aOperator.id(), operatorRole);

        auth.assertOperateNotOwned(GroupPermissions.REMOVE_ANY_MEMBER.value())
                .assertOperateOwned(GroupPermissions.REMOVE_SELF.value())
                .validate();

        final Set<UserId> memberIds = joinedMemberIds();
        memberIds.add(aMemberId);

        members.remove(aMemberId);
        updatedOn = new Date();
        emit(new GroupMemberRemoved(member, memberIds, new Date()));
    }

    public void changeMemberState(final UserId aMemberId, final GroupMember.State aNewState) {
        Utils.assertRequiredArgument(aMemberId, "invalid_member_id");

        if (aNewState != GroupMember.State.JOINED) {
            throw new IllegalArgumentException("can_only_join");
        }

        final GroupMember member = validateAndGetMember(aMemberId);
        if (member.state() == GroupMember.State.JOINED) {
            throw new IllegalStateException("already_joined");
        }

        final Role memberRole = effective(role(member.role()), defaultRole);
        final Authorization auth = new Authorization(aMemberId, memberRole, aMemberId, memberRole);
        auth.assertOperateOwned(GroupPermissions.ACCEPT_MEMBERSHIP.value()).validate();

        member.setState(GroupMember.State.JOINED);
        updatedOn = new Date();
        emit(new GroupMemberUpdated(member, joinedMemberIds(), new Date()));
    }

    public void changeMemberRole(final User aOperator, final UserId aMemberId, final RoleName aNewRole) {
        Utils.assertRequiredArgument(aOperator, "invalid_operator");
        Utils.assertRequiredArgument(aMemberId, "invalid_member_id");
        Utils.assertRequiredArgument(aNewRole, "invalid_new_role");

        final Role operatorRole = effective(aOperator, true);
        final GroupMember member = validateAndGetJoinedMember(aMemberId);
        final Role memberRole = effective(role(member.role()), defaultRole);
        if (!operatorRole.has(GroupPermissions.EDIT_MEMBER_ROLES.value())) {
            throw new SecurityException("not_permitted");
        }

        if (memberRole.isRankedHigher(operatorRole)) {
            throw new SecurityException("not_permitted");
        }

        final Role targetRole = role(aNewRole);
        if (targetRole.isRankedHigher(operatorRole)) {
            throw new SecurityException("not_permitted");
        }

        member.setRole(aNewRole);
        updatedOn = new Date();
        emit(new GroupMemberUpdated(member, joinedMemberIds(), new Date()));
    }

    public void updateLastReadMessage(final UserId aMemberId, final ChannelMessageId aLastReadMessageId) {
        Utils.assertRequiredArgument(aMemberId, "invalid_operator_id");

        final GroupMember member = validateAndGetJoinedMember(aMemberId);

        member.setLastReadMessageId(aLastReadMessageId);

        emit(new GroupMemberReadMessages(member, joinedMemberIds(), new Date()));
    }

    public void publishMessage(final User aAuthor) {
        Utils.assertRequiredArgument(aAuthor, "invalid_author");

        final Role effectiveRole = effective(aAuthor, true);
        if (!effectiveRole.has(Permissions.PUBLISH_MESSAGES.value())) {
            throw new SecurityException("not_permitted");
        }
    }

    public void queryMessages(final User aRequester) {
        Utils.assertRequiredArgument(aRequester, "invalid_requester");

        final Role effectiveRole = effective(aRequester, true);
        if (!effectiveRole.has(Permissions.READ_MESSAGES.value())) {
            throw new SecurityException("not_permitted");
        }
    }

    public void canManageMutes(final User aOperator, final User aMember) {
        Utils.assertRequiredArgument(aOperator, "invalid_operator");
        Utils.assertRequiredArgument(aMember, "invalid_member_id");

        final Role operatorRole = effective(aOperator, true);
        final GroupMember member = validateAndGetJoinedMember(aMember.id());
        final Role memberRole = effective(aMember.role(), role(member.role()), defaultRole);
        final Authorization auth = new Authorization(aMember.id(), memberRole, aOperator.id(), operatorRole);

        auth.assertOperateNotOwned(Permissions.MANAGE_MUTES.value())
                .validate();
    }

    GroupMember member(final UserId aMemberId) {
        return members.get(aMemberId);
    }

    private GroupMember validateAndGetMember(final UserId aMemberId) {
        GroupMember member = members.get(aMemberId);

        if (Objects.isNull(member)) {
            throw new SecurityException("not_a_member");
        }

        return member;
    }

    private GroupMember validateAndGetJoinedMember(final UserId aMemberId) {
        GroupMember member = joinedMember(aMemberId);

        if (Objects.isNull(member)) {
            throw new IllegalStateException("not_a_member");
        }

        return member;
    }

    private Role effective(User aUser, boolean aRequireJoined) {
        GroupMember member = aRequireJoined ? joinedMember(aUser.id()) : members.get(aUser.id());

        return effective(role(member), aUser.role(), defaultRole);
    }

    private Role effective(Role ...aRoles) {
        Role effectiveRole = Role.compose(aRoles);
        if (Objects.isNull(effectiveRole)) {
            throw new SecurityException("not_permitted");
        }

        return effectiveRole;
    }

    private Role role(final GroupMember aMember) {
        return Objects.nonNull(aMember) ? role(aMember.role()) : null;
    }

    private Role role(final RoleName aRoleName) {
        return GROUP_ROLES.get(aRoleName);
    }

    private GroupMember joinedMember(final UserId aMemberId) {
        final GroupMember member = members.get(aMemberId);
        if (Objects.isNull(member)) {
            return null;
        }

        return member.state() == GroupMember.State.PENDING ? null : member;
    }

    private Set<UserId> joinedMemberIds() {
        final Set<UserId> ids = new HashSet<>();

        for (GroupMember member: members.values()) {
            if (member.state() == GroupMember.State.JOINED) {
                ids.add(member.userId());
            }
        }

        return ids;
    }

    private void add(final GroupMember newMember) {
        if (members.size() == GROUP_MEMBER_MAX) {
            throw new IllegalStateException("max_member_limit_reached");
        }
        members.put(newMember.userId(), newMember);
        updatedOn = new Date();

        Set<UserId> joinedMembers = joinedMemberIds();
        joinedMembers.add(newMember.userId());

        emit(new GroupMemberAdded(newMember, joinedMembers, new Date()));
    }
}
