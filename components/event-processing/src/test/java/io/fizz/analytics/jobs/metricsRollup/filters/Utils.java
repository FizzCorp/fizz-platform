package io.fizz.analytics.jobs.metricsRollup.filters;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

public class Utils {
    public static Row createSessionRow(final Object[] values) {
        final StructType schema = DataTypes.createStructType(
                new StructField[] {
                        DataTypes.createStructField("s_type", DataTypes.StringType, false),
                        DataTypes.createStructField("year", DataTypes.StringType, false),
                        DataTypes.createStructField("month", DataTypes.StringType, false),
                        DataTypes.createStructField("day", DataTypes.StringType, false)
                }
        );

        return new GenericRowWithSchema(values, schema);
    }

    public static Row createUserLogRow(final Object[] values) {
        final StructType schema = DataTypes.createStructType(
                new StructField[] {
                        DataTypes.createStructField("path", DataTypes.StringType, false),
                        DataTypes.createStructField("year", DataTypes.StringType, false),
                        DataTypes.createStructField("month", DataTypes.StringType, false),
                        DataTypes.createStructField("day", DataTypes.StringType, false)
                }
        );

        return new GenericRowWithSchema(values, schema);
    }
}
