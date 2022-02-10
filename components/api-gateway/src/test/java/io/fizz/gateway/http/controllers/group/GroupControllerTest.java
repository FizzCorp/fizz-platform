package io.fizz.gateway.http.controllers.group;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.GroupApi;
import io.swagger.client.model.*;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
public class GroupControllerTest extends BaseGroupControllerTest {
    @Test
    @DisplayName("it should only allow an admin to create a group")
    public void createAdminGroupTest() throws Throwable {
        final GroupApi api = new GroupApi(new ApiClient());
        final String title = "myGroup";
        final String imageURL = "title.com";
        final String description = "my group for communicating!";
        final String type = "myType";

        final CreateGroupRequest request = new CreateGroupRequest();
        request.setTitle(title);
        request.setImageUrl(imageURL);
        request.setDescription(description);
        request.setType(type);

        api.getApiClient().setApiKey(adminToken);
        final Group group = api.createGroup(request);
        Assertions.assertNotNull(group.getId());
        Assertions.assertNotNull(group.getChannelId());
        Assertions.assertEquals(USER_ADMIN, group.getCreatedBy());
        Assertions.assertEquals(title, group.getTitle());
        Assertions.assertEquals(imageURL, group.getImageUrl());
        Assertions.assertEquals(description, group.getDescription());
        Assertions.assertEquals(type, group.getType());

        final Group fetchedGroup = api.fetchGroup(group.getId());
        Assertions.assertEquals(group.getId(), fetchedGroup.getId());
    }

    @Test
    @DisplayName("it should not allow a user to create a group")
    public void createUserGroupTest() {
        final GroupApi api = new GroupApi(new ApiClient());
        final String title = "myGroup";
        final String imageURL = "title.com";
        final String description = "my group for communicating!";
        final String type = "myType";

        final CreateGroupRequest request = new CreateGroupRequest();
        request.setTitle(title);
        request.setImageUrl(imageURL);
        request.setDescription(description);
        request.setType(type);

        api.getApiClient().setApiKey(sessionToken1);
        Assertions.assertThrows(ApiException.class, () -> api.createGroup(request));
    }

    @Test
    @DisplayName("it should allow an admin to update a group")
    public void updateGroupTest() throws ApiException {
        final GroupApi api = new GroupApi(new ApiClient());
        final String newTitle = "newTitle";
        final String newImageURL = "newImageURL";
        final String newDescription = "newDescription";
        final String newType = "newType";

        final Group group = createGroup();
        final UpdateGroupRequest req = new UpdateGroupRequest()
                .title(newTitle)
                .imageUrl(newImageURL)
                .description(newDescription)
                .type(newType);

        api.getApiClient().setApiKey(adminToken);
        api.updateGroup(group.getId(), req);

        Group fetchedGroup = api.fetchGroup(group.getId());
        Assertions.assertEquals(newTitle, fetchedGroup.getTitle());
        Assertions.assertEquals(newImageURL, fetchedGroup.getImageUrl());
        Assertions.assertEquals(newDescription, fetchedGroup.getDescription());
        Assertions.assertEquals(newType, fetchedGroup.getType());

        api.getApiClient().setApiKey(sessionToken1);
        Assertions.assertThrows(ApiException.class, () -> api.updateGroup(group.getId(), req));
    }

    @Test
    @DisplayName("it should allow an admin to manage members of a group")
    public void adminManageMembersTest() throws ApiException {
        final GroupApi api = new GroupApi(new ApiClient());
        final List<GroupMemberRequest> members = new ArrayList<>();

        final GroupMemberRequest user1Req = new GroupMemberRequest();
        user1Req.setId(USER_ID_1);
        user1Req.setRole(GroupRoleName.MEMBER);
        user1Req.setState(GroupMemberState.PENDING);
        members.add(user1Req);

        final GroupMemberRequest user2Req = new GroupMemberRequest();
        user2Req.setId(USER_ID_2);
        user2Req.setRole(GroupRoleName.MODERATOR);
        user2Req.setState(GroupMemberState.JOINED);
        members.add(user2Req);

        api.getApiClient().setApiKey(adminToken);
        final Group group = createGroup();
        api.addGroupMembers(group.getId(), members);

        FetchGroupMembersResponse response = api.fetchGroupMembers(group.getId());
        Assertions.assertEquals(2, response.size());

        api.removeGroupMember(group.getId(), USER_ID_1);

        response = api.fetchGroupMembers(group.getId());
        Assertions.assertEquals(1, response.size());
    }

