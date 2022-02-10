package io.fizz.analytics.jobs.metricsRollup.aggregator;

import io.fizz.analytics.common.AbstractTransformer;
import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.source.hive.HiveDefines;
import io.fizz.analytics.common.source.hive.HiveProfileEnrichedEventTableSchema;
import io.fizz.analytics.common.source.hive.HiveMetricTableSchema;
import io.fizz.common.domain.EventType;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.RelationalGroupedDataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.functions;

import java.io.Serializable;
import java.util.Objects;

public class NewUsersAggregator implements AbstractTransformer<Row,Row>, Serializable {
    private final String segment;
    private final HiveDefines.MetricId actionMetricId;

    public NewUsersAggregator(final HiveDefines.MetricId aActionMetricId, String aSegment) {
        if (Objects.isNull(aActionMetricId)) {
            throw new IllegalArgumentException("Invalid action metric id specified.");
        }

        actionMetricId = aActionMetricId;
        segment = aSegment;
    }

    @Override
    public Dataset<Row> transform(Dataset<Row> aSourceDS, HiveTime aTime) {
        if (Objects.isNull(aSourceDS)) {
            throw new IllegalArgumentException("invalid data set provided");
        }
        if (Objects.isNull(aTime)) {
            throw new IllegalArgumentException("invalid time provided");
        }

        final Dataset<Row> newUsersDS = mapNewUsersEvents(aSourceDS);

        return Objects.isNull(segment)
                ? aggregateNewUsersMetrics(newUsersDS, aTime)
                : aggregateNewUsersMetrics(newUsersDS, segment, aTime);
    }

    private Dataset<Row> mapNewUsersEvents(Dataset<Row> aSourceDS) {
        return aSourceDS.filter((FilterFunction<Row>) row -> HiveProfileEnrichedEventTableSchema.eventType(row) == EventType.NEW_USER_CREATED.value());
    }

    private Dataset<Row> aggregateNewUsersMetrics(final Dataset<Row> aActionDS, final HiveTime aTime) {
        RelationalGroupedDataset groupDS = aActionDS.groupBy(HiveProfileEnrichedEventTableSchema.COL_APP_ID.title());
        return aggregateNewUsersMetrics(groupDS, null, aTime);
    }

    private Dataset<Row> aggregateNewUsersMetrics(final Dataset<Row> aActionDS, String segment, final HiveTime aTime) {
        RelationalGroupedDataset groupDS = aActionDS.groupBy(HiveProfileEnrichedEventTableSchema.COL_APP_ID.title(), segment);
        return aggregateNewUsersMetrics(groupDS, segment, aTime);
    }

    private Dataset<Row> aggregateNewUsersMetrics(RelationalGroupedDataset groupDS, String segment, final HiveTime aTime) {
        return groupDS
                .agg(
                        functions.count("*").cast("string").as(HiveDefines.ValueTag.COUNT)
                )
                .map(
                        new AggregateToMetricRowMapper(AggregateType.COUNT, actionMetricId, aTime, segment, true),
                        RowEncoder.apply(new HiveMetricTableSchema().schema())
                );
    }
}
