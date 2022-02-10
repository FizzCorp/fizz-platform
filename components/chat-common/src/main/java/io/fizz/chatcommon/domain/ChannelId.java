package io.fizz.chatcommon.domain;

import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;

import java.util.Objects;

public class ChannelId {
    private final String value;
    private final ApplicationId appId;

    public ChannelId(final ApplicationId aAppId, final String aValue) {
        final String trimmed = Objects.isNull(aValue) ? null : aValue.trim();

        Utils.assertRequiredArgumentLength(trimmed, 128, "invalid_channel_id");
        Utils.assertRequiedArgumentMatches(trimmed, "[A-Za-z0-9._-]+", "invalid_channel_id");
        Utils.assertRequiredArgument(aAppId, "invalid_app_id");

        value = trimmed;
        appId = aAppId;
    }

    public String value() {
        return value;
    }

    public ApplicationId appId() {
        return appId;
    }
}
