package io.fizz.analytics.jobs.eventsETL;

import io.fizz.analytics.common.source.hive.HiveDefines;
import io.fizz.analytics.common.source.hive.HiveTransTableSchema;
import io.fizz.analytics.jobs.AbstractMockTableDataSource;
import io.fizz.analytics.jobs.metricsRollup.common.MetricValidator;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockTranslationStore extends AbstractMockTableDataSource {
    public MockTranslationStore(final SparkSession aSpark) {
        super(aSpark);
    }

    @Override
    protected List<Row> testDataFactory() {
        return new ArrayList<Row>() {
            {
                add(new GenericRowWithSchema(new Object[]{
                        "trans", "appA", "en", "fr", 8, "2018", "2018-04", "2018-04-15"
                }, schema));
                add(new GenericRowWithSchema(new Object[]{
                        "trans", "appA", "fr", "en", 10, "2018", "2018-04", "2018-04-15"
                }, schema));
                add(new GenericRowWithSchema(new Object[]{
                        "trans", "appB", "en", "fr", 20, "2018", "2018-04", "2018-04-15"
                }, schema));
            }
        };
    }

    @Override
    protected Map<MetricValidator.Key, MetricValidator> validatorsFactory() {
        return new HashMap<MetricValidator.Key,MetricValidator>() {
            {
                put(
                        new MetricValidator.Key("appA", null, null, HiveDefines.MetricId.CHARS_TRANSLATED_MONTHLY.value()),
                        new MetricValidator().sum(18.0)
                );
                put(
                        new MetricValidator.Key("appB", null, null, HiveDefines.MetricId.CHARS_TRANSLATED_MONTHLY.value()),
                        new MetricValidator().sum(20.0)
                );
                put(
                        new MetricValidator.Key("appA", null, null, HiveDefines.MetricId.CHARS_TRANSLATED_DAILY.value()),
                        new MetricValidator().sum(18.0)
                );
                put(
                        new MetricValidator.Key("appB", null, null, HiveDefines.MetricId.CHARS_TRANSLATED_DAILY.value()),
                        new MetricValidator().sum(20.0)
                );
            }
        };
    }

    @Override
    protected StructType schemaFactory() {
        return new HiveTransTableSchema().schema();
    }
}
