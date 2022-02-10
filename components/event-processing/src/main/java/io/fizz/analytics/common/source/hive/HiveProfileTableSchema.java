package io.fizz.analytics.common.source.hive;

import io.fizz.analytics.common.hive.AbstractHiveTableSchema;
import io.fizz.analytics.common.hive.ColumnDesc;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.DataTypes;

import java.util.Objects;

public class HiveProfileTableSchema extends AbstractHiveTableSchema {
    public static class RowBuilder {
        private long oldestActivityTS;
        private long latestActivityTS;
        private long amountInCents;
        private String location;
        private long build;
        private String platform;
        private double avgSentiment;
        private long messagesCounts;
        private String year;
        private String month;
        private String day;

        public Row get() {
            return new GenericRowWithSchema(new Object[] {
                    oldestActivityTS, latestActivityTS, amountInCents,
                    location, build, platform, avgSentiment, messagesCounts,
                    year, month, day
            }, new HiveRawEventTableSchema().schema());
        }

        public void setOldestActivityTS(long oldestActivityTS) {
            this.oldestActivityTS = oldestActivityTS;
        }

        public void setLatestActivityTS(long latestActivityTS) {
            this.latestActivityTS = latestActivityTS;
        }

        public void setAmountInCents(long amountInCents) {
            this.amountInCents = amountInCents;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public void setBuild(int build) {
            this.build = build;
        }

        public void setPlatform(String platform) {
            this.platform = platform;
        }

        public void setAvgSentiment(float avgSentiment) {
            this.avgSentiment = avgSentiment;
        }
    }

    public static final String TABLE_NAME = "profile";
    private static final RowFormat ROW_FORMAT = RowFormat.JSON;

    private static final ColumnDesc COL_ID = new ColumnDesc("userId", DataTypes.StringType, false, false);
    private static final ColumnDesc COL_OLDEST_ACTIVITY_TS = new ColumnDesc("oActivityTS", DataTypes.StringType, false, false);
    private static final ColumnDesc COL_LATEST_ACTIVITY_TS = new ColumnDesc("lActivityTS", DataTypes.StringType, false, false);
    private static final ColumnDesc COL_AMOUNT_IN_CENT = new ColumnDesc("amount", DataTypes.LongType, false, false);
    private static final ColumnDesc COL_LOCATION = new ColumnDesc("loc", DataTypes.StringType, false, false);
    private static final ColumnDesc COL_BUILD = new ColumnDesc("build", DataTypes.LongType, false, false);
    private static final ColumnDesc COL_PLATFORM = new ColumnDesc("platform", DataTypes.StringType, false, false);
    private static final ColumnDesc COL_AVERAGE_SENTIMENT = new ColumnDesc("avgSent", DataTypes.DoubleType, false, false);
    private static final ColumnDesc COL_MESSAGES_COUNT = new ColumnDesc("msgCount", DataTypes.LongType, false, false);
    public static final ColumnDesc COL_YEAR = new ColumnDesc("year", DataTypes.StringType, false, true);
    public static final ColumnDesc COL_MONTH = new ColumnDesc("month", DataTypes.StringType, false, true);
    public static final ColumnDesc COL_DAY = new ColumnDesc("day", DataTypes.StringType, false, true);

    static private final ColumnDesc[] columns = new ColumnDesc[] {
            COL_ID, COL_OLDEST_ACTIVITY_TS, COL_LATEST_ACTIVITY_TS,
            COL_AMOUNT_IN_CENT, COL_LOCATION, COL_BUILD,
            COL_PLATFORM, COL_AVERAGE_SENTIMENT, COL_MESSAGES_COUNT,
            COL_YEAR, COL_MONTH, COL_DAY
    };

    public HiveProfileTableSchema() {
        super(TABLE_NAME, ROW_FORMAT, columns);
    }

    public static String id(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_OLDEST_ACTIVITY_TS.title()));
    }

    public static String oldestActivityTS(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_LATEST_ACTIVITY_TS.title()));
    }

    public static String latestActivityTS(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_LATEST_ACTIVITY_TS.title()));
    }

    public static String amountInCent(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_AMOUNT_IN_CENT.title()));
    }

    public static String location(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_LOCATION.title()));
    }

    public static String build(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_BUILD.title()));
    }

    public static String platform(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_PLATFORM.title()));
    }

    public static String averageSentiment(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_AVERAGE_SENTIMENT.title()));
    }

    public static String messagesCount(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_MESSAGES_COUNT.title()));
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
