package io.fizz.analytics.jobs.metricsRollup.common;

import io.fizz.analytics.common.source.hive.HiveDefines;
import io.fizz.analytics.common.source.hive.HiveMetricTableSchema;
import org.apache.spark.sql.Row;

import java.util.HashMap;
import java.util.Objects;

public class MetricValidator {
    public static class Key {
        public final String appId;
        public final String segmentKey;
        public final String segmentValue;
        public final String metricId;

        public Key(final String aAppId, String aSegmentKey, String aSegmentValue, final String aMetricId) {
            if (Objects.isNull(aAppId)) {
                throw new IllegalArgumentException("invalid app id");
            }
            if (Objects.isNull(aMetricId)) {
                throw new IllegalArgumentException("invalid metric id");
            }

            appId = aAppId;
            segmentKey = Objects.isNull(aSegmentKey) ? "any" : aSegmentKey;
            segmentValue = Objects.isNull(aSegmentValue) ? "any" : aSegmentValue;
            metricId = aMetricId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return Objects.equals(appId, key.appId) &&
                    Objects.equals(segmentKey, key.segmentKey) &&
                    Objects.equals(segmentValue, key.segmentValue) &&
                    Objects.equals(metricId, key.metricId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(appId, segmentKey, segmentValue, metricId);
        }
    }

    private final HashMap<String,Double> values = new HashMap<>();

    public MetricValidator() {
    }

    public MetricValidator count(Double value) {
        values.put(HiveDefines.ValueTag.COUNT, value);
        return this;
    }

    public MetricValidator sum(Double value) {
        values.put(HiveDefines.ValueTag.SUM, value);
        return this;
    }

    public MetricValidator mean(Double value) {
        values.put(HiveDefines.ValueTag.MEAN, value);
        return this;
    }

    public MetricValidator min(Double value) {
        values.put(HiveDefines.ValueTag.MIN, value);
        return this;
    }

    public MetricValidator max(Double value) {
        values.put(HiveDefines.ValueTag.MAX, value);
        return this;
    }

    public void validate(final Row aMetric, String aSegment) {
        String dim =  aMetric.getString(aMetric.fieldIndex(HiveMetricTableSchema.COL_DIM.title()));
        assert (dim.equals(aSegment));

        for (int ai = 1; ai <= 5; ai++) {
            final String key = getAttrKey(aMetric, ai);
            if (key == null || key.equals("null")) {
                continue;
            }

            final String valueStr = getAttrValue(aMetric, ai);
            if (valueStr == null) {
                continue;
            }

            final double lhs = Double.parseDouble(valueStr);
            final double rhs = values.get(key);
            assert (lhs == rhs);
        }
    }

    private String getAttrKey(final Row aRow, int aAttrIdx) {
        switch (aAttrIdx) {
            case 1:
                return aRow.getString(aRow.fieldIndex(HiveMetricTableSchema.COL_ATTR_1.title()));
            case 2:
                return aRow.getString(aRow.fieldIndex(HiveMetricTableSchema.COL_ATTR_2.title()));
            case 3:
                return aRow.getString(aRow.fieldIndex(HiveMetricTableSchema.COL_ATTR_3.title()));
            case 4:
                return aRow.getString(aRow.fieldIndex(HiveMetricTableSchema.COL_ATTR_4.title()));
            case 5:
                return aRow.getString(aRow.fieldIndex(HiveMetricTableSchema.COL_ATTR_5.title()));
        }
        return null;
    }

    private String getAttrValue(final Row aRow, int aAttrIdx) {
        switch (aAttrIdx) {
            case 1:
                return aRow.getString(aRow.fieldIndex(HiveMetricTableSchema.COL_VALUE_1.title()));
            case 2:
                return aRow.getString(aRow.fieldIndex(HiveMetricTableSchema.COL_VALUE_2.title()));
            case 3:
                return aRow.getString(aRow.fieldIndex(HiveMetricTableSchema.COL_VALUE_3.title()));
            case 4:
                return aRow.getString(aRow.fieldIndex(HiveMetricTableSchema.COL_VALUE_4.title()));
            case 5:
                return aRow.getString(aRow.fieldIndex(HiveMetricTableSchema.COL_VALUE_5.title()));
        }
        return null;
    }
}
