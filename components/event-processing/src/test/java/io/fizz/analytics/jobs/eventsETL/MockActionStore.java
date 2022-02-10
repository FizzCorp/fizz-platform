package io.fizz.analytics.jobs.eventsETL;

import io.fizz.analytics.common.source.hive.HiveActionTableSchema;
import io.fizz.analytics.common.source.hive.HiveDefines;
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

public class MockActionStore extends AbstractMockTableDataSource {
    public MockActionStore(final SparkSession aSpark) {
        super(aSpark);
    }

    @Override
    protected List<Row> testDataFactory() {
        return new ArrayList<Row>(){
            {
                add(new GenericRowWithSchema(new Object[]{
                        "action", "appA", "userA", "userA", "1", "roomA", "", "", "1524983609", "", "{\"msg\":\"hello how are you?\"}", "2018", "2018-04", "2018-04-15"
                }, schema));
                add(new GenericRowWithSchema(new Object[]{
                        "action", "appA", "userB", "userB", "1", "roomA", "", "", "1524983609", "", "{\"msg\":\"i am fine thanks.\"}", "2018", "2018-04", "2018-04-15"
                }, schema));
                add(new GenericRowWithSchema(new Object[]{
                        "action", "appB", "userC", "userC", "1", "roomB", "", "", "1524983609", "", "{\"msg\":\"hello\"}", "2018", "2018-04", "2018-04-16"
                }, schema));
                add(new GenericRowWithSchema(new Object[]{
                        "action", "appB", "userD", "userD", "101", "roomB", "", "", "1524983609", "", "", "2018", "2018-04", "2018-04-16"
                }, schema));
            }
        };
    }

    @Override
    protected Map<MetricValidator.Key, MetricValidator> validatorsFactory() {
        return new HashMap<MetricValidator.Key,MetricValidator>() {
            {
                put(
                        new MetricValidator.Key("appA", null, null, HiveDefines.MetricId.CHAT_MSGS_DAILY.value()),
                        new MetricValidator().count(2.0)
                );
                put(
                        new MetricValidator.Key("appB", null, null, HiveDefines.MetricId.CHAT_MSGS_DAILY.value()),
                        new MetricValidator().count(1.0)
                );
                put(
                        new MetricValidator.Key("appA", null, null, HiveDefines.MetricId.CHAT_MSGS_MONTHLY.value()),
                        new MetricValidator().count(2.0)
                );
                put(
                        new MetricValidator.Key("appB", null, null, HiveDefines.MetricId.CHAT_MSGS_MONTHLY.value()),
                        new MetricValidator().count(1.0)
                );
            }
        };
    }

    @Override
    protected StructType schemaFactory() {
        return new HiveActionTableSchema().schema();
    }
}
