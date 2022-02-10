package io.fizz.analytics.common.source.hive;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;

public class HiveSentimentEventTableSchema extends HiveRawEventTableSchema {
    public static class RowBuilder extends HiveRawEventTableSchema.RowBuilder {

        @Override
        public Row get() {
            return new GenericRowWithSchema(new Object[] {
                    id, appId, countryCode, userId, sessionId, type,
                    version, occurredOn, platform, build,
                    custom01, custom02, custom03, fields,
                    year, month, day
            }, new HiveSentimentEventTableSchema().schema());
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

    public static final String TABLE_NAME = "sentiment_event";

    public HiveSentimentEventTableSchema() {
        super(TABLE_NAME);
    }
}
