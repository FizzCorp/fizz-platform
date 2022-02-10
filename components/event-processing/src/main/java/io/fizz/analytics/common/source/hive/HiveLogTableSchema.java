package io.fizz.analytics.common.source.hive;

import io.fizz.analytics.common.hive.ColumnDesc;
import io.fizz.analytics.common.hive.AbstractHiveTableSchema;
import org.apache.spark.sql.types.DataTypes;

public class HiveLogTableSchema extends AbstractHiveTableSchema {
    private static final String TABLE_NAME = "log";
    private static final RowFormat ROW_FORMAT = RowFormat.JSON;

    public static final String COL_TYPE = "e_type";
    public static final String COL_TRANSPORT_TYPE = "s_type";
    public static final String COL_API_PATH = "path";
    public static final String COL_RESPONSE_CODE = "status";
    public static final String COL_USER_ID = "user_id";
    public static final String COL_APP_ID = "aid";

    public static final String COL_YEAR = "year";
    public static final String COL_MONTH = "month";
    public static final String COL_DAY = "day";


    private static final ColumnDesc[] columns = new ColumnDesc[] {
        new ColumnDesc(COL_TYPE, DataTypes.StringType, false, false),
        new ColumnDesc(COL_TRANSPORT_TYPE, DataTypes.StringType, false, false),
        new ColumnDesc(COL_API_PATH, DataTypes.StringType, true, false),
        new ColumnDesc(COL_RESPONSE_CODE, DataTypes.IntegerType, true, false),
        new ColumnDesc(COL_USER_ID, DataTypes.StringType, true, false),
        new ColumnDesc(COL_APP_ID, DataTypes.StringType, false, false),

        new ColumnDesc(COL_YEAR, DataTypes.StringType, false, true),
        new ColumnDesc(COL_MONTH, DataTypes.StringType, false, true),
        new ColumnDesc(COL_DAY, DataTypes.StringType, false, true)
    };


    public HiveLogTableSchema() {
        super(TABLE_NAME, ROW_FORMAT, columns);

    }
}
