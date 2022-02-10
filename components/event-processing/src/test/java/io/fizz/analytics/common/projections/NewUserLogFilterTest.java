package io.fizz.analytics.common.projections;

import io.fizz.analytics.common.source.hive.HiveLogTableSchema;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NewUserLogFilterTest {
    private final Row userCreatedRowV1 = new GenericRowWithSchema(new Object[]{
        "log", "http", "/api/s2s/v2/create_anonymous_user", 201, "userA", "appA", "2018", "2018-04", "2018-04-15"
    }, new HiveLogTableSchema().schema());
    private final Row userFoundRowV1 = new GenericRowWithSchema(new Object[]{
            "log", "http", "/api/s2s/v2/create_anonymous_user", 200, "userA", "appA", "2018", "2018-04", "2018-04-15"
    }, new HiveLogTableSchema().schema());
    private final Row userCreatedRowV2 = new GenericRowWithSchema(new Object[]{
            "log", "http", "/api/s2s/v2/apps/users", 201, "userA", "appA", "2018", "2018-04", "2018-04-15"
    }, new HiveLogTableSchema().schema());
    private final Row userFoundRowV2 = new GenericRowWithSchema(new Object[]{
            "log", "http", "/api/s2s/v2/apps/users", 200, "userA", "appA", "2018", "2018-04", "2018-04-15"
    }, new HiveLogTableSchema().schema());
    private final Row invalidRow = new GenericRowWithSchema(new Object[]{
            "log", "http", "/api/test", 201, "userA", "appA", "2018", "2018-04", "2018-04-15"
    }, new HiveLogTableSchema().schema());

    @Test
    @DisplayName("it should filter correct API logs")
    void filterTest() {
        final NewUserLogFilter filter = new NewUserLogFilter();
        assert (filter.call(userCreatedRowV1));
        assert (!filter.call(userFoundRowV1));
        assert (filter.call(userCreatedRowV2));
        assert (!filter.call(userFoundRowV2));
        assert (!filter.call(invalidRow));
    }
}
