package io.fizz.analytics.common.source.hive;

import io.fizz.analytics.common.hive.ColumnDesc;
import io.fizz.analytics.common.hive.AbstractHiveTableSchema;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.DataTypes;

import java.util.*;

public class HiveMetricTableSchema extends AbstractHiveTableSchema {
    public static class RowBuilder {
        private static final int LENGTH_CUSTOM_ATTR = 10;

        private String type;
        private List<String> customAttr = new ArrayList<>();
        private String dim;
        private String dimValue;
        private String dtYear;
        private String dtMonth;
        private String dtDay;
        private String dtHour;
        private String appId;
        private String year;
        private String month;
        private String day;

        public Row get() {
            populateCustomAttr();
            return new GenericRowWithSchema(new Object[]{
                    type,
                    customAttr.get(0), customAttr.get(1),
                    customAttr.get(2), customAttr.get(3),
                    customAttr.get(4), customAttr.get(5),
                    customAttr.get(6), customAttr.get(7),
                    customAttr.get(8), customAttr.get(9),
                    dim, dimValue, dtYear, dtMonth,
                    dtDay, dtHour,
                    appId, year, month, day
            }, new HiveMetricTableSchema().schema());
        }

        private void populateCustomAttr() {
            for (int i = customAttr.size(); i < LENGTH_CUSTOM_ATTR; i += 2) {
                customAttr.add("null");
                customAttr.add("null");
            }
        }

        public String getType() {
            return type;
        }

        public RowBuilder setType(String type) {
            this.type = type;
            return this;
        }

        public RowBuilder addCustomAttr(String key, String value) {
            int currentLength = this.customAttr.size();
            if (currentLength < LENGTH_CUSTOM_ATTR) {
                this.customAttr.add(key);
                this.customAttr.add(value);
            }
            return this;
        }

        public RowBuilder setDim(String dim) {
            this.dim = dim;
            return this;
        }

        public RowBuilder setDimValue(String dimValue) {
            this.dimValue = dimValue;
            return this;
        }

        public RowBuilder setDtYear(String dtYear) {
            this.dtYear = dtYear;
            return this;
        }

        public RowBuilder setDtMonth(String dtMonth) {
            this.dtMonth = dtMonth;
            return this;
        }

        public RowBuilder setDtDay(String dtDay) {
            this.dtDay = dtDay;
            return this;
        }

        public RowBuilder setDtHour(String dtHour) {
            this.dtHour = dtHour;
            return this;
        }

        public RowBuilder setAppId(String appId) {
            this.appId = appId;
            return this;
        }

        public RowBuilder setYear(String year) {
            this.year = year;
            return this;
        }

        public RowBuilder setMonth(String month) {
            this.month = month;
            return this;
        }

        public RowBuilder setDay(String day) {
            this.day = day;
            return this;
        }
    }

    public static final String TABLE_NAME = "metrics";
    private static final RowFormat ROW_FORMAT = RowFormat.CSV;

    public static final ColumnDesc COL_TYPE = new ColumnDesc("type", DataTypes.StringType, false, false);
    public static final ColumnDesc COL_ATTR_1 = new ColumnDesc("attr1", DataTypes.StringType, true, false);
    public static final ColumnDesc COL_VALUE_1 = new ColumnDesc("value1", DataTypes.StringType, true, false);
    public static final ColumnDesc COL_ATTR_2 = new ColumnDesc("attr2", DataTypes.StringType, true, false);
    public static final ColumnDesc COL_VALUE_2 = new ColumnDesc("value2", DataTypes.StringType, true, false);
    public static final ColumnDesc COL_ATTR_3 = new ColumnDesc("attr3", DataTypes.StringType, true, false);
    public static final ColumnDesc COL_VALUE_3 = new ColumnDesc("value3", DataTypes.StringType, true, false);
    public static final ColumnDesc COL_ATTR_4 = new ColumnDesc("attr4", DataTypes.StringType, true, false);
    public static final ColumnDesc COL_VALUE_4 = new ColumnDesc("value4", DataTypes.StringType, true, false);
    public static final ColumnDesc COL_ATTR_5 = new ColumnDesc("attr5", DataTypes.StringType, true, false);
    public static final ColumnDesc COL_VALUE_5 = new ColumnDesc("value5", DataTypes.StringType, true, false);
    public static final ColumnDesc COL_DIM = new ColumnDesc("dim", DataTypes.StringType, true, false);
    public static final ColumnDesc COL_DIM_VALUE = new ColumnDesc("dim_value", DataTypes.StringType, true, false);
    public static final ColumnDesc COL_DT_YEAR = new ColumnDesc("dt_year", DataTypes.StringType, false, false);
    public static final ColumnDesc COL_DT_MONTH = new ColumnDesc("dt_month", DataTypes.StringType, false, false);
    public static final ColumnDesc COL_DT_DAY = new ColumnDesc("dt_day", DataTypes.StringType, false, false);
    public static final ColumnDesc COL_DT_HOUR = new ColumnDesc("dt_hour", DataTypes.StringType, false, false);
    public static final ColumnDesc COL_APP_ID = new ColumnDesc("appid", DataTypes.StringType, false, true);
    public static final ColumnDesc COL_YEAR = new ColumnDesc("year", DataTypes.StringType, false, true);
    public static final ColumnDesc COL_MONTH = new ColumnDesc("month", DataTypes.StringType, false, true);
    public static final ColumnDesc COL_DAY = new ColumnDesc("day", DataTypes.StringType, false, true);

    private static final ColumnDesc[] columns = new ColumnDesc[]{
            COL_TYPE,  COL_ATTR_1,  COL_VALUE_1,  COL_ATTR_2,  COL_VALUE_2, COL_ATTR_3, COL_VALUE_3,
            COL_ATTR_4,  COL_VALUE_4,  COL_ATTR_5, COL_VALUE_5, COL_DIM, COL_DIM_VALUE,
            COL_DT_YEAR, COL_DT_MONTH, COL_DT_DAY, COL_DT_HOUR, COL_APP_ID,
            COL_YEAR, COL_MONTH, COL_DAY
    };

    public HiveMetricTableSchema() {
        super(TABLE_NAME, ROW_FORMAT, columns);
    }

    protected HiveMetricTableSchema(String aTableName) {
        super(aTableName, ROW_FORMAT, columns);
    }

    public static String type(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_TYPE.title()));
    }

    public static String attr1(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_ATTR_1.title()));
    }

    public static String value1(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_VALUE_1.title()));
    }

    public static String attr2(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_ATTR_2.title()));
    }

    public static String value2(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_VALUE_2.title()));
    }

    public static String attr3(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_ATTR_3.title()));
    }

    public static String value3(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_VALUE_3.title()));
    }

    public static String attr4(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_ATTR_4.title()));
    }

    public static String value4(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_VALUE_4.title()));
    }

    public static String attr5(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_ATTR_5.title()));
    }

    public static String value5(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_VALUE_5.title()));
    }

    public static String dim(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_DIM.title()));
    }

    public static String dimValue(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_DIM_VALUE.title()));
    }

    public static String dtYear(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_DT_YEAR.title()));
    }

    public static String dtMonth(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_DT_MONTH.title()));
    }

    public static String dtDay(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_DT_DAY.title()));
    }

    public static String dtHour(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_DT_HOUR.title()));
    }

    public static String appId(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_APP_ID.title()));
    }

    public static String year(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_YEAR.title()));
    }

    public static String month(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_MONTH.title()));
    }

    public static String day(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_DAY.title()));
    }
}