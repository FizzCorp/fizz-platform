package io.fizz.analytics.jobs.dateFormatMapper;

import io.fizz.analytics.common.HiveTime;
import io.fizz.common.LoggingService;
import io.fizz.analytics.common.hive.HiveTableDataSource;
import io.fizz.analytics.jobs.AbstractJobExecutor;
import io.fizz.analytics.common.source.hive.*;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;

/**
 * Used for converting the YYYY/MM/DD partitioning format to YYYY/YYYY-MM/YYYY-MM-DD format.
 * This allows for better range querying.
 */
public class Executor extends AbstractJobExecutor {
    private static class DataFormatMapper implements MapFunction<Row,Row> {
        @Override
        public Row call(Row row) throws Exception {
            final int yearIdx = row.fieldIndex("year");
            final int monthIdx = row.fieldIndex("month");
            final int dayIdx = row.fieldIndex("day");

            final int year = Integer.parseInt(row.getString(yearIdx));
            final int month = Integer.parseInt(row.getString(monthIdx));
            final int day = Integer.parseInt(row.getString(dayIdx));

            final HiveTime time = new HiveTime(year, month, day);

            final Object[] fields = new Object[row.size()];
            for (int fi = 0; fi < row.size(); fi++) {
                if (fi == monthIdx) {
                    fields[fi] = time.yyyymmm();
                }
                else
                if (fi == dayIdx) {
                    fields[fi] = time.yyyymmmdd();
                }
                else {
                    fields[fi] = row.get(fi);
                }
            }
            return new GenericRowWithSchema(fields, row.schema());
        }
    }
    static {
        instance = new Executor();
    }

    private static LoggingService.Log logger = LoggingService.getLogger(Executor.class);

    @Override
    public void execute() throws Exception {
        logger.info("=== Running job to export events to new range partitions format");

        final HiveTableDataSource sessionStore = new HiveTableDataSource(spark, new HiveSessionTableSchema(), dataPath + "/eventsV2/session");
        mapDataSource(sessionStore);

        HiveTableDataSource translationStore = new HiveTableDataSource(spark, new HiveTransTableSchema(), dataPath + "/eventsV2/trans");
        mapDataSource(translationStore);

        HiveTableDataSource actionStore = new HiveTableDataSource(spark, new HiveActionTableSchema(), dataPath + "/eventsV2/action");
        mapDataSource(actionStore);

        HiveTableDataSource logStore = new HiveTableDataSource(spark, new HiveLogTableSchema(), dataPath + "/eventsV2/log");
        mapDataSource(logStore);
    }

    protected void mapDataSource(final HiveTableDataSource dataSource) {
        dataSource.scan()
                .map(new DataFormatMapper(), dataSource.getEncoder())
                .coalesce(1)
                .write()
                .mode(SaveMode.Overwrite)
                .insertInto(dataSource.getTableName());
    }

    public static void main (String[] args) throws Exception {
        final Executor instance = new Executor();

        instance.init();
        instance.execute();

        logger.info("=== exiting application");
    }
}
