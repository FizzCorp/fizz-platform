package io.fizz.analytics.common.projections;

import io.fizz.analytics.common.source.hive.HiveLogTableSchema;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.sql.Row;

import java.util.regex.Pattern;

public class NewUserLogFilter implements FilterFunction<Row> {
    private static final int CREATED_RESPONSE = 201;
    private static final Pattern oldApiPattern = Pattern.compile("/api/.*/create_anonymous_user");
    private static final Pattern newApiPattern = Pattern.compile("/api/.*/apps/users");

    @Override
    public boolean call(Row aRow) {
        final String fieldValue = aRow.getString(aRow.fieldIndex(HiveLogTableSchema.COL_API_PATH));
        final int status = aRow.getInt(aRow.fieldIndex(HiveLogTableSchema.COL_RESPONSE_CODE));

        if (status != CREATED_RESPONSE) {
            return false;
        }

        return oldApiPattern.matcher(fieldValue).find() || newApiPattern.matcher(fieldValue).find();
    }
}
