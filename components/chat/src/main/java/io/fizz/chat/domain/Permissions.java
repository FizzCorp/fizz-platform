package io.fizz.chat.domain;

public enum Permissions {
    READ_MESSAGES("readMessages"),
    READ_CHANNEL_TOPICS("readChannelTopics"),
    PUBLISH_MESSAGES("publishMessages"),
    EDIT_OWN_MESSAGE("editOwnMessage"),
    DELETE_OWN_MESSAGE("deleteOwnMessage"),
    DELETE_ANY_MESSAGE("deleteAnyMessage"),
    MANAGE_BANS("manageBans"),
    MANAGE_MUTES("manageMutes");

    private String value;
    Permissions(String aValue) {
        value = aValue;
    }

    public String value() {
        return value;
    }
}
