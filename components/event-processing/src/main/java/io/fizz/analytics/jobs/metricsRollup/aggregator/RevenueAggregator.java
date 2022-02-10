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
import java.util.UUID;

public class RevenueAggregator implements AbstractTransformer<Row,Row>, Serializable {
    private static UDF1 extractRevenue = new UDF1<String,Long>() {
        public Long call(final String fieldsStr){
            final JSONObject fields = new JSONObject(fieldsStr);
            return fields.has(HiveEventFields.PURCHASE_AMOUNT.value())
                    ? fields.getLong(HiveEventFields.PURCHASE_AMOUNT.value()) : 0;
        }
    };

    private static UDF1 extractReceipt = new UDF1<String,String>() {
        public String call(final String fieldsStr){
            final JSONObject fields = new JSONObject(fieldsStr);
            return fields.has(HiveEventFields.PURCHASE_RECEIPT.value())
                    ? fields.getString(HiveEventFields.PURCHASE_RECEIPT.value()) : UUID.randomUUID().toString();
        }
    };
    private static final String COL_REVENUE = "revenue";
    private static final String COL_RECEIPT = "receipt";

    private final String segment;
    private final HiveDefines.MetricId actionMetricId;

    public RevenueAggregator(final SparkSession aSpark, final HiveDefines.MetricId aActionMetricId, String aSegment) {
        if (Objects.isNull(aActionMetricId)) {
            throw new IllegalArgumentException("Invalid action metric id specified.");
        }

        if (Objects.isNull(aSpark)) {
            throw new IllegalArgumentException("Invalid spark session specified.");
        }

        actionMetricId = aActionMetricId;
        segment = aSegment;

        aSpark.udf().register("extractRevenue", extractRevenue, DataTypes.LongType);
        aSpark.udf().register("extractReceipt", extractReceipt, DataTypes.StringType);
    }

    @Override
    public Dataset<Row> transform(Dataset<Row> aSourceDS, HiveTime aTime) {
        if (Objects.isNull(aSourceDS)) {
            throw new IllegalArgumentException("invalid data set provided");
        }
        if (Objects.isNull(aTime)) {
            throw new IllegalArgumentException("invalid time provided");
        }

        final Dataset<Row> purchaseDS = mapPurchaseEvents(aSourceDS);

        return Objects.isNull(segment)
                ? aggregateRevenueMetrics(purchaseDS, aTime)
                : aggregateRevenueMetrics(purchaseDS, segment, aTime);
    }

    private Dataset<Row> mapPurchaseEvents(Dataset<Row> aSourceDS) {
        return aSourceDS.filter((FilterFunction<Row>) row -> HiveProfileEnrichedEventTableSchema.eventType(row) == EventType.PRODUCT_PURCHASED.value())
                .withColumn(COL_REVENUE, functions.callUDF("extractRevenue", aSourceDS.col(HiveProfileEnrichedEventTableSchema.COL_FIELDS.title())))
                .withColumn(COL_RECEIPT, functions.callUDF("extractReceipt", aSourceDS.col(HiveProfileEnrichedEventTableSchema.COL_FIELDS.title())))
                .dropDuplicates(COL_RECEIPT);
    }

    private Dataset<Row> aggregateRevenueMetrics(final Dataset<Row> aActionDS, final HiveTime aTime) {
        RelationalGroupedDataset groupDS = aActionDS.groupBy(HiveProfileEnrichedEventTableSchema.COL_APP_ID.title());
        return aggregateRevenueMetrics(groupDS, null, aTime);
    }

    private Dataset<Row> aggregateRevenueMetrics(final Dataset<Row> aActionDS, String segment, final HiveTime aTime) {
        RelationalGroupedDataset groupDS = aActionDS.groupBy(HiveProfileEnrichedEventTableSchema.COL_APP_ID.title(), segment);
        return aggregateRevenueMetrics(groupDS, segment, aTime);
    }

    private Dataset<Row> aggregateRevenueMetrics(RelationalGroupedDataset groupDS, String segment, final HiveTime aTime) {
        return groupDS
                .agg(
                        functions.sum(COL_REVENUE).cast("string").as(HiveDefines.ValueTag.SUM),
                        functions.max(COL_REVENUE).cast("string").as(HiveDefines.ValueTag.MAX),
                        functions.min(COL_REVENUE).cast("string").as(HiveDefines.ValueTag.MIN)
                )
                .map(
                        new AggregateToMetricRowMapper(AggregateType.SUM | AggregateType.MIN | AggregateType.MAX, actionMetricId, aTime, segment, true),
                        RowEncoder.apply(new HiveMetricTableSchema().schema())
                );
    }
}
