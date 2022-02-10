package io.fizz.analytics.common.projections.aggregation;

import io.fizz.analytics.AbstractSparkTest;
import io.fizz.analytics.common.source.hive.HiveDefines;
import io.fizz.analytics.common.source.hive.HiveKeywordsTableSchema;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class SentimentAggregationProjectionTest extends AbstractSparkTest {
    @Nested
    class MetricGroupTest {
        @Test
        @DisplayName("it should create valid metric group")
        void basicValidationTest() {
            final SentimentAggregationProjection.MetricGroup group = new SentimentAggregationProjection.MetricGroup(
                    HiveDefines.MetricId.SENTIMENT_NEGATIVE_DAILY, HiveDefines.MetricId.SENTIMENT_POSITIVE_DAILY, HiveDefines.MetricId.SENTIMENT_NEUTRAL_DAILY
            );
            assert (group.negative == HiveDefines.MetricId.SENTIMENT_NEGATIVE_DAILY);
            assert (group.positive == HiveDefines.MetricId.SENTIMENT_POSITIVE_DAILY);
            assert (group.neutral == HiveDefines.MetricId.SENTIMENT_NEUTRAL_DAILY);
        }

        @Test
        @DisplayName("it should not create group for invalid negative sentiment")
        void invalidNegativeSentimentTest() {
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                new SentimentAggregationProjection.MetricGroup(
                    null, HiveDefines.MetricId.SENTIMENT_POSITIVE_DAILY, HiveDefines.MetricId.SENTIMENT_NEUTRAL_DAILY
                );
            });
        }

        @Test
        @DisplayName("it should not create group for invalid positive sentiment")
        void invalidPositiveSentimentTest() {
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                new SentimentAggregationProjection.MetricGroup(
                        HiveDefines.MetricId.SENTIMENT_NEGATIVE_DAILY, null, HiveDefines.MetricId.SENTIMENT_NEUTRAL_DAILY
                );
            });
        }

        @Test
        @DisplayName("it should not create group for invalid neutral sentiment")
        void invalidNeutralSentimentTest() {
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                new SentimentAggregationProjection.MetricGroup(
                        HiveDefines.MetricId.SENTIMENT_NEGATIVE_DAILY, HiveDefines.MetricId.SENTIMENT_POSITIVE_DAILY, null
                );
            });
        }
    }

    @Test
    @DisplayName("it should aggregate sentiment values")
    void aggregationTest() {
        final StructType schema = new HiveKeywordsTableSchema().schema();
        final List<Row> rows = new ArrayList<Row>(){
            {
                add(new GenericRowWithSchema(new Object[]{
                    "keyword1", 0.2, 0.1, 0.0, 0.0, 0.0, 0.0, "appA", "2018", "2018-04", "2018-04-16"
                }, schema));
                add(new GenericRowWithSchema(new Object[]{
                    "keyword2", 0.6, 0.1, 0.0, 0.0, 0.0, 0.0, "appA", "2018", "2018-04", "2018-04-16"
                }, schema));
                add(new GenericRowWithSchema(new Object[]{
                    "keyword3", -0.2, 0.1, 0.0, 0.0, 0.0, 0.0, "appA", "2018", "2018-04", "2018-04-16"
                }, schema));
                add(new GenericRowWithSchema(new Object[]{
                    "keyword4", 0.0, 0.1, 0.0, 0.0, 0.0, 0.0, "appA", "2018", "2018-04", "2018-04-16"
                }, schema));
                add(new GenericRowWithSchema(new Object[]{
                    "keyword1", 0.2, 0.1, 0.0, 0.0, 0.0, 0.0, "appB", "2018", "2018-04", "2018-04-16"
                }, schema));
            }
        };

        final Dataset<Row> ds = spark.createDataset(rows, RowEncoder.apply(schema));
        final JavaRDD<Row> sentimentCountDD = new SentimentAggregationProjection().project(ds.javaRDD());
        final List<Row> sentimentCountRows = sentimentCountDD.collect();

        for (Row row: sentimentCountRows) {
            if (row.getString(0).equals("appA")) {
                int sentimentValue = row.getInt(1);
                int sentimentValueCount = row.getInt(2);
                switch (sentimentValue) {
                    case 0:
                        assert (sentimentValueCount == 1);
                        break;
                    case 1:
                        assert (sentimentValueCount == 2);
                        break;
                    case -1:
                        assert (sentimentValueCount == 1);
                        break;
                }
            }
            else
            {
                // one positive keyword in appB
                assert (row.getInt(1) == 1);
                assert (row.getInt(2) == 1);
            }
        }
    }
}
