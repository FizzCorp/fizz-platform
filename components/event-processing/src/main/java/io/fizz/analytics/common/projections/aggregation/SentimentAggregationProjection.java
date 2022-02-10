package io.fizz.analytics.common.projections.aggregation;

import io.fizz.analytics.common.source.hive.HiveDefines;
import io.fizz.analytics.common.source.hive.HiveKeywordsTableSchema;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.Row;

import java.util.Objects;

public class SentimentAggregationProjection {
    public static class MetricGroup {
        public final HiveDefines.MetricId negative;
        public final HiveDefines.MetricId positive;
        public final HiveDefines.MetricId neutral;

        public MetricGroup(final HiveDefines.MetricId aNegative, final HiveDefines.MetricId aPositive, final HiveDefines.MetricId aNeutral) {
            if (Objects.isNull(aNegative)) {
                throw new IllegalArgumentException("invalid aNegative sentiment metric id specified.");
            }
            if (Objects.isNull(aPositive)) {
                throw new IllegalArgumentException("invalid aPositive sentiment metric id specified.");
            }
            if (Objects.isNull(aNeutral)) {
                throw new IllegalArgumentException("invalid aNeutral sentiment metric id specified.");
            }

            negative = aNegative;
            positive = aPositive;
            neutral = aNeutral;
        }
    }

    public JavaRDD<Row> project(JavaRDD<Row> aDS) {
        return  aDS
        .groupBy((Function<Row, String>) aRow -> {
                final String appId = aRow.getString(aRow.fieldIndex(HiveKeywordsTableSchema.COL_APP_ID));
                final double value = aRow.getDouble(aRow.fieldIndex(HiveKeywordsTableSchema.COL_SENTIMENT_SCORE));

                ValueGroup group = ValueGroup.NEUTRAL;
                if (value < 0) {
                    group = ValueGroup.NEGATIVE;
                }
                else
                if (value > 0) {
                    group = ValueGroup.POSITIVE;
                }

                return appId + ":" + group;
            }
        )
        .map(new SentimentScoreCounter());
    }
}
