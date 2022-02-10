package io.fizz.analytics.common.source.hive;

import io.fizz.analytics.common.hive.ColumnDesc;
import io.fizz.analytics.common.hive.AbstractHiveTableSchema;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataTypes;

import java.util.Objects;

public class HiveKeywordsTableSchema extends AbstractHiveTableSchema {
    public static final String TABLE_NAME = "keywords";
    private static final RowFormat ROW_FORMAT = RowFormat.JSON;

    public static final String COL_KEYWORD = "keyword";
    public static final String COL_SENTIMENT_SCORE = "sentiment_score";
    public static final String COL_ANGER = "anger";
    public static final String COL_DISGUST = "disgust";
    public static final String COL_FEAR = "fear";
    public static final String COL_JOY = "joy";
    public static final String COL_SADNESS = "sadness";
    public static final String COL_APP_ID = "aid";
    public static final String COL_YEAR = "year";
    public static final String COL_MONTH = "month";
    public static final String COL_DAY = "day";

    private static ColumnDesc[] columns = new ColumnDesc[] {
            new ColumnDesc(COL_KEYWORD, DataTypes.StringType, false,false),
            new ColumnDesc(COL_SENTIMENT_SCORE, DataTypes.DoubleType, false,false),
            new ColumnDesc(COL_ANGER, DataTypes.DoubleType, false,false),
            new ColumnDesc(COL_DISGUST, DataTypes.DoubleType, false,false),
            new ColumnDesc(COL_FEAR, DataTypes.DoubleType, false, false),
            new ColumnDesc(COL_JOY, DataTypes.DoubleType, false, false),
            new ColumnDesc(COL_SADNESS, DataTypes.DoubleType, false,false),
            new ColumnDesc(COL_APP_ID, DataTypes.StringType, false,false),
            new ColumnDesc(COL_YEAR, DataTypes.StringType, false,true),
            new ColumnDesc(COL_MONTH, DataTypes.StringType, false,true),
            new ColumnDesc(COL_DAY, DataTypes.StringType, false,true)
    };

    public HiveKeywordsTableSchema() {
        super (TABLE_NAME, ROW_FORMAT, columns);
    }

    public static String keyword(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_KEYWORD));
    }

    public static double sentimentScore(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getDouble(aRow.fieldIndex(COL_SENTIMENT_SCORE));
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
