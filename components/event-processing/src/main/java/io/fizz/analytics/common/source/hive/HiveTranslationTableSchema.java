package io.fizz.analytics.common.source.hive;

import io.fizz.analytics.common.hive.AbstractHiveTableSchema;
import io.fizz.analytics.common.hive.ColumnDesc;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.DataTypes;

import java.util.Objects;

public class HiveTranslationTableSchema extends AbstractHiveTableSchema {
    public static class RowBuilder {
        private String id;
        private String appId;
        private String platform;
        private String build;
        private String custom01;
        private String custom02;
        private String custom03;
        private String age;
        private String spend;
        private int len;
        private String src;
        private String dest;
        private String year;
        private String month;
        private String day;

        public Row get() {
            return new GenericRowWithSchema(new Object[] {
                    id, appId, platform, build,
                    custom01, custom02, custom03,
                    age, spend, len, src, dest,
                    year, month, day
            }, new HiveTranslationTableSchema().schema());
        }

        public RowBuilder setId(String id) {
            this.id = id;
            return this;
        }

        public RowBuilder setAppId(String appId) {
            this.appId = appId;
            return this;
        }

        public RowBuilder setPlatform(String platform) {
            this.platform = platform;
            return this;
        }

        public RowBuilder setBuild(String build) {
            this.build = build;
            return this;
        }

        public RowBuilder setCustom01(String custom01) {
            this.custom01 = custom01;
            return this;
        }

        public RowBuilder setCustom02(String custom02) {
            this.custom02 = custom02;
            return this;
        }

        public RowBuilder setCustom03(String custom03) {
            this.custom03 = custom03;
            return this;
        }

        public RowBuilder setAge(String age) {
            this.age = age;
            return this;
        }

        public RowBuilder setSpend(String spend) {
            this.spend = spend;
            return this;
        }

        public RowBuilder setLength(int len) {
            this.len = len;
            return this;
        }

        public RowBuilder setSrc(String src) {
            this.src = src;
            return this;
        }

        public RowBuilder setDest(String dest) {
            this.dest = dest;
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

    public static final String TABLE_NAME = "translation";
    private static final RowFormat ROW_FORMAT = RowFormat.JSON;

    public static final ColumnDesc COL_ID = new ColumnDesc("id", DataTypes.StringType, false, false);
    public static final ColumnDesc COL_APP_ID = new ColumnDesc("appId", DataTypes.StringType, false, false);
    public static final ColumnDesc COL_PLATFORM = new ColumnDesc("platform", DataTypes.StringType, true, false);
    public static final ColumnDesc COL_BUILD = new ColumnDesc("build", DataTypes.StringType, true, false);
    public static final ColumnDesc COL_CUSTOM_01 = new ColumnDesc("custom01", DataTypes.StringType, true, false);
    public static final ColumnDesc COL_CUSTOM_02 = new ColumnDesc("custom02", DataTypes.StringType, true, false);
    public static final ColumnDesc COL_CUSTOM_03 = new ColumnDesc("custom03", DataTypes.StringType, true, false);
    public static final ColumnDesc COL_AGE = new ColumnDesc("age", DataTypes.StringType, true, false);
    public static final ColumnDesc COL_SPEND = new ColumnDesc("spend", DataTypes.StringType, true, false);
    public static final ColumnDesc COL_LENGTH = new ColumnDesc("len", DataTypes.IntegerType, false, false);
    public static final ColumnDesc COL_SRC = new ColumnDesc("src", DataTypes.StringType, false, false);
    public static final ColumnDesc COL_DEST = new ColumnDesc("dest", DataTypes.StringType, false, false);
    public static final ColumnDesc COL_YEAR = new ColumnDesc("year", DataTypes.StringType, false, true);
    public static final ColumnDesc COL_MONTH = new ColumnDesc("month", DataTypes.StringType, false, true);
    public static final ColumnDesc COL_DAY = new ColumnDesc("day", DataTypes.StringType, false, true);

    static private final ColumnDesc[] columns = new ColumnDesc[] {
            COL_ID, COL_APP_ID, COL_PLATFORM, COL_BUILD,
            COL_CUSTOM_01, COL_CUSTOM_02, COL_CUSTOM_03,
            COL_AGE, COL_SPEND, COL_LENGTH, COL_SRC, COL_DEST,
            COL_YEAR, COL_MONTH, COL_DAY
    };

    public HiveTranslationTableSchema() {
        super(TABLE_NAME, ROW_FORMAT, columns);
    }

    public static String id(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_ID.title()));
    }

    public static String appId(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_APP_ID.title()));
    }

    public static String platform(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_PLATFORM.title()));
    }

    public static String build(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_BUILD.title()));
    }

    public static String custom01(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_CUSTOM_01.title()));
    }

    public static String custom02(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_CUSTOM_02.title()));
    }

    public static String custom03(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_CUSTOM_03.title()));
    }

    public static String age(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_AGE.title()));
    }

    public static String spend(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_SPEND.title()));
    }

    public static String length(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_LENGTH.title()));
    }

    public static String src(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_SRC.title()));
    }

    public static String dest(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_DEST.title()));
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
