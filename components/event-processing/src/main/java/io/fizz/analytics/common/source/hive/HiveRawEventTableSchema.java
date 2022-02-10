package io.fizz.analytics.common.source.hive;

import io.fizz.analytics.common.hive.AbstractHiveTableSchema;
import io.fizz.analytics.common.hive.ColumnDesc;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.DataTypes;

import java.util.Objects;

public class HiveRawEventTableSchema extends AbstractHiveTableSchema {
    public static class RowBuilder {
        protected String id;
        protected String appId;
        protected String countryCode;
        protected String userId;
        protected String sessionId;
        protected int type;
        protected int version;
        protected long occurredOn;
        protected String platform;
        protected String build;
        protected String custom01;
        protected String custom02;
        protected String custom03;
        protected String fields;
        protected String year;
        protected String month;
        protected String day;

        public Row get() {
            return new GenericRowWithSchema(new Object[] {
                    id, appId, countryCode, userId, sessionId, type,
                    version, occurredOn, platform, build,
                    custom01, custom02, custom03, fields,
                    year, month, day
            }, new HiveRawEventTableSchema().schema());
        }

        public RowBuilder setId(String id) {
            this.id = id;
            return this;
        }

        public RowBuilder setAppId(String appId) {
            this.appId = appId;
            return this;
        }

        public RowBuilder setCountryCode(String countryCode) {
            this.countryCode = countryCode;
            return this;
        }

        public RowBuilder setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public RowBuilder setSessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public RowBuilder setType(int type) {
            this.type = type;
            return this;
        }

        public RowBuilder setVersion(int version) {
            this.version = version;
            return this;
        }

        public RowBuilder setOccurredOn(long occurredOn) {
            this.occurredOn = occurredOn;
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

        public RowBuilder setFields(String fields) {
            this.fields = fields;
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

    public static final String TABLE_NAME = "raw_event";
    private static final RowFormat ROW_FORMAT = RowFormat.JSON;

    public static final ColumnDesc COL_ID = new ColumnDesc("id", DataTypes.StringType, false, false);
    public static final ColumnDesc COL_APP_ID = new ColumnDesc("appId", DataTypes.StringType, false, false);
    public static final ColumnDesc COL_COUNTRY_CODE = new ColumnDesc("countryCode", DataTypes.StringType, true, false);
    public static final ColumnDesc COL_USER_ID = new ColumnDesc("actorId", DataTypes.StringType, false, false);
    public static final ColumnDesc COL_SESSION_ID = new ColumnDesc("sessionId", DataTypes.StringType, false, false);
    public static final ColumnDesc COL_TYPE = new ColumnDesc("type", DataTypes.IntegerType, false, false);
    public static final ColumnDesc COL_VERSION = new ColumnDesc("ver", DataTypes.IntegerType, false, false);
    public static final ColumnDesc COL_OCCURRED_ON = new ColumnDesc("ts", DataTypes.LongType, false, false);
    public static final ColumnDesc COL_PLATFORM = new ColumnDesc("platform", DataTypes.StringType, true, false);
    public static final ColumnDesc COL_BUILD = new ColumnDesc("build", DataTypes.StringType, true, false);
    public static final ColumnDesc COL_CUSTOM_01 = new ColumnDesc("custom01", DataTypes.StringType, true, false);
    public static final ColumnDesc COL_CUSTOM_02 = new ColumnDesc("custom02", DataTypes.StringType, true, false);
    public static final ColumnDesc COL_CUSTOM_03 = new ColumnDesc("custom03", DataTypes.StringType, true, false);
    public static final ColumnDesc COL_FIELDS = new ColumnDesc("fields", DataTypes.StringType, true, false);
    public static final ColumnDesc COL_YEAR = new ColumnDesc("year", DataTypes.StringType, false, true);
    public static final ColumnDesc COL_MONTH = new ColumnDesc("month", DataTypes.StringType, false, true);
    public static final ColumnDesc COL_DAY = new ColumnDesc("day", DataTypes.StringType, false, true);

    static private final ColumnDesc[] columns = new ColumnDesc[] {
            COL_ID, COL_APP_ID, COL_COUNTRY_CODE, COL_USER_ID, COL_SESSION_ID, COL_TYPE,
            COL_VERSION, COL_OCCURRED_ON, COL_PLATFORM, COL_BUILD,
            COL_CUSTOM_01, COL_CUSTOM_02, COL_CUSTOM_03, COL_FIELDS,
            COL_YEAR, COL_MONTH, COL_DAY
    };

    public HiveRawEventTableSchema() {
        super(TABLE_NAME, ROW_FORMAT, columns);
    }

    protected HiveRawEventTableSchema(String aTableName) {
        super(aTableName, ROW_FORMAT, columns);
    }

    protected HiveRawEventTableSchema(String aTableName, final ColumnDesc[] aColumns) {
        super(aTableName, ROW_FORMAT, aColumns);
    }

    public static String id(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_ID.title()));
    }

    public static String appId(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_APP_ID.title()));
    }

    public static String countryCode(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_COUNTRY_CODE.title()));
    }

    public static String userId(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_USER_ID.title()));
    }

    public static String sessionId(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_SESSION_ID.title()));
    }

    public static int eventType(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getInt(aRow.fieldIndex(COL_TYPE.title()));
    }

    public static int version(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getInt(aRow.fieldIndex(COL_VERSION.title()));
    }

    public static long occurredOn(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getLong(aRow.fieldIndex(COL_OCCURRED_ON.title()));
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

    public static String fields(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_FIELDS.title()));
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

    public static String value(final Row aRow, String field) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        Object val = aRow.get(aRow.fieldIndex(field));
        if (val instanceof String) {
            return (String) val;
        }
        return String.valueOf(val);
    }
}
