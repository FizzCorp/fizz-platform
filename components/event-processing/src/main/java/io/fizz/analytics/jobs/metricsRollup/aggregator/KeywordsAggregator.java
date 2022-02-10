package io.fizz.analytics.jobs.metricsRollup.aggregator;

import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.projections.aggregation.SentimentScoreCounter;
import io.fizz.analytics.common.source.hive.HiveDefines;
import io.fizz.analytics.common.source.hive.HiveMetricTableSchema;
import io.fizz.analytics.common.projections.aggregation.SentimentAggregationProjection;
import io.fizz.analytics.common.AbstractTransformer;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.*;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.types.StructType;

import java.io.Serializable;
import java.util.Objects;

public class KeywordsAggregator extends SentimentAggregationProjection implements AbstractTransformer<Row,Row>, Serializable {
    private static class SentimentCount2Metrics implements MapFunction<Row,Row> {
        private final String year;
        private final String month;
        private final String dayOfMonth;
        private final String negativeId;
        private final String positiveId;
        private final String neutralId;
        private final StructType schema;

        public SentimentCount2Metrics(int aYear, int aMonth, int aDayOfMonth, final MetricGroup aGroup, final StructType aSchema) {
            year = Integer.toString(aYear);
            month = Integer.toString(aMonth);
            dayOfMonth = Integer.toString(aDayOfMonth);
            negativeId = aGroup.negative.value();
            positiveId = aGroup.positive.value();
            neutralId = aGroup.neutral.value();
            schema = aSchema;
        }

        @Override
        public Row call(Row row) {
            final String appId = row.getString(row.fieldIndex(SentimentScoreCounter.COL_APP_ID));
            final int score = row.getInt(row.fieldIndex(SentimentScoreCounter.COL_SCORE));
            final int count = row.getInt(row.fieldIndex(SentimentScoreCounter.COL_COUNT));
            String metric = neutralId;
            if (score < 0.0) {
                metric = negativeId;
            }
            else
            if (score > 0.0) {
                metric = positiveId;
            }

            return new HiveMetricTableSchema.RowBuilder()
                .setType(metric)
                .addCustomAttr(HiveDefines.ValueTag.COUNT, Integer.toString(count))
                .setDim("any")
                .setDimValue("any")
                .setDtYear(year)
                .setDtMonth(month)
                .setDtDay(dayOfMonth)
                .setDtHour("any")
                .setAppId(appId)
                .setYear(year)
                .setMonth(month)
                .setDay(dayOfMonth)
                .get();
        }
    }

    private final HiveDefines.MetricId negativeId;
    private final HiveDefines.MetricId positiveId;
    private final HiveDefines.MetricId neutralId;
    private final SparkSession spark;

    private static Encoder<Row> encoder() {
        return RowEncoder.apply(new HiveMetricTableSchema().schema());
    }

    public KeywordsAggregator(final MetricGroup aGroup, final SparkSession aSpark) {
        if (Objects.isNull(aGroup)) {
            throw new IllegalArgumentException("invalid metric group specified for keywords aggregation.");
        }
        if (Objects.isNull(aSpark)) {
            throw new IllegalArgumentException("invalid spark session specified for keywords aggregation.");
        }

        negativeId = aGroup.negative;
        positiveId = aGroup.positive;
        neutralId = aGroup.neutral;
        spark = aSpark;
    }

    @Override
    public Dataset<Row> transform(final Dataset<Row> aSourceDS, final HiveTime aTime) {
        if (Objects.isNull(aSourceDS)) {
            throw new IllegalArgumentException("invalid data set provided");
        }
        if (Objects.isNull(aTime)) {
            throw new IllegalArgumentException("invalid time provided");
        }

        final StructType schema = new HiveMetricTableSchema().schema();
        final int year = aTime.year.getValue();
        final int month = aTime.month.getValue();
        final int dayOfMonth = aTime.dayOfMonth.getValue();

        final JavaRDD<Row> rdd = super.project(aSourceDS.javaRDD());
        return spark.createDataset(rdd.rdd(), SentimentScoreCounter.encoder())
        .map(
            new SentimentCount2Metrics(year, month, dayOfMonth, new MetricGroup(negativeId, positiveId, neutralId), schema),
            encoder()
        );
    }
}
