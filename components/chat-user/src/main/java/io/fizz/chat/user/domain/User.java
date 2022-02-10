package io.fizz.chat.user.domain;

import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.Url;
import io.fizz.common.domain.UserId;

import java.util.*;

public class User {
    public static class Builder {
        private ApplicationId appId;
        private UserId userId;
        private Nick nick;
        private StatusMessage statusMessage;
        private Url profileUrl;

        public Builder setAppId(final ApplicationId aAppId) {
            this.appId = aAppId;
            return this;
        }

        public Builder setUserId(final UserId aUserId) {
            this.userId = aUserId;
            return this;
        }

        public Builder setNick(final Nick aNick) {
            this.nick = aNick;
            return this;
        }

        public Builder setStatusMessage(final StatusMessage aStatusMessage) {
            this.statusMessage = aStatusMessage;
            return this;
        }

        public Builder setProfileUrl(Url aProfileUrl) {
            this.profileUrl = aProfileUrl;
            return this;
        }

        public User build() {
            if (Objects.isNull(nick)) {
                nick = new Nick("");
            }
            if (Objects.isNull(statusMessage)) {
                statusMessage = new StatusMessage("");
            }
            return new User(appId, userId, nick, statusMessage, profileUrl);
        }
    }

    private final UserId userId;
    private final ApplicationId appId;
    private final Nick nick;
    private final StatusMessage statusMessage;
    private final Url profileUrl;
    private String tokenFCM;

    public User(final ApplicationId aAppId,
                final UserId aUserId,
                final Nick aNick,
                final StatusMessage aStatusMessage,
                final Url aProfileUrl) {
        Utils.assertRequiredArgument(aAppId, "invalid_app_id");
        Utils.assertRequiredArgument(aUserId, "invalid_user_id");

        this.appId = aAppId;
        this.userId = aUserId;
        this.nick = aNick;
        this.statusMessage = aStatusMessage;
        this.profileUrl = aProfileUrl;
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

    public void setToken(final String aToken) {
        Utils.assertRequiredArgumentLength(aToken, 1, 4096, "invalid_push_token");

        tokenFCM = aToken;
    }

    public void clearToken() {
        tokenFCM = null;
    }

    public String token() {
        return tokenFCM;
    }
}
