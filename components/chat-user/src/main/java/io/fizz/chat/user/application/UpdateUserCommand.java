package io.fizz.chat.user.application;

import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.Url;
import io.fizz.common.domain.UserId;
import io.fizz.chat.user.domain.Nick;
import io.fizz.chat.user.domain.StatusMessage;

public class UpdateUserCommand {
    private final ApplicationId appId;
    private final UserId userId;
    private final Nick nick;
    private final StatusMessage statusMessage;
    private final Url profileUrl;

    public UpdateUserCommand(final ApplicationId aAppId,
                             final UserId aUserId,
                             final Nick aNick,
                             final StatusMessage aStatusMessage,
                             final Url aProfileUrl) {
        Utils.assertRequiredArgument(aAppId, "invalid_app_id");
        Utils.assertRequiredArgument(aUserId, "invalid_user_id");

        appId = aAppId;
        userId = aUserId;
        nick = aNick;
        statusMessage = aStatusMessage;
        profileUrl = aProfileUrl;
    }

    public ApplicationId appId() {
        return appId;
    }

    public UserId userId() {
        return userId;
    }

    public Nick nick() {
        return nick;
    }

    public StatusMessage statusMessage() {
        return statusMessage;
    }

    public Url profileUrl() {
        return profileUrl;
    }
}