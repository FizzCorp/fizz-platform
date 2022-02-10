package io.fizz.analytics.jobs.metricsRollup.aggregator;

import io.fizz.analytics.common.AbstractTransformer;
import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.source.hive.HiveDefines;
import io.fizz.analytics.common.source.hive.HiveEventFields;
import io.fizz.analytics.common.source.hive.HiveMetricTableSchema;
import io.fizz.analytics.common.source.hive.HiveProfileEnrichedEventTableSchema;
import io.fizz.common.domain.EventType;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.sql.*;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.types.DataTypes;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Objects;

public class SessionAttributeAggregator implements AbstractTransformer<Row,Row>, Serializable {
    private static UDF1 extractDuration = new UDF1<String,Integer>() {
        public Integer call(final String fieldsStr) {
            final JSONObject fields = new JSONObject(fieldsStr);
            return fields.getInt(HiveEventFields.SESSION_LENGTH.value());
        }
    };
    private static final String COL_DURATION = "duration";

    protected final String segment;
    private final HiveDefines.MetricId metricId;

    public SessionAttributeAggregator(final SparkSession aSpark, final HiveDefines.MetricId aSessionMetricId, String aSegment) {
        if (Objects.isNull(aSessionMetricId)) {
            throw new IllegalArgumentException("Invalid session metric id specified.");
        }
        if (Objects.isNull(aSpark)) {
            throw new IllegalArgumentException("Invalid spark session specified.");
        }

        this.metricId = aSessionMetricId;
        segment = aSegment;
        aSpark.udf().register("extractDuration", extractDuration, DataTypes.IntegerType);
    }

    @Override
    public Dataset<Row> transform(final Dataset<Row> aEventsDS, final HiveTime aTime) {
        if (Objects.isNull(aEventsDS)) {
            throw new IllegalArgumentException("invalid event data set specified.");
        }
        if (Objects.isNull(aTime)) {
            throw new IllegalArgumentException("invalid time specified.");
        }

        final Dataset<Row> sessionEndDS = mapSessionEndEvents(aEventsDS);

        return Objects.isNull(segment)
                ? aggregateSessionMetrics(sessionEndDS, aTime)
                : aggregateSessionMetrics(sessionEndDS, segment, aTime);

    }

    private Dataset<Row> mapSessionEndEvents(final Dataset<Row> aEventsDS) {
        return aEventsDS
                .filter((FilterFunction<Row>)row -> HiveProfileEnrichedEventTableSchema.eventType(row) == EventType.SESSION_ENDED.value())
                .withColumn(COL_DURATION, functions.callUDF("extractDuration", aEventsDS.col(HiveProfileEnrichedEventTableSchema.COL_FIELDS.title())));
    }

    private Dataset<Row> aggregateSessionMetrics(final Dataset<Row> aSessionsDS, final HiveTime aTime) {
        RelationalGroupedDataset groupDS = aSessionsDS.groupBy(HiveProfileEnrichedEventTableSchema.COL_APP_ID.title());
        return aggregateSessionMetrics(groupDS, null, aTime);
    }

    private Dataset<Row> aggregateSessionMetrics(final Dataset<Row> aSessionsDS, String segment, final HiveTime aTime) {
        RelationalGroupedDataset groupDS = aSessionsDS.groupBy(HiveProfileEnrichedEventTableSchema.COL_APP_ID.title(), segment);
        return aggregateSessionMetrics(groupDS, segment, aTime);
    }

    private Dataset<Row> aggregateSessionMetrics(RelationalGroupedDataset groupDS, String segment, final HiveTime aTime) {
        return groupDS
                .agg(
                        functions.sum(COL_DURATION).cast("string").as(HiveDefines.ValueTag.SUM),
                        functions.mean(COL_DURATION).cast("string").as(HiveDefines.ValueTag.MEAN),
                        functions.max(COL_DURATION).cast("string").as(HiveDefines.ValueTag.MAX),
                        functions.min(COL_DURATION).cast("string").as(HiveDefines.ValueTag.MIN)
                )
                .map(
                        new AggregateToMetricRowMapper(AggregateType.SUM | AggregateType.MAX | AggregateType.MIN | AggregateType.MEAN,
                                metricId, aTime, segment, true),
                        RowEncoder.apply(new HiveMetricTableSchema().schema())
                );
    }
}
