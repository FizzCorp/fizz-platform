package io.fizz.analytics.jobs.metricsRollup.common;

import io.fizz.analytics.common.source.hive.HiveDefines;
import io.fizz.analytics.common.source.hive.HiveLogTableSchema;
import io.fizz.analytics.jobs.AbstractMockTableDataSource;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockLogStore extends AbstractMockTableDataSource {
    public MockLogStore(final SparkSession aSpark) {
        super(aSpark);
    }

    @Override
    protected List<Row> testDataFactory() {
        return new ArrayList<Row>() {
            {
                add(new GenericRowWithSchema(new Object[]{
                        "log", "http", "/api/s2s/v2/create_anonymous_user", 201, "userA", "appA", "2018", "2018-04", "2018-04-15"
                }, schema));
                add(new GenericRowWithSchema(new Object[]{
                        "log", "http", "/api/s2s/v2/create_anonymous_user", 200, "userA", "appA", "2018", "2018-04", "2018-04-15"
                }, schema));
                add(new GenericRowWithSchema(new Object[]{
                        "log", "http", "/api/s2s/v2/apps/users", 201, "userC", "appA", "2018", "2018-04", "2018-04-15"
                }, schema));
                add(new GenericRowWithSchema(new Object[]{
                        "log", "http", "/api/s2s/v2/apps/users", 200, "userC", "appA", "2018", "2018-04", "2018-04-15"
                }, schema));
                add(new GenericRowWithSchema(new Object[]{
                        "log", "http", "/api/s2s/v2/apps/users", 201, "userD", "appB", "2018", "2018-04", "2018-04-15"
                }, schema));
                add(new GenericRowWithSchema(new Object[]{
                        "log", "http", "/api/test", 201, "userE", "appA", "2018", "2018-04", "2018-04-15"
                }, schema));
            }
        };
    }

    @Override
    protected Map<MetricValidator.Key, MetricValidator> validatorsFactory() {
        return new HashMap<MetricValidator.Key,MetricValidator>() {
            {
                put(
                        new MetricValidator.Key("appA", null, null, HiveDefines.MetricId.NEW_USERS_DAILY.value()),
                        new MetricValidator().count(2.0)
                );
                put(
                        new MetricValidator.Key("appB", null, null, HiveDefines.MetricId.NEW_USERS_DAILY.value()),
                        new MetricValidator().count(1.0)
                );
                put(
                        new MetricValidator.Key("appA", null, null, HiveDefines.MetricId.NEW_USERS_MONTHLY.value()),
                        new MetricValidator().count(2.0).mean(2.0).min(2.0).max(2.0)
                );
                put(
                        new MetricValidator.Key("appB", null, null, HiveDefines.MetricId.NEW_USERS_MONTHLY.value()),
                        new MetricValidator().count(1.0).mean(1.0).min(1.0).max(1.0)
                );
            }
        };
    }

    @Override
    protected StructType schemaFactory() {
        return new HiveLogTableSchema().schema();
    }
}
