package io.fizz.analytics.common.hive;

import io.fizz.analytics.common.*;
import org.apache.spark.sql.*;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.types.StructType;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;

public class HiveTableDataSource implements AbstractTableDataSource {
    private final SparkSession spark;
    private final AbstractHiveTableSchema schema;

    public HiveTableDataSource(SparkSession spark, AbstractHiveTableSchema schema, String dataPath) {
        if (Objects.isNull(spark)) {
            throw new IllegalArgumentException("spark instance needs to be specified.");
        }
        if (Objects.isNull(schema)) {
            throw new IllegalArgumentException("schema must be specified for data source");
        }
        if (Objects.isNull(dataPath)) {
            throw new IllegalArgumentException("invalid data path specified for hive table");
        }

        this.spark = spark;
        this.schema = schema;

        spark.sql(schema.genDropSQL());
        spark.sql(schema.genCreateSQL(dataPath));
        if (schema.isPartitioned()) {
            spark.sql(schema.genLoadDataSQL());
        }
    }

    @Override
    public Dataset<Row> scan() {
        return spark.sql("SELECT * FROM " + schema.getTableName());
    }

    @Override
    public Dataset<Row> scanForDay(final HiveTime aOrigin) {
        String query = String.format("SELECT * FROM %s\n", schema.getTableName()) +
                String.format("WHERE day = '%d' AND month = '%d' AND year = '%d'\n",
                        aOrigin.dayOfMonth.getValue(), aOrigin.month.getValue(), aOrigin.year.getValue());
        return spark.sql(query);
    }

    public Dataset<Row> scan(Year year, Month month, DayOfMonth day) {
        final HiveTime time = new HiveTime(year.getValue(), month.getValue(), day.getValue());
        return scanForRange(time, time);
    }

    @Override
    public Dataset<Row> scanForRange(final HiveTime start, final HiveTime end) {
        final StringBuilder builder = new StringBuilder();
        final String startDate = start.yyyymmmdd();
        final String endDate = end.yyyymmmdd();

        builder.append(String.format("SELECT * FROM %s\n", schema.getTableName()));
        builder.append(String.format("WHERE day BETWEEN '%s' AND '%s'\n", startDate, endDate));

        return spark.sql(builder.toString());
    }

    @Override
    public Dataset<Row> scanForLastDaysFrom(int count, final HiveTime origin) {
        if (Objects.isNull(origin)) {
            throw new IllegalArgumentException("Invalid origin specified");
        }

        final int endYear = origin.year.getValue();
        final int endMonth = origin.month.getValue();
        final int endDay = origin.dayOfMonth.getValue();
        final HiveTime end = new HiveTime(endYear, endMonth, endDay);
        final HiveTime start = end.addDays(-1*count);

        return scanForRange(start, end);
    }

    @Override
    public Dataset<Row> scanForCurrentMonth(HiveTime aOrigin) {
        if (Objects.isNull(aOrigin)) {
            throw new IllegalArgumentException("Invalid origin specified");
        }

        final int endYear = aOrigin.year.getValue();
        final int endMonth = aOrigin.month.getValue();
        final int endDay = aOrigin.dayOfMonth.getValue();

        final HiveTime end = new HiveTime(endYear, endMonth, endDay);
        final HiveTime start = end.addDays(-1*(endDay-1));

        return scanForRange(start, end);
    }

    @Override
    public Dataset<Row> scanForMonthWindow(final HiveTime aOrigin, final int aStartDay) {
        if (Objects.isNull(aOrigin)) {
            throw new IllegalArgumentException("Invalid origin specified");
        }

        final int startDay = 16;

        final int endYear = aOrigin.year.getValue();
        final int endMonth = aOrigin.month.getValue();
        final int endDay = aOrigin.dayOfMonth.getValue();

        final HiveTime end = new HiveTime(endYear, endMonth, endDay);

        final HiveTime start = endDay < startDay ?
                        end.previousMonth(startDay) :
                        new HiveTime(endYear, endMonth, startDay);

        return scanForRange(start, end);
    }

    @Override
    public String getTableName() {
        return schema.getTableName();
    }

    public AbstractHiveTableSchema getSchema() {
        return schema;
    }

    @Override
    public StructType describe() {
        return spark.sql(String.format("SELECT * FROM %s LIMIT 1", schema.getTableName())).schema();
    }

    @Override
    public Encoder<Row> getEncoder() {
        return RowEncoder.apply(describe());
    }
}
