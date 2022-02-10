package io.fizz.chat.group.application.notifications;

import io.fizz.chat.group.domain.group.GroupId;

public class GroupProfileNotification extends GroupNotification {
    private final String title;
    private final String imageURL;
    private final String description;
    private final String type;

    public GroupProfileNotification(final GroupId aGroupId,
                                    final String aTitle,
                                    final String aImageURL,
                                    final String aDescription,
                                    final String aType) {
        super(aGroupId);
        this.title = aTitle;
        this.imageURL = aImageURL;
        this.description = aDescription;
        this.type = aType;
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
}
