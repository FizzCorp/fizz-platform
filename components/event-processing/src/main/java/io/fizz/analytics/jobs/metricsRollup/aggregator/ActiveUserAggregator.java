package io.fizz.analytics.jobs.metricsRollup.aggregator;

import io.fizz.analytics.common.AbstractTransformer;
import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.source.hive.HiveDefines;
import io.fizz.analytics.common.source.hive.HiveMetricTableSchema;
import io.fizz.analytics.common.source.hive.HiveProfileEnrichedEventTableSchema;
import io.fizz.common.domain.EventType;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.sql.*;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;

import java.io.Serializable;
import java.util.Objects;

public class ActiveUserAggregator implements AbstractTransformer<Row,Row>, Serializable {
    protected final String segment;
    private final HiveDefines.MetricId metricId;

    public ActiveUserAggregator(final HiveDefines.MetricId aActiveUsersMetricId, String aSegment) {
        if (Objects.isNull(aActiveUsersMetricId)) {
            throw new IllegalArgumentException("Invalid active users metric id specified.");
        }

        this.metricId = aActiveUsersMetricId;
        segment = aSegment;
    }

    @Override
    public Dataset<Row> transform(final Dataset<Row> aEventsDS, final HiveTime aTime) {
        if (Objects.isNull(aEventsDS)) {
            throw new IllegalArgumentException("invalid event data set specified.");
        }
        if (Objects.isNull(aTime)) {
            throw new IllegalArgumentException("invalid time specified.");
        }

        final Dataset<Row> sessionDS = mapStartSessionEvents(aEventsDS);

        return Objects.isNull(segment)
                ? aggregateActiveUsersMetrics(sessionDS, aTime)
                : aggregateActiveUsersMetrics(sessionDS, segment, aTime);

    }

    private Dataset<Row> mapStartSessionEvents(final Dataset<Row> aEventsDS) {
        return aEventsDS
                .filter((FilterFunction<Row>)row -> HiveProfileEnrichedEventTableSchema.eventType(row) == EventType.SESSION_STARTED.value());
    }

    private Dataset<Row> aggregateActiveUsersMetrics(final Dataset<Row> aSessionsDS, final HiveTime aTime) {
        RelationalGroupedDataset groupDS = aSessionsDS.groupBy(HiveProfileEnrichedEventTableSchema.COL_APP_ID.title());
        return aggregateActiveUsersMetrics(groupDS, null, aTime);
    }

    private Dataset<Row> aggregateActiveUsersMetrics(final Dataset<Row> aSessionsDS, String segment, final HiveTime aTime) {
        RelationalGroupedDataset groupDS = aSessionsDS.groupBy(HiveProfileEnrichedEventTableSchema.COL_APP_ID.title(), segment);
        return aggregateActiveUsersMetrics(groupDS, segment, aTime);
    }

    private Dataset<Row> aggregateActiveUsersMetrics(RelationalGroupedDataset groupDS, String segment, final HiveTime aTime) {
        return groupDS
                .agg(
                        functions.countDistinct(HiveProfileEnrichedEventTableSchema.COL_USER_ID.title()).cast("string").as(HiveDefines.ValueTag.COUNT)
                )
                .map(
                        new AggregateToMetricRowMapper(AggregateType.COUNT, metricId, aTime, segment, true),
                        RowEncoder.apply(new HiveMetricTableSchema().schema())
                );
    }
}
