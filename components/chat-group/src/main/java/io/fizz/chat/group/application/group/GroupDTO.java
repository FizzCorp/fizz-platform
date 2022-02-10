package io.fizz.chat.group.application.group;

import io.fizz.chat.group.domain.group.Group;
import io.fizz.chat.group.domain.group.GroupMember;
import io.fizz.common.Utils;

import java.util.Date;

public class GroupDTO {
    private final String id;
    private final String title;
    private final String imageURL;
    private final String description;
    private final String type;
    private final String createdBy;
    private final String channel;
    private final Date createdOn;
    private final Date updatedOn;
    private final GroupMemberDTO[] members;

    public GroupDTO(final Group aGroup) {
        Utils.assertRequiredArgument(aGroup, "invalid_group");

        id = aGroup.id().value();
        title = aGroup.title();
        imageURL = aGroup.imageURL();
        description = aGroup.description();
        type = aGroup.type();
        createdBy = aGroup.createdBy().value();
        channel = aGroup.channelId().value();
        createdOn = aGroup.createdOn();
        updatedOn = aGroup.updatedOn();

        members = new GroupMemberDTO[aGroup.members().size()];
        int mi = 0;
        for (GroupMember member: aGroup.members()) {
            members[mi++] = new GroupMemberDTO(member);
        }
    }

    public String id() {
        return id;
    }

    public String title() {
        return title;
    }

    public String imageURL() {
        return imageURL;
    }

    public String description() {
        return description;
    }

    public String type() {
        return type;
    }

    public String createdBy() {
        return createdBy;
    }

    public String channel() {
        return channel;
    }

    public Date createdOn() {
        return createdOn;
    }

    public Date updatedOn() {
        return updatedOn;
    }

    public GroupMemberDTO[] members() {
        return members;
    }
}
