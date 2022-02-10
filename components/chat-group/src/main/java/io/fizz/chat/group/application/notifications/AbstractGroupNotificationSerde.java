package io.fizz.chat.group.application.notifications;

public interface AbstractGroupNotificationSerde {
    String serialize(final GroupMemberNotification aNotification);
    String serialize(final GroupProfileNotification aNotification);
}
