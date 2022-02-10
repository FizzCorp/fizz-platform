package io.fizz.chat.user.application;

import io.fizz.common.domain.Url;
import io.fizz.common.domain.UserId;
import io.fizz.chat.user.domain.Nick;
import io.fizz.chat.user.domain.StatusMessage;

public class UserDTO {
    private final UserId userId;
    private final Nick nick;
    private final StatusMessage statusMessage;
    private final Url profileUrl;
    private final boolean isOnline;
    private final String tokenFCM;

    public UserDTO(final UserId aUserId,
                   final Nick aNick,
                   final StatusMessage aStatusMessage,
                   final Url aProfileUrl,
                   final boolean aIsOnline,
                   final String aTokenFCM) {
        this.userId = aUserId;
        this.nick = aNick;
        this.statusMessage = aStatusMessage;
        this.profileUrl = aProfileUrl;
        this.isOnline = aIsOnline;
        this.tokenFCM = aTokenFCM;
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

    public boolean isOnline() {
        return isOnline;
    }

    public String token() {
        return tokenFCM;
    }
}