    @Test
    @DisplayName("it should allow an admin to manage mutes of a group")
    public void adminManageMutesTest() throws Exception {
        final GroupApi api = new GroupApi(new ApiClient());
        final List<GroupMemberRequest> members = new ArrayList<>();

        final GroupMemberRequest user1Req = new GroupMemberRequest();
        user1Req.setId(USER_ID_1);
        user1Req.setRole(GroupRoleName.MEMBER);
        user1Req.setState(GroupMemberState.JOINED);
        members.add(user1Req);

        final GroupMemberRequest user2Req = new GroupMemberRequest();
        user2Req.setId(USER_ID_2);
        user2Req.setRole(GroupRoleName.MODERATOR);
        user2Req.setState(GroupMemberState.JOINED);
        members.add(user2Req);

        api.getApiClient().setApiKey(adminToken);
        final Group group = createGroup();
        api.addGroupMembers(group.getId(), members);

        api.getApiClient().setApiKey(sessionToken1);
        ChannelMessageModel message1 = buildMessage();
        api.publishGroupMessage(group.getId(), message1);

        // mute user
        MuteGroupMemberRequest muteMemberRequest = new MuteGroupMemberRequest();
        muteMemberRequest.setId(USER_ID_1);
        muteMemberRequest.setDuration(new BigDecimal(60));
        api.getApiClient().setApiKey(adminToken);
        api.muteGroupMember(group.getId(), muteMemberRequest);

        // validate ops after mute
        api.getApiClient().setApiKey(sessionToken1);
        List<ChannelMessage> messages = api.queryGroupMessages(group.getId(), 5, null, null);
        Assertions.assertEquals(messages.size(), 1);
        validateErrorResponse(() -> api.publishGroupMessage(group.getId(), message1));

        // unmute user
        api.getApiClient().setApiKey(adminToken);
        api.unmuteGroupMember(group.getId(), USER_ID_1);

        // validate ops after unmute
        api.getApiClient().setApiKey(sessionToken1);
        api.publishGroupMessage(group.getId(), message1);
    }

    @Test
    @DisplayName("it should allow a moderator to manage mutes of a group")
    public void moderatorManageMutesTest() throws Exception {
        final GroupApi api = new GroupApi(new ApiClient());
        final List<GroupMemberRequest> members = new ArrayList<>();

        final GroupMemberRequest user1Req = new GroupMemberRequest();
        user1Req.setId(USER_ID_1);
        user1Req.setRole(GroupRoleName.MEMBER);
        user1Req.setState(GroupMemberState.JOINED);
        members.add(user1Req);

        final GroupMemberRequest user2Req = new GroupMemberRequest();
        user2Req.setId(USER_ID_2);
        user2Req.setRole(GroupRoleName.MODERATOR);
        user2Req.setState(GroupMemberState.JOINED);
        members.add(user2Req);

        api.getApiClient().setApiKey(adminToken);
        final Group group = createGroup();
        api.addGroupMembers(group.getId(), members);

        api.getApiClient().setApiKey(sessionToken1);
        ChannelMessageModel message1 = buildMessage();
        api.publishGroupMessage(group.getId(), message1);

        // mute user
        MuteGroupMemberRequest muteMemberRequest = new MuteGroupMemberRequest();
        muteMemberRequest.setId(USER_ID_1);
        muteMemberRequest.setDuration(new BigDecimal(60));
        api.getApiClient().setApiKey(sessionToken2);
        api.muteGroupMember(group.getId(), muteMemberRequest);

        // validate ops after mute
        api.getApiClient().setApiKey(sessionToken1);
        List<ChannelMessage> messages = api.queryGroupMessages(group.getId(), 5, null, null);
        Assertions.assertEquals(messages.size(), 1);
        validateErrorResponse(() -> api.publishGroupMessage(group.getId(), message1));

        // unmute user
        api.getApiClient().setApiKey(sessionToken2);
        api.unmuteGroupMember(group.getId(), USER_ID_1);

        // validate ops after unmute
        api.getApiClient().setApiKey(sessionToken1);
        api.publishGroupMessage(group.getId(), message1);
    }

