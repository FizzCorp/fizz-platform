package io.fizz.analytics.jobs.metricsRollup.aggregator.translation;

import io.fizz.analytics.common.source.hive.HiveDefines;
import io.fizz.analytics.common.source.hive.HiveEventFields;
import io.fizz.analytics.common.source.hive.HiveProfileEnrichedEventTableSchema;
import io.fizz.analytics.jobs.metricsRollup.aggregator.AggregateType;
import org.apache.spark.sql.*;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class TranslationCharCountAggregator extends AbstractTranslationAggregator {
    private static UDF1 extractTranslationCharCount = new UDF1<String,Integer>() {
        public Integer call(final String fieldsStr) {
            int charCount = 0, toLocalsCount = 1;

            final JSONObject fields = new JSONObject(fieldsStr);
            if (fields.has(HiveEventFields.TRANS_TEXT_LEN.value())) {
                charCount = fields.has(HiveEventFields.TRANS_TEXT_LEN.value())
                        ? fields.getInt(HiveEventFields.TRANS_TEXT_LEN.value()) : 0;

                String toStr = fields.has(HiveEventFields.TRANS_LANG_TO.value())
                        ? fields.getString(HiveEventFields.TRANS_LANG_TO.value()) : null;
                if (Objects.nonNull(toStr)) {
                    try {
                        toLocalsCount = new JSONArray(toStr).length();
                    } catch (JSONException ignored) { }
                }
            }

            return charCount * toLocalsCount;
        }
    };

    private static final String COL_CHAR_COUNT = "charCount";

    public TranslationCharCountAggregator(final SparkSession aSpark, final HiveDefines.MetricId aActionMetricId, String aSegment) {
        super(aSpark, aActionMetricId, aSegment);

        if (Objects.isNull(aSpark)) {
            throw new IllegalArgumentException("Invalid spark session specified.");
        }

        aSpark.udf().register("extractTranslationCharCount", extractTranslationCharCount, DataTypes.IntegerType);
    }

    @Override
    protected Column aggregatorColumn() {
        return functions.sum(COL_CHAR_COUNT).cast("string").as(HiveDefines.ValueTag.SUM);
    }

    @Override
    int aggregateType() {
        return AggregateType.SUM;
    }

    @Override
    Dataset<Row> mapTranslationEvents(Dataset<Row> aSourceDS) {
        return super.mapTranslationEvents(aSourceDS)
                .withColumn(COL_CHAR_COUNT, functions.callUDF("extractTranslationCharCount", aSourceDS.col(HiveProfileEnrichedEventTableSchema.COL_FIELDS.title())));
    }
}
