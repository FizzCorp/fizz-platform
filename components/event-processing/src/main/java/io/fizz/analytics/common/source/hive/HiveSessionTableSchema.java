package io.fizz.analytics.common.source.hive;

import io.fizz.analytics.common.hive.ColumnDesc;
import io.fizz.analytics.common.hive.AbstractHiveTableSchema;
import org.apache.spark.sql.types.DataTypes;

public class HiveSessionTableSchema extends AbstractHiveTableSchema {
    private static final String TABLE_NAME = "session";
    private static final RowFormat ROW_FORMAT = RowFormat.JSON;

    public static final String COL_TYPE = "e_type";
    public static final String COL_APP_ID = "aid";
    public static final String COL_USER_APP_ID = "uid";
    public static final String COL_USER_FIZZ_ID = "fid";
    public static final String COL_SESSION_TYPE = "s_type";
    public static final String COL_SESSION_ID = "sid";
    public static final String COL_START_TIME = "start_ts";
    public static final String COL_END_TIME = "end_ts";
    public static final String COL_COUNTRY = "country";
    public static final String COL_CONTINENT = "continent";
    public static final String COL_DURATION = "duration";
    public static final String COL_YEAR = "year";
    public static final String COL_MONTH = "month";
    public static final String COL_DAY = "day";

    private static final ColumnDesc[] columns = new ColumnDesc[] {
        new ColumnDesc(COL_TYPE, DataTypes.StringType, false, false),
        new ColumnDesc(COL_APP_ID, DataTypes.StringType, false, false),
        new ColumnDesc(COL_USER_APP_ID, DataTypes.StringType, false, false),
        new ColumnDesc(COL_USER_FIZZ_ID, DataTypes.StringType, true, false),
        new ColumnDesc(COL_SESSION_TYPE, DataTypes.StringType, true, false),
        new ColumnDesc(COL_SESSION_ID, DataTypes.StringType, true, false),
        new ColumnDesc(COL_START_TIME, DataTypes.StringType, true, false),
        new ColumnDesc(COL_END_TIME, DataTypes.StringType, true, false),
        new ColumnDesc(COL_COUNTRY, DataTypes.StringType, true, false),
        new ColumnDesc(COL_CONTINENT, DataTypes.StringType, true, false),
        new ColumnDesc(COL_DURATION, DataTypes.IntegerType, true, false),
        new ColumnDesc(COL_YEAR, DataTypes.StringType, true, true),
        new ColumnDesc(COL_MONTH, DataTypes.StringType, true, true),
        new ColumnDesc(COL_DAY, DataTypes.StringType, true, true)
    };

    public HiveSessionTableSchema() {
        super(TABLE_NAME, ROW_FORMAT, columns);
    }
}
