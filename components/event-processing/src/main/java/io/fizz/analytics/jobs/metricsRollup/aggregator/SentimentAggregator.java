package io.fizz.analytics.jobs.metricsRollup.aggregator;

import io.fizz.analytics.common.AbstractTransformer;
import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.source.hive.HiveDefines;
import io.fizz.analytics.common.source.hive.HiveEventFields;
import io.fizz.analytics.common.source.hive.HiveProfileEnrichedEventTableSchema;
import io.fizz.analytics.common.source.hive.HiveMetricTableSchema;
import io.fizz.common.domain.EventType;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.sql.*;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.types.DataTypes;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Objects;

public class SentimentAggregator implements AbstractTransformer<Row,Row>, Serializable {
    private static UDF1 extractSentiment = new UDF1<String,Double>() {
        public Double call(final String fieldsStr){
            final JSONObject fields = new JSONObject(fieldsStr);
            return fields.has(HiveEventFields.SENTIMENT_SCORE.value())
                    ? fields.getDouble(HiveEventFields.SENTIMENT_SCORE.value()) : 0.0;
        }
    };
    private static final String COL_SENTIMENT_SCORE = "sentimentScore";

    private final String segment;
    private final HiveDefines.MetricId actionMetricId;

    public SentimentAggregator(final SparkSession aSpark, final HiveDefines.MetricId aActionMetricId, String aSegment) {
        if (Objects.isNull(aActionMetricId)) {
            throw new IllegalArgumentException("Invalid action metric id specified.");
        }

        if (Objects.isNull(aSpark)) {
            throw new IllegalArgumentException("Invalid spark session specified.");
        }

        actionMetricId = aActionMetricId;
        segment = aSegment;

        aSpark.udf().register("extractSentiment", extractSentiment, DataTypes.DoubleType);
    }

    @Override
    public Dataset<Row> transform(Dataset<Row> aSourceDS, HiveTime aTime) {
        if (Objects.isNull(aSourceDS)) {
            throw new IllegalArgumentException("invalid data set provided");
        }
        if (Objects.isNull(aTime)) {
            throw new IllegalArgumentException("invalid time provided");
        }

        final Dataset<Row> scoredMsgDS = mapSentimentScoredMessageEvents(aSourceDS);

        return Objects.isNull(segment)
                ? aggregateSentimentMetrics(scoredMsgDS, aTime)
                : aggregateSentimentMetrics(scoredMsgDS, segment, aTime);
    }

    private Dataset<Row> mapSentimentScoredMessageEvents(Dataset<Row> aSourceDS) {
        return aSourceDS.filter((FilterFunction<Row>) row -> HiveProfileEnrichedEventTableSchema.eventType(row) == EventType.TEXT_MESSAGE_SENT.value())
                .withColumn(COL_SENTIMENT_SCORE, functions.callUDF("extractSentiment", aSourceDS.col(HiveProfileEnrichedEventTableSchema.COL_FIELDS.title())));
    }

    private Dataset<Row> aggregateSentimentMetrics(final Dataset<Row> aActionDS, final HiveTime aTime) {
        RelationalGroupedDataset groupDS = aActionDS.groupBy(HiveProfileEnrichedEventTableSchema.COL_APP_ID.title());
        return aggregateSentimentMetrics(groupDS, null, aTime);
    }

    private Dataset<Row> aggregateSentimentMetrics(final Dataset<Row> aActionDS, String segment, final HiveTime aTime) {
        RelationalGroupedDataset groupDS = aActionDS.groupBy(HiveProfileEnrichedEventTableSchema.COL_APP_ID.title(), segment);
        return aggregateSentimentMetrics(groupDS, segment, aTime);
    }

    private Dataset<Row> aggregateSentimentMetrics(RelationalGroupedDataset groupDS, String segment, final HiveTime aTime) {
        return groupDS
                .agg(
                        functions.mean(COL_SENTIMENT_SCORE).cast("string").as(HiveDefines.ValueTag.MEAN),
                        functions.max(COL_SENTIMENT_SCORE).cast("string").as(HiveDefines.ValueTag.MAX),
                        functions.min(COL_SENTIMENT_SCORE).cast("string").as(HiveDefines.ValueTag.MIN)
                )
                .map(
                        new AggregateToMetricRowMapper(AggregateType.MIN | AggregateType.MAX | AggregateType.MEAN, actionMetricId, aTime, segment, true),
                        RowEncoder.apply(new HiveMetricTableSchema().schema())
                );
    }
}