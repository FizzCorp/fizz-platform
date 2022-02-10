package io.fizz.gateway.http.controllers.group;

import io.fizz.gateway.http.controllers.BaseControllerTest;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.GroupApi;
import io.swagger.client.model.*;

import java.util.ArrayList;
import java.util.List;

public class BaseGroupControllerTest extends BaseControllerTest {
    protected Group createGroup() throws ApiException {
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
        return api.createGroup(request);
    }

    protected void addMember(final String aGroupId,
                             final String aMemberId,
                             final GroupRoleName aRole,
                             final GroupMemberState aState) throws ApiException {
        final GroupApi api = new GroupApi(new ApiClient());
        final List<GroupMemberRequest> members = new ArrayList<>();
        members.add(new GroupMemberRequest().id(aMemberId).role(aRole).state(aState));

        api.getApiClient().setApiKey(adminToken);
        api.addGroupMembers(aGroupId, members);
    }
}
