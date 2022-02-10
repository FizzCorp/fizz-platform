package io.fizz.chat.group.application.group;

import io.fizz.chat.group.domain.group.GroupId;
import io.fizz.command.bus.AbstractCommand;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;

import java.nio.charset.StandardCharsets;

public class UpdateGroupProfileCommand implements AbstractCommand {
    private final UserId operatorId;
    private final GroupId groupId;
    private final String newTitle;
    private final String newImageURL;
    private final String newDescription;
    private final String newType;

    public UpdateGroupProfileCommand(final String aOperatorId,
                                     final String aAppId,
                                     final String aGroupId,
                                     final String aNewTitle,
                                     final String aNewImageURL,
                                     final String aNewDescription,
                                     final String aNewType) {
        try {
            this.operatorId = new UserId(aOperatorId);
            this.groupId = new GroupId(new ApplicationId(aAppId), aGroupId);
            this.newTitle = aNewTitle;
            this.newImageURL = aNewImageURL;
            this.newDescription = aNewDescription;
            this.newType = aNewType;
        }
        catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    @Override
    public byte[] key() {
        return groupId.qualifiedValue().getBytes(StandardCharsets.UTF_8);
    }

    public UserId operatorId() {
        return operatorId;
    }

    public GroupId groupId() {
        return groupId;
    }

    public String newTitle() {
        return newTitle;
    }

    public String newImageURL() {
        return newImageURL;
    }

    public String newDescription() {
        return newDescription;
    }

    public String newType() {
        return newType;
    }
}
