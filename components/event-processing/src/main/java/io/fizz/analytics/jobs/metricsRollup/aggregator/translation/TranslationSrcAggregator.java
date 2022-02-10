package io.fizz.analytics.jobs.metricsRollup.aggregator.translation;

import io.fizz.analytics.common.source.hive.HiveDefines;
import io.fizz.analytics.common.source.hive.HiveEventFields;
import io.fizz.analytics.common.source.hive.HiveProfileEnrichedEventTableSchema;
import io.fizz.analytics.jobs.metricsRollup.aggregator.AggregateType;
import org.apache.spark.sql.*;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;
import org.json.JSONObject;

import java.util.*;

public class TranslationSrcAggregator extends AbstractTranslationAggregator {
    private static UDF1 extractSrc = new UDF1<String,String>() {
        public String call(final String fieldsStr){
            final JSONObject fields = new JSONObject(fieldsStr);
            return fields.has(HiveEventFields.TRANS_TEXT_LEN.value())
                    ? fields.getString(HiveEventFields.TRANS_TEXT_LEN.value()) : "??";
        }
    };

    private static final String COL_SRC = "src";

    public TranslationSrcAggregator(final SparkSession aSpark, final HiveDefines.MetricId aActionMetricId, String aSegment) {
        super(aSpark, aActionMetricId, aSegment);

        if (Objects.isNull(aSpark)) {
            throw new IllegalArgumentException("Invalid spark session specified.");
        }

        aSpark.udf().register("extractSrc", extractSrc, DataTypes.StringType);
    }

    @Override
    List<String> groupColumns() {
        return new ArrayList<String>() {
            {
                add(COL_SRC);
            }
        };
    }

    @Override
    Dataset<Row> mapTranslationEvents(Dataset<Row> aSourceDS) {
        return super.mapTranslationEvents(aSourceDS)
                .withColumn(COL_SRC, functions.callUDF("extractSrc", aSourceDS.col(HiveProfileEnrichedEventTableSchema.COL_FIELDS.title())));
    }
}
