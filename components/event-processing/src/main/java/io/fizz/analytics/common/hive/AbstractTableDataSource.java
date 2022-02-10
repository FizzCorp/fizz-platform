package io.fizz.analytics.common.hive;

import io.fizz.analytics.common.HiveTime;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructType;

public interface AbstractTableDataSource {
    Dataset<Row> scan();
    Dataset<Row> scanForDay(final HiveTime aOrigin);
    Dataset<Row> scanForRange(final HiveTime aStart, final HiveTime aEnd);
    Dataset<Row> scanForLastDaysFrom(int aCount, final HiveTime aOrigin);
    Dataset<Row> scanForCurrentMonth(final HiveTime aOrigin);
    Dataset<Row> scanForMonthWindow(final HiveTime aOrigin, final int aStartDay);
    String getTableName();
    StructType describe();
    Encoder<Row> getEncoder();
}
