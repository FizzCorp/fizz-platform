package io.fizz.gateway.http.controllers.group;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.GroupApi;
import io.swagger.client.api.UserApi;
import io.swagger.client.model.*;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.util.*;

@ExtendWith(VertxExtension.class)
public class UserGroupControllerTest extends BaseGroupControllerTest {
    @Test
    @DisplayName("it should add all joined groups to a user's group list")
    void groupListTest() throws ApiException {
        final String user1 = UUID.randomUUID().toString();
        final String sessionToken1 = createSessionToken(user1, APP_ID, LanguageCode.EN);
        final String user2 = UUID.randomUUID().toString();
        final Group group1 = createGroup();
        final Group group2 = createGroup();
        final Group group3 = createGroup();

        final Set<String> groupIds = new HashSet<>();
        groupIds.add(group1.getId());
        groupIds.add(group2.getId());
        groupIds.add(group3.getId());

        addMember(group1.getId(), user1, GroupRoleName.MEMBER, GroupMemberState.PENDING);
        addMember(group1.getId(), user2, GroupRoleName.MEMBER, GroupMemberState.PENDING);
        addMember(group2.getId(), user1, GroupRoleName.MEMBER, GroupMemberState.PENDING);
        addMember(group2.getId(), user2, GroupRoleName.MEMBER, GroupMemberState.PENDING);
        addMember(group3.getId(), user1, GroupRoleName.MEMBER, GroupMemberState.PENDING);
        addMember(group3.getId(), user2, GroupRoleName.MEMBER, GroupMemberState.PENDING);

        final UserApi api = new UserApi(new ApiClient());
        api.getApiClient().setApiKey(sessionToken1);
        final QueryUserGroupsResponse response = api.fetchUserGroups(user1, null, null);

        Assertions.assertEquals(3, response.getGroups().size());
        Assertions.assertNull(response.getLinks().getNext());
        Assertions.assertTrue(groupIds.contains(response.getGroups().get(0).getGroupId()));
        Assertions.assertTrue(groupIds.contains(response.getGroups().get(1).getGroupId()));
        Assertions.assertTrue(groupIds.contains(response.getGroups().get(2).getGroupId()));
    }

    @Test
    @DisplayName("it should be possible to query pages of a user's groups")
    void groupListPagingTest() throws ApiException {
        final String user = UUID.randomUUID().toString();
        final String sessionToken = createSessionToken(user, APP_ID, LanguageCode.EN);
        final Group group1 = createGroup();
        final Group group2 = createGroup();
        final Group group3 = createGroup();

        addMember(group1.getId(), user, GroupRoleName.MEMBER, GroupMemberState.PENDING);
        addMember(group2.getId(), user, GroupRoleName.MEMBER, GroupMemberState.PENDING);
        addMember(group3.getId(), user, GroupRoleName.MEMBER, GroupMemberState.PENDING);

        final UserApi api = new UserApi(new ApiClient());
        Set<String> groupIds = new HashSet<>();

        api.getApiClient().setApiKey(sessionToken);

        QueryUserGroupsResponse response = api.fetchUserGroups(user, null, 1);
        Assertions.assertEquals(1, response.getGroups().size());
        Assertions.assertNotNull(response.getLinks().getNext());
        groupIds.add(response.getGroups().get(0).getGroupId());

        response = api.fetchUserGroups(user, response.getLinks().getNext(), 2);
        Assertions.assertEquals(2, response.getGroups().size());
        Assertions.assertNull(response.getLinks().getNext());
        groupIds.add(response.getGroups().get(0).getGroupId());
        groupIds.add(response.getGroups().get(1).getGroupId());

        Assertions.assertEquals(3, groupIds.size());
    }

    @Test
    @DisplayName("it should be possible for a user to join the specified group")
    void joinGroupTest() throws ApiException {
        final String user = UUID.randomUUID().toString();
        final String sessionToken = createSessionToken(user, APP_ID, LanguageCode.EN);
        final Group group1 = createGroup();
        final Group group2 = createGroup();
        final Group group3 = createGroup();

        addMember(group1.getId(), user, GroupRoleName.MEMBER, GroupMemberState.PENDING);
        addMember(group2.getId(), user, GroupRoleName.MEMBER, GroupMemberState.PENDING);
        addMember(group3.getId(), user, GroupRoleName.MEMBER, GroupMemberState.PENDING);

        final UserApi api = new UserApi(new ApiClient());

        api.getApiClient().setApiKey(sessionToken);
        final UpdateUserGroupRequest updateRequest = new UpdateUserGroupRequest();
        updateRequest.setState(GroupMemberState.JOINED);
        api.updateGroup(user, group2.getId(), updateRequest);

        QueryUserGroupsResponse response = api.fetchUserGroups(user, null, 3);
        List<UserGroup> groups = response.getGroups();
        int validations = 0;

        for (UserGroup group: groups) {
            if (group.getGroupId().equals(group1.getId()) || group.getGroupId().equals(group3.getId())) {
                validations++;
                Assertions.assertEquals(GroupMemberState.PENDING, group.getState());
            }
            else
            if (group.getGroupId().equals(group2.getId())) {
                validations++;
                Assertions.assertEquals(GroupMemberState.JOINED, group.getState());
            }
        }

        Assertions.assertEquals(validations, 3);
    }

