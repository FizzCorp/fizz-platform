package io.fizz.analytics.jobs.metricsRollup.common;

import io.fizz.analytics.common.source.hive.HiveDefines;
import io.fizz.analytics.common.source.hive.HiveKeywordsTableSchema;
import io.fizz.analytics.jobs.AbstractMockTableDataSource;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockKeywordsStore extends AbstractMockTableDataSource {
    public MockKeywordsStore(final SparkSession aSpark) {
        super(aSpark);
    }

    @Override
    protected List<Row> testDataFactory() {
        return new ArrayList<Row>(){
            {
                add(new GenericRowWithSchema(new Object[]{
                        "keyword1", 0.2, 0.1, 0.0, 0.0, 0.0, 0.0, "appA", "2018", "2018-04", "2018-04-16"
                }, schema));
                add(new GenericRowWithSchema(new Object[]{
                        "keyword2", 0.6, 0.1, 0.0, 0.0, 0.0, 0.0, "appA", "2018", "2018-04", "2018-04-16"
                }, schema));
                add(new GenericRowWithSchema(new Object[]{
                        "keyword3", -0.2, 0.1, 0.0, 0.0, 0.0, 0.0, "appA", "2018", "2018-04", "2018-04-16"
                }, schema));
                add(new GenericRowWithSchema(new Object[]{
                        "keyword4", 0.0, 0.1, 0.0, 0.0, 0.0, 0.0, "appA", "2018", "2018-04", "2018-04-16"
                }, schema));
                add(new GenericRowWithSchema(new Object[]{
                        "keyword5", 0.2, 0.1, 0.0, 0.0, 0.0, 0.0, "appB", "2018", "2018-04", "2018-04-16"
                }, schema));
            }
        };
    }

    @Override
    protected Map<MetricValidator.Key, MetricValidator> validatorsFactory() {
        return new HashMap<MetricValidator.Key,MetricValidator>() {
            {
                put(
                        new MetricValidator.Key("appA", null, null, HiveDefines.MetricId.SENTIMENT_POSITIVE_DAILY.value()),
                        new MetricValidator().count(2.0)
                );
                put(
                        new MetricValidator.Key("appA", null, null, HiveDefines.MetricId.SENTIMENT_NEGATIVE_DAILY.value()),
                        new MetricValidator().count(1.0)
                );
                put(
                        new MetricValidator.Key("appA", null, null, HiveDefines.MetricId.SENTIMENT_NEUTRAL_DAILY.value()),
                        new MetricValidator().count(1.0)
                );
                put(
                        new MetricValidator.Key("appB", null, null, HiveDefines.MetricId.SENTIMENT_POSITIVE_DAILY.value()),
                        new MetricValidator().count(1.0)
                );
                put(
                        new MetricValidator.Key("appA", null, null, HiveDefines.MetricId.SENTIMENT_POSITIVE_MONTHLY.value()),
                        new MetricValidator().count(2.0)
                );
                put(
                        new MetricValidator.Key("appA", null, null, HiveDefines.MetricId.SENTIMENT_NEGATIVE_MONTHLY.value()),
                        new MetricValidator().count(1.0)
                );
                put(
                        new MetricValidator.Key("appA", null, null, HiveDefines.MetricId.SENTIMENT_NEUTRAL_MONTHLY.value()),
                        new MetricValidator().count(1.0)
                );
                put(
                        new MetricValidator.Key("appB", null, null, HiveDefines.MetricId.SENTIMENT_POSITIVE_MONTHLY.value()),
                        new MetricValidator().count(1.0)
                );
            }
        };
    }

    @Override
    protected StructType schemaFactory() {
        return new HiveKeywordsTableSchema().schema();
    }
}
