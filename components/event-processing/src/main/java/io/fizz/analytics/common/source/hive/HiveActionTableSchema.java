package io.fizz.analytics.common.source.hive;

import io.fizz.analytics.common.hive.ColumnDesc;
import io.fizz.analytics.common.hive.AbstractHiveTableSchema;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataTypes;

import java.util.Objects;

public class HiveActionTableSchema extends AbstractHiveTableSchema {
    private static final String TABLE_NAME = "action";
    private static final RowFormat ROW_FORMAT = RowFormat.JSON;

    public static final String COL_TYPE = "e_type";
    public static final String COL_APP_ID = "aid";
    public static final String COL_SENDER_ID = "uid";
    public static final String COL_NICK = "nick";
    public static final String COL_ACTION_TYPE = "a_type";
    public static final String COL_ROOM_ID = "room_id";
    public static final String COL_REGION_ID = "region_id";
    public static final String COL_SHARD_ID = "shard_id";
    public static final String COL_TIMESTAMP = "timestamp";
    public static final String COL_EXTENSIONS = "extensions";
    public static final String COL_FIELDS = "fields";
    public static final String COL_YEAR = "year";
    public static final String COL_MONTH = "month";
    public static final String COL_DAY = "day";

    static private final ColumnDesc[] columns = new ColumnDesc[] {
        new ColumnDesc(COL_TYPE, DataTypes.StringType, false, false),
        new ColumnDesc(COL_APP_ID, DataTypes.StringType, false, false),
        new ColumnDesc(COL_SENDER_ID, DataTypes.StringType, true, false),
        new ColumnDesc(COL_NICK, DataTypes.StringType, true, false),
        new ColumnDesc(COL_ACTION_TYPE, DataTypes.StringType, true, false),
        new ColumnDesc(COL_ROOM_ID, DataTypes.StringType, true, false),
        new ColumnDesc(COL_REGION_ID, DataTypes.StringType, true, false),
        new ColumnDesc(COL_SHARD_ID, DataTypes.StringType, true, false),
        new ColumnDesc(COL_TIMESTAMP, DataTypes.StringType, true, false),
        new ColumnDesc(COL_EXTENSIONS, DataTypes.StringType, true, false),
        new ColumnDesc(COL_FIELDS, DataTypes.StringType, false, false),
        new ColumnDesc(COL_YEAR, DataTypes.StringType, false, true),
        new ColumnDesc(COL_MONTH, DataTypes.StringType, false, true),
        new ColumnDesc(COL_DAY, DataTypes.StringType, false, true)
    };

    public HiveActionTableSchema() {
        super (TABLE_NAME, ROW_FORMAT, columns);
    }

    public static String appId(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_APP_ID));
    }

    public static String senderId(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_SENDER_ID));
    }

    public static String nick(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_NICK));
    }

    public static String actionType(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_ACTION_TYPE));
    }

    public static String roomId(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_ROOM_ID));
    }

    public static String timestamp(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_TIMESTAMP));
    }

    public static String year(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_YEAR));
    }

    public static String month(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_MONTH));
    }

    public static String day(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_DAY));
    }
}