    @Test
    @DisplayName("it should be possible for a user to delete groups")
    void removeGroupTest() throws ApiException {
        try {
            final String user = UUID.randomUUID().toString();
            final String sessionToken = createSessionToken(user, APP_ID, LanguageCode.EN);
            final Group group1 = createGroup();
            final Group group2 = createGroup();
            final Group group3 = createGroup();

            addMember(group1.getId(), user, GroupRoleName.MEMBER, GroupMemberState.PENDING);
            addMember(group2.getId(), user, GroupRoleName.MEMBER, GroupMemberState.PENDING);
            addMember(group3.getId(), user, GroupRoleName.MEMBER, GroupMemberState.PENDING);

            final UserApi api = new UserApi(new ApiClient());

            api.getApiClient().setApiKey(sessionToken);
            final UpdateUserGroupRequest updateRequest = new UpdateUserGroupRequest();
            updateRequest.setState(GroupMemberState.JOINED);
            api.updateGroup(user, group3.getId(), updateRequest);
            api.removeGroup(user, group2.getId());
            api.removeGroup(user, group3.getId());

            QueryUserGroupsResponse response = api.fetchUserGroups(user, null, 3);
            Assertions.assertEquals(1, response.getGroups().size());
            Assertions.assertNull(response.getLinks().getNext());
        }
        catch (ApiException ex) {
            System.out.println(ex.getResponseBody());
            throw ex;
        }
    }

    @Test
    @DisplayName("it should allow a member to update their last read message id")
    public void updateLastReadMessageId() throws Throwable {
        final Group group = createGroup();
        final String userId = USER_ID_3;
        final String token = sessionToken3;

        // add member
        final GroupApi api = new GroupApi(new ApiClient());
        final GroupMemberRequest req = new GroupMemberRequest();
        req.setId(userId);
        req.setRole(GroupRoleName.MEMBER);
        req.setState(GroupMemberState.JOINED);
        api.getApiClient().setApiKey(adminToken);
        api.addGroupMembers(group.getId(), Collections.singletonList(req));

        final UserApi user = new UserApi(new ApiClient());
        user.getApiClient().setApiKey(token);

        // validate that initial value of last read message is null
        QueryUserGroupsResponse response = user.fetchUserGroups(userId, null, 100);
        Assertions.assertEquals(1, response.getGroups().size());
        UserGroup userGroup = response.getGroups().get(0);
        Assertions.assertEquals(group.getId(), userGroup.getGroupId());
        Assertions.assertNull(response.getGroups().get(0).getLastReadMessageId());

        Group group2 = api.fetchGroup(group.getId());
        Assertions.assertEquals(group.getId(), group2.getId());

        // update last read message
        final UpdateUserGroupRequest updateRequest = new UpdateUserGroupRequest();
        updateRequest.setLastReadMessageId(BigDecimal.valueOf(10L));
        user.updateGroup(userId, group.getId(), updateRequest);

        // validate updated value of last read message
         response = user.fetchUserGroups(userId, null, 100);
        Assertions.assertEquals(1, response.getGroups().size());
        Assertions.assertEquals(BigDecimal.valueOf(10L), response.getGroups().get(0).getLastReadMessageId());
    }

    @Test
    @DisplayName("it should not allow a non-member to update their last read message id")
    public void updateNonMemberLastReadMessageId() throws Throwable {
        final Group group = createGroup();

        // update last read message
        final UserApi user = new UserApi(new ApiClient());
        user.getApiClient().setApiKey(sessionToken1);

        final UpdateUserGroupRequest updateRequest = new UpdateUserGroupRequest();
        updateRequest.setLastReadMessageId(BigDecimal.valueOf(10L));
        Assertions.assertThrows(
                ApiException.class,
                () -> user.updateGroup(group.getId(), USER_ID_1, updateRequest)
        );
    }
}
