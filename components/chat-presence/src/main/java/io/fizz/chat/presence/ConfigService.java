package io.fizz.chat.presence;

import io.fizz.common.Config;

import java.util.Objects;

public class ConfigService {
    private static Config instance;

    public static Config config() {
        if (Objects.isNull(instance)) {
            instance = new Config("chat-presence");
        }
        return instance;
    }
}
