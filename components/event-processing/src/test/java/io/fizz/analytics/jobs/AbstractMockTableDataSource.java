package io.fizz.analytics.jobs;

import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.hive.AbstractTableDataSource;
import io.fizz.analytics.common.source.hive.HiveMetricTableSchema;
import io.fizz.analytics.jobs.metricsRollup.common.MetricValidator;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.types.StructType;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractMockTableDataSource implements AbstractTableDataSource {
    protected final SparkSession spark;
    private final List<Row> testData;
    private final Map<MetricValidator.Key,MetricValidator> validators;
    protected final StructType schema;

    public AbstractMockTableDataSource(final SparkSession aSpark) {
        if (Objects.isNull(aSpark)) {
            throw new IllegalArgumentException("invalid spark session specified.");
        }
        spark = aSpark;
        schema = schemaFactory();
        testData = testDataFactory();
        validators = validatorsFactory();
    }

    protected abstract List<Row> testDataFactory();
    protected abstract Map<MetricValidator.Key,MetricValidator> validatorsFactory();
    protected abstract StructType schemaFactory();

    public void validate(final Dataset<Row> aMetricsDS) {
        final List<Row> rows = aMetricsDS.collectAsList();

        for (final Row row: rows) {
            final String metricId = row.getString(row.fieldIndex(HiveMetricTableSchema.COL_TYPE.title()));
            final String appId = row.getString(row.fieldIndex(HiveMetricTableSchema.COL_APP_ID.title()));
            final String dim = row.getString(row.fieldIndex(HiveMetricTableSchema.COL_DIM.title()));
            final String dimValue = row.getString(row.fieldIndex(HiveMetricTableSchema.COL_DIM_VALUE.title()));
            final MetricValidator.Key key = new MetricValidator.Key(appId, dim, dimValue, metricId);
            final MetricValidator validator = validators.get(key);

            assert (validator != null);
            validator.validate(row, dim);
        }
    }

    @Override
    public Dataset<Row> scan() {
        return spark.createDataset(testData, RowEncoder.apply(schema));
    }

    @Override
    public Dataset<Row> scanForDay(HiveTime aOrigin) {
        return scan();
    }

    @Override
    public Dataset<Row> scanForCurrentMonth(HiveTime aOrigin) {
        return scan();
    }

    @Override
    public Dataset<Row> scanForRange(HiveTime start, HiveTime end) {
        return scan();
    }

    @Override
    public Dataset<Row> scanForLastDaysFrom(int count, HiveTime origin) {
        return scan();
    }

    @Override
    public Dataset<Row> scanForMonthWindow(final HiveTime aOrigin, final int aStartDay) {
        return scan();
    }

    @Override
    public String getTableName() {
        return "session";
    }

    @Override
    public StructType describe() {
        return null;
    }

    @Override
    public Encoder<Row> getEncoder() {
        return null;
    }
}
