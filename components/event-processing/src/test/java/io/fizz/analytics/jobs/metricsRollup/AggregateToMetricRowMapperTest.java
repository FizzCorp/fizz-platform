package io.fizz.analytics.jobs.metricsRollup;

import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.source.hive.HiveDefines;
import io.fizz.analytics.common.source.hive.HiveProfileEnrichedEventTableSchema;
import io.fizz.analytics.jobs.metricsRollup.aggregator.AggregateToMetricRowMapper;
import io.fizz.analytics.jobs.metricsRollup.aggregator.AggregateType;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AggregateToMetricRowMapperTest {
    private final String appId = "01ddef0034e0432";
    private final String countValue = "3345";
    private final String sumValue = "2323";
    private final String meanValue = "1500.453";
    private final String maxValue = "4356";
    private final String minValue = "657";
    private final HiveDefines.MetricId metricId = HiveDefines.MetricId.ACTIVE_USERS_DAILY;
    private final HiveTime time = new HiveTime(2018, 1, 15);

    @Test
    public void itShouldMapAggregateRowWithOnlyCount() throws Exception {
        final AggregateToMetricRowMapper mapper = new AggregateToMetricRowMapper(AggregateType.COUNT, metricId, time, null, true);

        final Row row = createAggregateRow(appId, new HashMap<String,Object>() {
            {
                put(HiveDefines.ValueTag.COUNT, countValue);
            }
        });

        final Row mappedRow = mapper.call(row);

        validateMappedRow(AggregateType.COUNT, mappedRow);
    }

    @Test
    public void itShouldMapAggregateRowWithOnlySum() throws Exception {
        final AggregateToMetricRowMapper mapper = new AggregateToMetricRowMapper(AggregateType.SUM, metricId, time, null, true);

        final Row row = createAggregateRow(appId, new HashMap<String,Object>() {
            {
                put(HiveDefines.ValueTag.SUM, sumValue);
            }
        });

        final Row mappedRow = mapper.call(row);

        validateMappedRow(AggregateType.SUM, mappedRow);
    }

    @Test
    public void itShouldMapAggregateRowWithOnlyMean() throws Exception {
        final AggregateToMetricRowMapper mapper = new AggregateToMetricRowMapper(AggregateType.MEAN, metricId, time, null, true);

        final Row row = createAggregateRow(appId, new HashMap<String,Object>() {
            {
                put(HiveDefines.ValueTag.MEAN, meanValue);
            }
        });

        final Row mappedRow = mapper.call(row);

        validateMappedRow(AggregateType.MEAN, mappedRow);
    }

    @Test
    public void itShouldMapAggregateRowWithOnlyMin() throws Exception {
        final AggregateToMetricRowMapper mapper = new AggregateToMetricRowMapper(AggregateType.MIN, metricId, time, null, true);

        final Row row = createAggregateRow(appId, new HashMap<String,Object>() {
            {
                put(HiveDefines.ValueTag.MIN, minValue);
            }
        });

        final Row mappedRow = mapper.call(row);

        validateMappedRow(AggregateType.MIN, mappedRow);
    }

    @Test
    public void itShouldMapAggregateRowWithOnlyMax() throws Exception {
        final AggregateToMetricRowMapper mapper = new AggregateToMetricRowMapper(AggregateType.MAX, metricId, time, null, true);

        final Row row = createAggregateRow(appId, new HashMap<String,Object>() {
            {
                put(HiveDefines.ValueTag.MAX, maxValue);
            }
        });

        final Row mappedRow = mapper.call(row);

        validateMappedRow(AggregateType.MAX, mappedRow);
    }

    @Test
    public void itShouldMapAggregateRowWithAllAggregates() throws Exception {
        final AggregateToMetricRowMapper mapper = new AggregateToMetricRowMapper(AggregateType.ALL, metricId, time, null, true);

        final Row row = createAggregateRow(appId, new HashMap<String,Object>() {
            {
                put(HiveDefines.ValueTag.COUNT, countValue);
                put(HiveDefines.ValueTag.SUM, sumValue);
                put(HiveDefines.ValueTag.MEAN, meanValue);
                put(HiveDefines.ValueTag.MIN, minValue);
                put(HiveDefines.ValueTag.MAX, maxValue);
            }
        });

        final Row mappedRow = mapper.call(row);

        validateMappedRow(AggregateType.ALL, mappedRow);
    }

    private void validateMappedRow(int aggregateType, final Row row) throws Exception {
        int fieldIdx = 0;
        assert(row.getString(fieldIdx++).equals(metricId.value()));

        if ((aggregateType & AggregateType.COUNT) != 0) {
            assert(row.getString(fieldIdx++).equals(HiveDefines.ValueTag.COUNT));
            assert(row.getString(fieldIdx++).equals(countValue));
        }
        if ((aggregateType & AggregateType.SUM) != 0) {
            assert(row.getString(fieldIdx++).equals(HiveDefines.ValueTag.SUM));
            assert(row.getString(fieldIdx++).equals(sumValue));
        }
        if ((aggregateType & AggregateType.MEAN) != 0) {
            assert(row.getString(fieldIdx++).equals(HiveDefines.ValueTag.MEAN));
            assert(row.getString(fieldIdx++).equals(meanValue));
        }
        if ((aggregateType & AggregateType.MIN) != 0) {
            assert(row.getString(fieldIdx++).equals(HiveDefines.ValueTag.MIN));
            assert(row.getString(fieldIdx++).equals(minValue));
        }
        if ((aggregateType & AggregateType.MAX) != 0) {
            assert(row.getString(fieldIdx++).equals(HiveDefines.ValueTag.MAX));
            assert(row.getString(fieldIdx++).equals(maxValue));
        }

        for (int fi = fieldIdx; fi < 11; fi++) {
            assert(row.getString(fieldIdx++).equals("null"));
        }

        assert(row.getString(fieldIdx++).equals("any"));
        assert(row.getString(fieldIdx++).equals("any"));

        final String year = Integer.toString(time.year.getValue());
        final String month = Integer.toString(time.month.getValue());
        final String dayOfMonth = Integer.toString(time.dayOfMonth.getValue());

        assert(row.getString(fieldIdx++).equals(year));
        assert(row.getString(fieldIdx++).equals(month));
        assert(row.getString(fieldIdx++).equals(dayOfMonth));
        assert(row.getString(fieldIdx++).equals("any"));

        assert(row.getString(fieldIdx++).equals(appId));
        assert(row.getString(fieldIdx++).equals(year));
        assert(row.getString(fieldIdx++).equals(month));
        assert(row.getString(fieldIdx++).equals(dayOfMonth));
    }

    private Row createAggregateRow(final String appId, final Map<String,Object> values) {
        final ArrayList<StructField> fields = new ArrayList<>();
        final ArrayList<Object> fieldValues = new ArrayList<>();

        fields.add(DataTypes.createStructField(HiveProfileEnrichedEventTableSchema.COL_APP_ID.title(), DataTypes.StringType, false));
        fieldValues.add(appId);

        for (Map.Entry<String,Object> entry: values.entrySet()) {
            fields.add(DataTypes.createStructField(entry.getKey(), DataTypes.StringType, false));
            fieldValues.add(entry.getValue());
        }

        final StructType schema = DataTypes.createStructType(fields);

        return new GenericRowWithSchema(fieldValues.toArray(), schema);
    }
}
