package io.fizz.analytics.jobs.metricsRollup.aggregator.translation;

import io.fizz.analytics.common.AbstractTransformer;
import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.source.hive.HiveDefines;
import io.fizz.analytics.common.source.hive.HiveMetricTableSchema;
import io.fizz.analytics.common.source.hive.HiveProfileEnrichedEventTableSchema;
import io.fizz.analytics.jobs.metricsRollup.aggregator.AggregateToMetricRowMapper;
import io.fizz.analytics.jobs.metricsRollup.aggregator.AggregateType;
import io.fizz.common.domain.EventType;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.sql.*;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class AbstractTranslationAggregator implements AbstractTransformer<Row,Row>, Serializable {
    private final String segment;
    private final HiveDefines.MetricId actionMetricId;

    private static final String COL_ACTION_ID = "receipt";

    AbstractTranslationAggregator(final SparkSession aSpark, final HiveDefines.MetricId aTranslationsMetricId, String aSegment) {
        if (Objects.isNull(aTranslationsMetricId)) {
            throw new IllegalArgumentException("Invalid action metric id specified.");
        }

        actionMetricId = aTranslationsMetricId;
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

        final Dataset<Row> translationDS = mapTranslationEvents(aSourceDS);

        return Objects.isNull(segment)
                ? aggregateMetrics(translationDS, aTime)
                : aggregateMetrics(translationDS, segment, aTime);
    }

    Dataset<Row> mapTranslationEvents(Dataset<Row> aSourceDS) {
        return aSourceDS.filter((FilterFunction<Row>) row -> HiveProfileEnrichedEventTableSchema.eventType(row) == EventType.TEXT_TRANSLATED.value());
    }

    List<String> groupColumns(){
        return new ArrayList<>();
    }

    Column aggregatorColumn() {
        return functions.count("*").cast("string").as(HiveDefines.ValueTag.COUNT);
    }

    int aggregateType() {
        return AggregateType.COUNT;
    }

    private Dataset<Row> aggregateMetrics(final Dataset<Row> aActionDS, final HiveTime aTime) {
        String[] groupColumns = groupColumns().toArray(new String[0]);
        RelationalGroupedDataset groupDS = aActionDS.groupBy(HiveProfileEnrichedEventTableSchema.COL_APP_ID.title(), groupColumns);
        return aggregateMetrics(groupDS, null, aTime);
    }

    private Dataset<Row> aggregateMetrics(final Dataset<Row> aActionDS, String segment, final HiveTime aTime) {
        List<String> groupColumnsList = groupColumns();
        groupColumnsList.add(segment);
        String[] groupColumns = groupColumnsList.toArray(new String[0]);
        RelationalGroupedDataset groupDS = aActionDS.groupBy(HiveProfileEnrichedEventTableSchema.COL_APP_ID.title(), groupColumns);
        return aggregateMetrics(groupDS, segment, aTime);
    }

    private Dataset<Row> aggregateMetrics(RelationalGroupedDataset groupDS, String segment, final HiveTime aTime) {
        return groupDS
                .agg(
                        aggregatorColumn()
                )
                .map(
                        new AggregateToMetricRowMapper(aggregateType(), actionMetricId, aTime, segment, true),
                        RowEncoder.apply(new HiveMetricTableSchema().schema())
                );
    }
}
