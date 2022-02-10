package io.fizz.analytics.common.source.hive;

public class HiveTempMetricTableSchema extends HiveMetricTableSchema {
    public static final String TABLE_NAME = "metrics_temp";

    public HiveTempMetricTableSchema() {
        super(TABLE_NAME);
    }
}
