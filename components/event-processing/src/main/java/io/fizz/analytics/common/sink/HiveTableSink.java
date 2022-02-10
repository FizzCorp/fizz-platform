package io.fizz.analytics.common.sink;

import io.fizz.common.LoggingService;
import io.fizz.analytics.common.hive.HiveTableDataSource;
import io.fizz.analytics.common.hive.AbstractHiveTableSchema;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

import java.util.Objects;

public class HiveTableSink extends HiveTableDataSource implements AbstractSink<Row> {
    private static final LoggingService.Log logger = LoggingService.getLogger(HiveTableSink.class);
    private final SaveMode mode;

    public HiveTableSink(final SparkSession aSpark, final AbstractHiveTableSchema aSchema, final String aDataPath, final SaveMode aMode) {
        super(aSpark, aSchema, aDataPath);

        if (Objects.isNull(aMode)) {
            throw new IllegalArgumentException("invalid save mode");
        }
        mode = aMode;
    }

    @Override
    public void put(Dataset<Row> aInputDS) {
        if (Objects.isNull(aInputDS)) {
            logger.warn("Invalid input data set supplied for hive table sink");
            return;
        }

        aInputDS
        .coalesce(1)
        .write()
        .mode(mode)
        .insertInto(getTableName());
    }
}