    @Test
    @DisplayName("it should not allow a member to manage mutes of a group")
    public void memberShouldNotManageMutesTest() throws Exception {
        final GroupApi api = new GroupApi(new ApiClient());
        final List<GroupMemberRequest> members = new ArrayList<>();

        final GroupMemberRequest user1Req = new GroupMemberRequest();
        user1Req.setId(USER_ID_1);
        user1Req.setRole(GroupRoleName.MEMBER);
        user1Req.setState(GroupMemberState.JOINED);
        members.add(user1Req);

        final GroupMemberRequest user2Req = new GroupMemberRequest();
        user2Req.setId(USER_ID_2);
        user2Req.setRole(GroupRoleName.MODERATOR);
        user2Req.setState(GroupMemberState.JOINED);
        members.add(user2Req);

        api.getApiClient().setApiKey(adminToken);
        final Group group = createGroup();
        api.addGroupMembers(group.getId(), members);

        api.getApiClient().setApiKey(sessionToken2);
        ChannelMessageModel message1 = buildMessage();
        api.publishGroupMessage(group.getId(), message1);

        // mute user
        MuteGroupMemberRequest muteMemberRequest = new MuteGroupMemberRequest();
        muteMemberRequest.setId(USER_ID_2);
        muteMemberRequest.setDuration(new BigDecimal(60));
        api.getApiClient().setApiKey(sessionToken1);
        validateErrorResponse(() -> api.muteGroupMember(group.getId(), muteMemberRequest));

        // validate ops after mute
        api.getApiClient().setApiKey(sessionToken2);
        List<ChannelMessage> messages = api.queryGroupMessages(group.getId(), 5, null, null);
        Assertions.assertEquals(messages.size(), 1);
        api.publishGroupMessage(group.getId(), message1);

        // unmute user
        api.getApiClient().setApiKey(sessionToken1);
        validateErrorResponse(() -> api.unmuteGroupMember(group.getId(), USER_ID_2));
    }

    @Test
    @DisplayName("it should allow a group member to publish message whose mute has expired")
    void timedMuteGroupMemberTest() throws Exception {
        final GroupApi api = new GroupApi(new ApiClient());
        final List<GroupMemberRequest> members = new ArrayList<>();

        final GroupMemberRequest user1Req = new GroupMemberRequest();
        user1Req.setId(USER_ID_1);
        user1Req.setRole(GroupRoleName.MEMBER);
        user1Req.setState(GroupMemberState.JOINED);
        members.add(user1Req);

        final GroupMemberRequest user2Req = new GroupMemberRequest();
        user2Req.setId(USER_ID_2);
        user2Req.setRole(GroupRoleName.MODERATOR);
        user2Req.setState(GroupMemberState.JOINED);
        members.add(user2Req);

        api.getApiClient().setApiKey(adminToken);
        final Group group = createGroup();
        api.addGroupMembers(group.getId(), members);

        api.getApiClient().setApiKey(sessionToken1);
        ChannelMessageModel message1 = buildMessage();
        api.publishGroupMessage(group.getId(), message1);

        // mute user
        MuteGroupMemberRequest muteMemberRequest = new MuteGroupMemberRequest();
        muteMemberRequest.setId(USER_ID_1);
        muteMemberRequest.setDuration(new BigDecimal(1));
        api.getApiClient().setApiKey(adminToken);
        api.muteGroupMember(group.getId(), muteMemberRequest);

        // validate ops after mute
        api.getApiClient().setApiKey(sessionToken1);
        List<ChannelMessage> messages = api.queryGroupMessages(group.getId(), 5, null, null);
        Assertions.assertEquals(messages.size(), 1);
        validateErrorResponse(() -> api.publishGroupMessage(group.getId(), message1));

        // wait for mute to expire
        TimeUnit.SECONDS.sleep(2);

        // validate ops after unmute
        api.getApiClient().setApiKey(sessionToken1);
        api.publishGroupMessage(group.getId(), message1);
    }

