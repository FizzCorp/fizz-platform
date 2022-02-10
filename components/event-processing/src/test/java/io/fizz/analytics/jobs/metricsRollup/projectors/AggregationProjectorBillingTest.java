package io.fizz.analytics.jobs.metricsRollup.projectors;

import io.fizz.analytics.AbstractSparkTest;
import io.fizz.analytics.common.AbstractTransformer;
import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.hive.AbstractTableDataSource;
import io.fizz.analytics.common.sink.AbstractSink;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class AggregationProjectorBillingTest extends AbstractSparkTest {
    private static class MockTableDataSource implements AbstractTableDataSource {
        private final SparkSession spark;

        public final StructType schema = DataTypes.createStructType(new StructField[]{
            DataTypes.createStructField("id", DataTypes.StringType, false),
            DataTypes.createStructField("value", DataTypes.StringType, true)
        });

        public MockTableDataSource(final SparkSession aSpark) {
            spark = aSpark;
        }

        @Override
        public Dataset<Row> scan() {
            final List<Row> rows = new ArrayList<Row>() {
                {
                    add(new GenericRowWithSchema(new Object[]{"id1", "value1"}, schema));
                    add(new GenericRowWithSchema(new Object[]{"id2", "value2"}, schema));
                    add(new GenericRowWithSchema(new Object[]{"id3", "value3"}, schema));
                    add(new GenericRowWithSchema(new Object[]{"id3", "value4"}, schema));
                }
            };
            return spark.createDataset(rows, RowEncoder.apply(schema));
        }

        @Override
        public Dataset<Row> scanForDay(HiveTime aOrigin) {
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
        public Dataset<Row> scanForMonthWindow(HiveTime origin, int startTime) {
            return scan();
        }

        @Override
        public Dataset<Row> scanForCurrentMonth(HiveTime aOrigin) {
            return scan();
        }

        @Override
        public String getTableName() {
            return null;
        }

        @Override
        public StructType describe() {
            return null;
        }

        @Override
        public Encoder<Row> getEncoder() {
            return RowEncoder.apply(schema);
        }
    }

    private static class MockTransformer implements AbstractTransformer<Row,Row> {
        @Override
        public Dataset<Row> transform(Dataset<Row> sourceDS, HiveTime time) {
            return sourceDS;
        }
    }

    private static class MockSink implements AbstractSink<Row> {
        @Override
        public void put(Dataset<Row> ds) {
            final List<Row> rows = ds.collectAsList();
            assert (rows.size() == 3);
            for (final Row row: rows) {
                final String key = row.getString(0);
                final String value = row.getString(1);

                assert (key.equals("id1") || key.equals("id2") || key.equals("id3"));
                assert (value.equals("value1") || value.equals("value2") || value.equals("value3"));
            }
        }
    }

    @Test
    @DisplayName("it should run basic projection")
    void monthlyGrainBasicValidityTest() {
        final AggregationProjectorBilling projector = new AggregationProjectorBilling(new MockTableDataSource(spark), "id", new HiveTime(2018, 4, 16));
        projector.addSink(new MockSink());
        projector.project(Grain.MONTHLY, new MockTransformer());
    }

    @Test
    @DisplayName("it should run basic projection")
    void midMonthlyGrainBasicValidityTest() {
        final AggregationProjectorBilling projector = new AggregationProjectorBilling(new MockTableDataSource(spark), "id", new HiveTime(2018, 4, 30));
        projector.addSink(new MockSink());
        projector.project(Grain.MID_MONTHLY, new MockTransformer());
    }
}
