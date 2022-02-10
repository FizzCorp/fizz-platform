package io.fizz.chatcommon.domain;

import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;

public class PublicChannelId extends ChannelId {
    public PublicChannelId(ApplicationId aAppId, String aValue) {
        super(aAppId, aValue);

        Utils.assertRequiedArgumentMatches(value(), "[A-Za-z0-9_-]+", "invalid_channel_id");
    }
}
