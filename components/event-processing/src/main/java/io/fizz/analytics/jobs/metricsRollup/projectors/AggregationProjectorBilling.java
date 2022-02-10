package io.fizz.analytics.jobs.metricsRollup.projectors;

import io.fizz.analytics.common.AbstractTransformer;
import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.hive.AbstractTableDataSource;
import io.fizz.analytics.common.sink.AbstractSink;
import io.fizz.common.LoggingService;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AggregationProjectorBilling {
    private static final LoggingService.Log logger = LoggingService.getLogger(AggregationProjectorBilling.class);

    private AbstractTableDataSource source;
    private String uniqueColumn;
    private List<AbstractSink<Row>> sinks = new ArrayList<>();
    private HiveTime time;
    private Dataset<Row> monthlyMetricDS;
    private Dataset<Row> midMonthlyMetricDS;

    public AggregationProjectorBilling(final AbstractTableDataSource aSource, final String aUniqueColumn, final HiveTime aTime) {
        if (Objects.isNull(aSource)) {
            throw new IllegalArgumentException("invalid data source");
        }
        if (Objects.isNull(aUniqueColumn)) {
            throw new IllegalArgumentException("invalid unique column");
        }
        if (Objects.isNull(aTime)) {
            throw new IllegalArgumentException("invalid time");
        }
        source = aSource;
        uniqueColumn = aUniqueColumn;
        time = aTime;

        monthlyMetricDS = createSourceDS(Grain.MONTHLY, source, time);
        midMonthlyMetricDS = createSourceDS(Grain.MID_MONTHLY, source, time);
        monthlyMetricDS.persist();
    }

    public void cleanup() {
        monthlyMetricDS.unpersist();
    }

    public void addSink(AbstractSink<Row> aSink) {
        if (!Objects.isNull(aSink)) {
            sinks.add(aSink);
        }
    }

    public void project(final Grain aGrain, final AbstractTransformer<Row, Row> aTransformer) {
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

        Dataset<Row> inputDS = (aGrain == Grain.MONTHLY ? monthlyMetricDS : midMonthlyMetricDS);
        Dataset<Row> metricsDS = aTransformer.transform(inputDS, time);

        for (AbstractSink<Row> sink: sinks) {
            sink.put(metricsDS);
        }
    }

    private Dataset<Row> createSourceDS(final Grain aGrain, final AbstractTableDataSource aSource, final HiveTime aTime) {
        Dataset<Row> ds;
        if (aGrain == Grain.MONTHLY) {
            ds = aSource.scanForCurrentMonth(aTime);
        }
        else if (aGrain == Grain.MID_MONTHLY) {
            ds = aSource.scanForMonthWindow(aTime, 16);
        }
        else {
            throw new IllegalArgumentException("Invalid grain specified.");
        }
        return ds.dropDuplicates(uniqueColumn);
    }
}
