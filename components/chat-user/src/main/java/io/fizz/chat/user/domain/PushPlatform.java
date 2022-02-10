package io.fizz.chat.user.domain;

import java.util.Objects;

public enum PushPlatform {
    FCM("fcm");

    private final String value;

    PushPlatform(final String aValue) {
        this.value = aValue;
    }

    public String value() {
        return value;
    }

    public static PushPlatform fromValue(String aValue) {
        for (PushPlatform platform : PushPlatform.values()) {
            if (Objects.equals(platform.value(), aValue)) {
                return platform;
            }
        }

        return null;
    }
}
