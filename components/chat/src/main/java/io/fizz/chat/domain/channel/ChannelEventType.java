package io.fizz.chat.domain.channel;

public enum ChannelEventType {
    MessagePublished("CMSGP"),
    MessageUpdated("CMSGU"),
    MessageDeleted("CMSGD");

    private final String value;
    ChannelEventType(final String aValue) {
        value = aValue;
    }

    public String value() {
        return value;
    }
}
