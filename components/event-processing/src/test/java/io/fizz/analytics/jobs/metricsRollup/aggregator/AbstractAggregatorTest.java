package io.fizz.analytics.jobs.metricsRollup.aggregator;

import io.fizz.analytics.AbstractSparkTest;
import io.fizz.analytics.common.source.hive.HiveMetricTableSchema;
import org.apache.spark.sql.Row;

public abstract class AbstractAggregatorTest extends AbstractSparkTest {
    protected String getAppId(final Row row) {
        return row.getString(row.fieldIndex(HiveMetricTableSchema.COL_APP_ID.title()));
    }

    protected String getMetricId(final Row row) {
        return row.getString(row.fieldIndex(HiveMetricTableSchema.COL_TYPE.title()));
    }

    protected String getAttrKey(final Row aRow, int aAttrIdx) {
        switch (aAttrIdx) {
            case 1:
                return aRow.getString(aRow.fieldIndex(HiveMetricTableSchema.COL_ATTR_1.title()));
            case 2:
                return aRow.getString(aRow.fieldIndex(HiveMetricTableSchema.COL_ATTR_2.title()));
            case 3:
                return aRow.getString(aRow.fieldIndex(HiveMetricTableSchema.COL_ATTR_3.title()));
            case 4:
                return aRow.getString(aRow.fieldIndex(HiveMetricTableSchema.COL_ATTR_4.title()));
            case 5:
                return aRow.getString(aRow.fieldIndex(HiveMetricTableSchema.COL_ATTR_5.title()));
        }
        return null;
    }

    protected String getAttrValue(final Row aRow, int aAttrIdx) {
        switch (aAttrIdx) {
            case 1:
                return aRow.getString(aRow.fieldIndex(HiveMetricTableSchema.COL_VALUE_1.title()));
            case 2:
                return aRow.getString(aRow.fieldIndex(HiveMetricTableSchema.COL_VALUE_2.title()));
            case 3:
                return aRow.getString(aRow.fieldIndex(HiveMetricTableSchema.COL_VALUE_3.title()));
            case 4:
                return aRow.getString(aRow.fieldIndex(HiveMetricTableSchema.COL_VALUE_4.title()));
            case 5:
                return aRow.getString(aRow.fieldIndex(HiveMetricTableSchema.COL_VALUE_5.title()));
        }
        return null;
    }

    protected String getYear(final Row aRow) {
        return aRow.getString(aRow.fieldIndex(HiveMetricTableSchema.COL_YEAR.title()));
    }

    protected String getMonth(final Row aRow) {
        return aRow.getString(aRow.fieldIndex(HiveMetricTableSchema.COL_MONTH.title()));
    }

    protected String getDay(final Row aRow) {
        return aRow.getString(aRow.fieldIndex(HiveMetricTableSchema.COL_DAY.title()));
    }
}
