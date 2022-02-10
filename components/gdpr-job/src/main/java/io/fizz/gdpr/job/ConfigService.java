package io.fizz.gdpr.job;

import io.fizz.common.Config;
import org.apache.hadoop.conf.Configuration;

public class ConfigService {
    private final Configuration hadoopConfig;
    private final Config localConfig;

    public ConfigService(Configuration hadoopConfig) {
        this.hadoopConfig = hadoopConfig;
        localConfig = new Config();
    }

    public String get(String key) {
        return hadoopConfig.get(key, localConfig.getString(key));
    }

    public int getInt(String key) {
        return Integer.parseInt(hadoopConfig.get(key, localConfig.getString(key)));
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(hadoopConfig.get(key, localConfig.getString(key)));
    }

    public Boolean hasKey(String key) {
        return localConfig.hasKey(key);
    }
}
