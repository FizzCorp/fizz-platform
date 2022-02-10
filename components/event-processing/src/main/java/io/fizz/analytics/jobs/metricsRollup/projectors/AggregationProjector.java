package io.fizz.analytics.jobs.metricsRollup.projectors;

import io.fizz.analytics.common.HiveTime;
import io.fizz.common.LoggingService;
import io.fizz.analytics.common.hive.AbstractTableDataSource;
import io.fizz.analytics.common.sink.AbstractSink;
import io.fizz.analytics.common.AbstractTransformer;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.*;

public class AggregationProjector {
    private static final LoggingService.Log logger = LoggingService.getLogger(AggregationProjector.class);

    private AbstractTableDataSource source;
    private String uniqueColumn;
    private List<AbstractSink<Row>> sinks = new ArrayList<>();
    private HiveTime time;
    private Dataset<Row> dailyMetricsDS;
    private Dataset<Row> monthlyMetricsDS;

    public AggregationProjector(final AbstractTableDataSource aSource, final String aUniqueColumn, final HiveTime aTime) {
        if (Objects.isNull(aSource)) {
            throw new IllegalArgumentException("invalid data source");
        }
        if (Objects.isNull(aTime)) {
            throw new IllegalArgumentException("invalid time");
        }

        source = aSource;
        uniqueColumn = aUniqueColumn;
        time = aTime;

        dailyMetricsDS = createSourceDS(Grain.DAILY, source, time);
        monthlyMetricsDS = createSourceDS(Grain.MONTHLY, source, time);

        dailyMetricsDS.persist();
        monthlyMetricsDS.persist();
    }

    public void cleanup() {
        dailyMetricsDS.unpersist();
        monthlyMetricsDS.unpersist();
    }

    public void addSink(AbstractSink<Row> aSink) {
        if (!Objects.isNull(aSink)) {
            sinks.add(aSink);
        }
    }

    public void project(final Grain aGrain, final AbstractTransformer<Row,Row> aTransformer) {
        if (Objects.isNull(source)) {
            logger.warn("No input store specified for projection.");
            return;
        }
        if (Objects.isNull(sinks)) {
            logger.warn("No output stores specified for projection.");
            return;
        }
        if (Objects.isNull(aTransformer)) {
            logger.warn("No aggregator specified for projection.");
            return;
        }

        Dataset<Row> inputDS = (aGrain == Grain.DAILY ? dailyMetricsDS : monthlyMetricsDS);
        Dataset<Row> metricsDS = aTransformer.transform(inputDS, time);

        for (AbstractSink<Row> sink: sinks) {
            sink.put(metricsDS);
        }
    }

    private Dataset<Row> createSourceDS(Grain aGrain, final AbstractTableDataSource aSource, final HiveTime aTime) {
        Dataset<Row> ds;
        if (aGrain == Grain.DAILY) {
            ds = aSource.scanForLastDaysFrom(0, aTime);
        }
        else if (aGrain == Grain.MONTHLY) {
            ds = aSource.scanForLastDaysFrom(30, aTime);
        }
        else {
            throw new IllegalArgumentException("Invalid grain specified.");
        }

        if (Objects.isNull(uniqueColumn)) {
            return ds;
        }

        return ds.dropDuplicates(uniqueColumn);
    }
}
