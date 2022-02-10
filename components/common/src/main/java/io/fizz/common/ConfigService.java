package io.fizz.common;

public class ConfigService {
    private static ConfigService instance = null;

    private final Config config = new Config();

    public static ConfigService instance() {
        if (instance == null) {
            instance = new ConfigService();
        }
        return instance;
    }

    private ConfigService() {
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
        return config.hasKey(key);
    }
}
