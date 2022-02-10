package io.fizz.analytics.jobs.metricsRollup.aggregator;

import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.source.hive.HiveDefines;
import io.fizz.analytics.common.source.hive.HiveProfileEnrichedEventTableSchema;
import io.fizz.analytics.common.source.hive.HiveMetricTableSchema;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Row;

public class AggregateToMetricRowMapper implements MapFunction<Row,Row> {
    private final int aggregations;
    private final String metricId;
    private final String year;
    private final String month;
    private final String dayOfMonth;
    private final String segment;

    public AggregateToMetricRowMapper(int aAggregations, HiveDefines.MetricId aMetricId, final HiveTime aTime, final String segment, boolean aIsDailyGranular){
        this.aggregations = aAggregations;
        this.metricId = aMetricId.value();
        this.year = Integer.toString(aTime.year.getValue());
        this.month = Integer.toString(aTime.month.getValue());
        this.dayOfMonth = aIsDailyGranular ? Integer.toString(aTime.dayOfMonth.getValue()) : "any";
        this.segment = segment;
    }

    @Override
    public Row call(Row aRow) {
        HiveMetricTableSchema.RowBuilder rowBuilder =  new HiveMetricTableSchema.RowBuilder()
        .setType(metricId)
        .setDtYear(year)
        .setDtMonth(month)
        .setDtDay(dayOfMonth)
        .setDtHour("any")
        .setAppId(HiveProfileEnrichedEventTableSchema.appId(aRow))
        .setYear(year)
        .setMonth(month)
        .setDay(dayOfMonth);

        setSegment(aRow, rowBuilder);

        addCustomAttr(rowBuilder, AggregateType.COUNT, aRow, HiveDefines.ValueTag.COUNT);
        addCustomAttr(rowBuilder, AggregateType.SUM, aRow, HiveDefines.ValueTag.SUM);
        addCustomAttr(rowBuilder, AggregateType.MEAN, aRow, HiveDefines.ValueTag.MEAN);
        addCustomAttr(rowBuilder, AggregateType.MIN, aRow, HiveDefines.ValueTag.MIN);
        addCustomAttr(rowBuilder, AggregateType.MAX, aRow, HiveDefines.ValueTag.MAX);

        return rowBuilder.get();
    }

    private void addCustomAttr(HiveMetricTableSchema.RowBuilder rowBuilder, int aggregateType, final Row row, final String key) {
        if ((aggregations&aggregateType) != 0) {
            final int fieldIdx = row.fieldIndex(key);
            final Object fieldValue = row.get(fieldIdx);

            String value = (fieldValue instanceof String) ? (String) fieldValue : fieldValue.toString();
            rowBuilder.addCustomAttr(key, value);
        }
    }

    private void setSegment(Row aRow, HiveMetricTableSchema.RowBuilder rowBuilder) {
        if (segment != null) {
            rowBuilder.setDim(segment);
            rowBuilder.setDimValue(HiveProfileEnrichedEventTableSchema.value(aRow, segment));
        }
        else {
            rowBuilder
            .setDim("any")
            .setDimValue("any");

        }
    }
}
