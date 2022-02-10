package io.fizz.common;

import com.typesafe.config.ConfigFactory;

public class Config {
    private final com.typesafe.config.Config config;

    public Config() {
        config = ConfigFactory.load();
    }

    public Config(String name) {
        config = ConfigFactory.load(name);
    }

    public String getString(String key) {
        return config.getString(key);
    }

    public Number getNumber(String key) {
        return config.getNumber(key);
    }

    public Boolean getBoolean(String key) {
        return config.getBoolean(key);
    }

    public Boolean hasKey(String key) {
        return config.hasPath(key);
    }
}
