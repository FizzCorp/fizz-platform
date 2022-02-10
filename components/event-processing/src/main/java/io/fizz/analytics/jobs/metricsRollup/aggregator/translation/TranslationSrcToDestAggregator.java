package io.fizz.analytics.jobs.metricsRollup.aggregator.translation;

import io.fizz.analytics.common.source.hive.HiveDefines;
import io.fizz.analytics.common.source.hive.HiveTranslationTableSchema;
import io.fizz.analytics.jobs.metricsRollup.aggregator.AggregateType;
import io.fizz.analytics.jobs.metricsRollup.transformer.TranslationEventTransformer;
import org.apache.spark.sql.*;

import java.util.Arrays;
import java.util.List;

public class TranslationSrcToDestAggregator extends AbstractTranslationAggregator {
    public TranslationSrcToDestAggregator(final SparkSession aSpark, final HiveDefines.MetricId aActionMetricId, String aSegment) {
        super(aSpark, aActionMetricId, aSegment);
    }

    @Override
    List<String> groupColumns(){
        return Arrays.asList(HiveTranslationTableSchema.COL_SRC.title(), HiveTranslationTableSchema.COL_DEST.title());
    }

    @Override
    Dataset<Row> mapTranslationEvents(Dataset<Row> aSourceDS) {
        Dataset<Row> translationDS = super.mapTranslationEvents(aSourceDS);
        return new TranslationEventTransformer().transform(translationDS, null);
    }
}
