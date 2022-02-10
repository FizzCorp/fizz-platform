package io.fizz.analytics.jobs.hive2tsdb;

import io.fizz.analytics.common.Utils;
import io.fizz.common.ConfigService;
import io.fizz.analytics.common.HiveTime;
import io.fizz.common.LoggingService;
import io.fizz.analytics.common.hive.HiveTableDataSource;
import io.fizz.analytics.common.sink.TSDBSink;
import io.fizz.analytics.common.source.hive.HiveMetricTableSchema;
import io.fizz.analytics.jobs.AbstractJobExecutor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.net.URL;

/*
 * Exports the metrics from the metrics table to OpenTSDB (for realtime querying)
 */
public class Executor extends AbstractJobExecutor {

    private static LoggingService.Log logger = LoggingService.getLogger(Executor.class);

    @Override
    public void execute() throws Exception {
        logger.info("=== Running hive metrics to TSDB job");

        final HiveTime time = Utils.previousDay();

        final HiveTableDataSource ds = new HiveTableDataSource(spark, new HiveMetricTableSchema(), dataPath + "/" + HiveMetricTableSchema.TABLE_NAME);
        final Dataset<Row> data = ds.scanForDay(time);

        final String host = ConfigService.instance().getString("hive2tsdb.tsdb.host");
        final int port = (int)ConfigService.instance().getNumber("hive2tsdb.tsdb.port");
        final URL endpoint = new URL("http", host, port, "");
        final TSDBSink tsdbSink = new TSDBSink(endpoint);

        logger.info("adding metrics to http://" + host + ":" + port);

        tsdbSink.put(data);
    }

    public static void main (String[] args) throws Exception {
        System.setProperty("es.set.netty.runtime.available.processors", "false");

        final Executor instance = new Executor();

        instance.init();
        instance.execute();

        logger.info("=== exiting application");
    }
}
