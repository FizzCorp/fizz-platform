package io.fizz.analytics.common.source.hive;

import io.fizz.analytics.common.hive.ColumnDesc;
import io.fizz.analytics.common.hive.AbstractHiveTableSchema;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataTypes;

import java.util.Objects;

public class HiveTransTableSchema extends AbstractHiveTableSchema {
    private static final String TABLE_NAME = "trans";
    private static final RowFormat ROW_FORMAT = RowFormat.JSON;

    public static final String COL_TYPE = "e_type";
    public static final String COL_APP_ID = "aid";
    public static final String COL_DEST_LANG = "dst";
    public static final String COL_SRC_LANG = "src";
    public static final String COL_TEXT_LEN = "len";
    public static final String COL_YEAR = "year";
    public static final String COL_MONTH = "month";
    public static final String COL_DAY = "day";

    private static final ColumnDesc[] columns = new ColumnDesc[] {
        new ColumnDesc(COL_TYPE, DataTypes.StringType, true, false),
        new ColumnDesc(COL_APP_ID, DataTypes.StringType, true, false),
        new ColumnDesc(COL_DEST_LANG, DataTypes.StringType, true, false),
        new ColumnDesc(COL_SRC_LANG, DataTypes.StringType, true, false),
        new ColumnDesc(COL_TEXT_LEN, DataTypes.IntegerType, true, false),
        new ColumnDesc(COL_YEAR, DataTypes.StringType, true, true),
        new ColumnDesc(COL_MONTH, DataTypes.StringType, true, true),
        new ColumnDesc(COL_DAY, DataTypes.StringType, true, true)
    };

    public HiveTransTableSchema() {
        super(TABLE_NAME, ROW_FORMAT, columns);
    }

    public static String appId(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_APP_ID));
    }

    public static String sourceLang(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_SRC_LANG));
    }

    public static String destLang(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_DEST_LANG));
    }

    public static int length(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getInt(aRow.fieldIndex(COL_TEXT_LEN));
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
