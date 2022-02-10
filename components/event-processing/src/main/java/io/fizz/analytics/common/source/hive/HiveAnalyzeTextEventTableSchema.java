package io.fizz.analytics.common.source.hive;

import io.fizz.analytics.common.hive.ColumnDesc;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.DataTypes;

import java.util.Objects;

public class HiveAnalyzeTextEventTableSchema extends HiveRawEventTableSchema {
    public static class RowBuilder extends HiveRawEventTableSchema.RowBuilder {
        private String sentiment;
        @Override
        public Row get() {
            return new GenericRowWithSchema(new Object[] {
                    id, appId, countryCode, userId, sessionId, type,
                    version, occurredOn, platform, build,
                    custom01, custom02, custom03, fields, sentiment,
                    year, month, day
            }, new HiveAnalyzeTextEventTableSchema().schema());
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

        public RowBuilder setSentiment(String sentiment) {
            this.sentiment = sentiment;
            return this;
        }
    }

    public static final String TABLE_NAME = "analyze_text_event";
    public static final ColumnDesc COL_SENTIMENT = new ColumnDesc("sentiment", DataTypes.StringType, true, false);

    static private final ColumnDesc[] columns = new ColumnDesc[] {
            COL_ID, COL_APP_ID, COL_COUNTRY_CODE, COL_USER_ID, COL_SESSION_ID, COL_TYPE,
            COL_VERSION, COL_OCCURRED_ON, COL_PLATFORM, COL_BUILD,
            COL_CUSTOM_01, COL_CUSTOM_02, COL_CUSTOM_03, COL_FIELDS, COL_SENTIMENT,
            COL_YEAR, COL_MONTH, COL_DAY
    };

    public HiveAnalyzeTextEventTableSchema() {
        super(TABLE_NAME, columns);
    }

    public static String sentiment(final Row aRow) {
        if (Objects.isNull(aRow)) { throw new IllegalArgumentException("invalid row"); }
        return aRow.getString(aRow.fieldIndex(COL_SENTIMENT.title()));
    }
}
