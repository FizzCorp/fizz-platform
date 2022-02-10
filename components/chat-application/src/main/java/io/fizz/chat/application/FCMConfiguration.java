package io.fizz.chat.application;

import io.fizz.common.Utils;

public class FCMConfiguration {
    private final String title;
    private final String secret;

    public FCMConfiguration(final String aTitle, final String aSecret) {
        Utils.assertRequiredArgumentLength(aSecret, 1, 4096, "invalid_fcm_secret");

        this.title = aTitle;
        this.secret = aSecret;
    }

    public String title() {
        return title;
    }

    public String secret() {
        return secret;
    }
}
