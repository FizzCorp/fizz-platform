package io.fizz.analytics.jobs.eventsETL;

import io.fizz.analytics.common.source.hive.HiveDefines;
import io.fizz.analytics.common.source.hive.HiveSessionTableSchema;
import io.fizz.analytics.jobs.AbstractMockTableDataSource;
import io.fizz.analytics.jobs.metricsRollup.common.MetricValidator;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.StructType;

import java.util.*;

public class MockSessionStore extends AbstractMockTableDataSource {
    @Override
    protected List<Row> testDataFactory() {
        return new ArrayList<Row>(){
            {
                add(new GenericRowWithSchema(new Object[]{
                        "session", "appA", "userA", "userA", "session_end", "session_1", "1517516490202", "1517516490764", "CA", "", 10, "2018", "2018-04", "2018-04-15"
                }, schema));
                add(new GenericRowWithSchema(new Object[]{
                        "session", "appA", "userA", "userA", "session_end", "session_2", "1517516490202", "1517516490764", "CA", "", 10, "2018", "2018-04", "2018-04-15"
                }, schema));
                add(new GenericRowWithSchema(new Object[]{
                        "session", "appA", "userB", "userB", "session_start", "session_3", "1517516490202", "1517516490764", "CA", "", 10, "2018", "2018-04", "2018-04-15"
                }, schema));
                add(new GenericRowWithSchema(new Object[]{
                        "session", "appA", "userB", "userB", "session_end", "session_3", "1517516490202", "1517516490764", "CA", "", 10, "2018", "2018-04", "2018-04-15"
                }, schema));
                add(new GenericRowWithSchema(new Object[]{
                        "session", "appB", "userC", "userC", "session_end", "session_4", "1517516490202", "1517516490764", "CA", "", 10, "2018", "2018-04", "2018-04-15"
                }, schema));
            }
        };
    }

    @Override
    protected Map<MetricValidator.Key,MetricValidator> validatorsFactory() {
        return new HashMap<MetricValidator.Key,MetricValidator>() {
            {
                put(
                        new MetricValidator.Key("appA", null, null, HiveDefines.MetricId.USER_SESSIONS_MONTHLY.value()),
                        new MetricValidator().count(3.0).sum(30.0).mean(10.0).min(10.0).max(10.0)
                );
                put(
                        new MetricValidator.Key("appB", null, null, HiveDefines.MetricId.USER_SESSIONS_MONTHLY.value()),
                        new MetricValidator().count(1.0).sum(10.0).mean(10.0).min(10.0).max(10.0)
                );
                put(
                        new MetricValidator.Key("appA", null, null, HiveDefines.MetricId.ACTIVE_USERS_MONTHLY.value()),
                        new MetricValidator().count(2.0)
                );
                put(
                        new MetricValidator.Key("appB", null, null, HiveDefines.MetricId.ACTIVE_USERS_MONTHLY.value()),
                        new MetricValidator().count(1.0)
                );
                put(
                        new MetricValidator.Key("appA", null, null, HiveDefines.MetricId.USER_SESSIONS_DAILY.value()),
                        new MetricValidator().count(3.0).sum(30.0).mean(10.0).min(10.0).max(10.0)
                );
                put(
                        new MetricValidator.Key("appB", null, null, HiveDefines.MetricId.USER_SESSIONS_DAILY.value()),
                        new MetricValidator().count(1.0).sum(10.0).mean(10.0).min(10.0).max(10.0)
                );
                put(
                        new MetricValidator.Key("appA", null, null, HiveDefines.MetricId.ACTIVE_USERS_DAILY.value()),
                        new MetricValidator().count(2.0)
                );
                put(
                        new MetricValidator.Key("appB", null, null, HiveDefines.MetricId.ACTIVE_USERS_DAILY.value()),
                        new MetricValidator().count(1.0)
                );
            }
        };
    }

    @Override
    protected StructType schemaFactory() {
        return new HiveSessionTableSchema().schema();
    }

    public MockSessionStore(final SparkSession aSpark) {
        super(aSpark);
    }
}