    @Test
    @DisplayName("it should not allow a user to manage members of a group")
    public void userAddMembersTest() throws ApiException {
        final GroupApi api = new GroupApi(new ApiClient());
        final List<GroupMemberRequest> members = new ArrayList<>();

        final GroupMemberRequest user1Req = new GroupMemberRequest();
        user1Req.setId(USER_ID_1);
        user1Req.setRole(GroupRoleName.MEMBER);
        members.add(user1Req);

        final Group group = createGroup();
        api.getApiClient().setApiKey(sessionToken2);
        Assertions.assertThrows(ApiException.class, () -> api.addGroupMembers(group.getId(), members));
        Assertions.assertThrows(ApiException.class, () -> api.removeGroupMember(group.getId(), USER_ID_1));
    }

    @Test
    @DisplayName("it should allow an admin to update a member's role")
    public void updateMemberRoleTest() throws ApiException {
        final GroupApi api = new GroupApi(new ApiClient());
        final List<GroupMemberRequest> members = new ArrayList<>();

        final GroupMemberRequest user1Req = new GroupMemberRequest();
        user1Req.setId(USER_ID_1);
        user1Req.setRole(GroupRoleName.MEMBER);
        user1Req.setState(GroupMemberState.JOINED);
        members.add(user1Req);

        api.getApiClient().setApiKey(adminToken);
        final Group group = createGroup();
        api.addGroupMembers(group.getId(), members);

        FetchGroupMembersResponse response = api.fetchGroupMembers(group.getId());
        Assertions.assertEquals(1, response.size());

        api.updateGroupMember(group.getId(), USER_ID_1, new UpdateGroupMemberRequest().role(GroupRoleName.MODERATOR));

        response = api.fetchGroupMembers(group.getId());
        Assertions.assertEquals(1, response.size());
        Assertions.assertEquals(GroupRoleName.MODERATOR, response.get(0).getRole());

        api.getApiClient().setApiKey(sessionToken2);
        Assertions.assertThrows(
                ApiException.class,
                () -> api.updateGroupMember(
                    group.getId(), USER_ID_1, new UpdateGroupMemberRequest().role(GroupRoleName.MODERATOR)
                )
        );
    }

    @Test
    @DisplayName("it should allow an admin to publish a message in a group")
    public void adminMessagePublishTest() throws ApiException {
        final GroupApi api = new GroupApi(new ApiClient());
        final String message1 = "hello1";
        final String message2 = "hello2";
        final String message3 = "hello3";
        final Group group = createGroup();

        final ChannelMessageModel request = new ChannelMessageModel().body(message1).filter(false);

        api.getApiClient().setApiKey(adminToken);
        api.publishGroupMessage(group.getId(), request);
        api.publishGroupMessage(group.getId(), request.body(message2));
        api.publishGroupMessage(group.getId(), request.body(message3));

        QueryGroupMessagesResponse res = api.queryGroupMessages(group.getId(), 10, null, null);
        Assertions.assertEquals(3, res.size());
        Assertions.assertEquals(message1, res.get(0).getBody());
        Assertions.assertEquals(message2, res.get(1).getBody());
        Assertions.assertEquals(message3, res.get(2).getBody());

        QueryGroupMessagesResponse res2 = api.queryGroupMessages(group.getId(), 10, res.get(1).getId(), null);
        Assertions.assertEquals(1, res2.size());

        QueryGroupMessagesResponse res3 = api.queryGroupMessages(group.getId(), 10, null, res.get(1).getId());
        Assertions.assertEquals(1, res3.size());
    }

    private ChannelMessageModel buildMessage() {
        return new ChannelMessageModel()
                .nick("userA")
                .body("message1")
                .data("data1")
                .persist(true);
    }

    @FunctionalInterface
    public interface Executor {
        void get() throws ApiException;
    }

    private void validateErrorResponse(Executor aExecutor) throws Exception {
        try {
            aExecutor.get();
        }
        catch (ApiException ex) {
            Assertions.assertEquals(ex.getCode(), 403);
            return;
        }
        throw new ApiException("no exception thrown");
    }
}
