package io.fizz.session;

import io.fizz.common.Config;

import java.util.Objects;

public class ConfigService {
    private static Config instance;

    public static Config config() {
        if (Objects.isNull(instance)) {
            instance = new Config("session-store");
        }
        return instance;
    }
}