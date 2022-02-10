package io.fizz.analytics.common.adapter;

import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.Profile;
import io.fizz.analytics.common.Segmentation;
import io.fizz.analytics.common.Utils;
import io.fizz.analytics.common.source.hive.HiveEventFields;
import io.fizz.analytics.common.source.hive.HiveProfileEnrichedEventTableSchema;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.analytics.domain.User;
import io.fizz.common.domain.EventType;
import io.fizz.common.domain.events.AbstractDomainEvent;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.json.JSONObject;

import java.util.Date;
import java.util.UUID;

public class UserRowAdapter {
    private static final String ID = "id";
    private static final String APP_ID = "appId";
    private static final String FIRST_TIME_ACTIVE_TS = "firstTimeActiveTS";
    private static final String LAST_TIME_ACTIVE_TS = "lastTimeActiveTS";
    private static final String AMOUNT_SPENT_IN_CENTS = "amountSpentInCents";
    private static final String LOCATION = "location";
    private static final String LIFE_CYCLE_SEGMENT = "lifeCycleSegment";
    private static final String SPENDER_SEGMENT = "spenderSegment";
    private static final String BUILD = "build";
    private static final String PLATFORM = "platform";
    private static final String CUSTOM01 = "custom01";
    private static final String CUSTOM02 = "custom02";
    private static final String CUSTOM03 = "custom03";
    private static final String SENTIMENT_SUM = "sentimentSum";
    private static final String MESSAGES_COUNTS = "messagesCounts";

    public static final StructType schema = DataTypes.createStructType(
            new StructField[] {
                    DataTypes.createStructField(ID, DataTypes.StringType, false),
                    DataTypes.createStructField(APP_ID, DataTypes.StringType, false),
                    DataTypes.createStructField(FIRST_TIME_ACTIVE_TS, DataTypes.LongType, false),
                    DataTypes.createStructField(LAST_TIME_ACTIVE_TS, DataTypes.LongType, false),
                    DataTypes.createStructField(AMOUNT_SPENT_IN_CENTS, DataTypes.LongType, false),
                    DataTypes.createStructField(LIFE_CYCLE_SEGMENT, DataTypes.StringType, false),
                    DataTypes.createStructField(SPENDER_SEGMENT, DataTypes.StringType, false),
                    DataTypes.createStructField(LOCATION, DataTypes.StringType, false),
                    DataTypes.createStructField(BUILD, DataTypes.StringType, false),
                    DataTypes.createStructField(PLATFORM, DataTypes.StringType, false),
                    DataTypes.createStructField(CUSTOM01, DataTypes.StringType, true),
                    DataTypes.createStructField(CUSTOM02, DataTypes.StringType, true),
                    DataTypes.createStructField(CUSTOM03, DataTypes.StringType, true),
                    DataTypes.createStructField(SENTIMENT_SUM, DataTypes.DoubleType, false),
                    DataTypes.createStructField(MESSAGES_COUNTS, DataTypes.LongType, false),
            }
    );

    public static Row toGenericRow(User aUser) {
        String lifecycleSegment = Segmentation.lifecycle.fetchSegment(Profile.durationInDays(aUser.lastTimeActiveTS(), aUser.firstTimeActiveTS()));
        String spenderSegment = Segmentation.spender.fetchSegment(aUser.amountSpentInCents());

        return new GenericRowWithSchema(new Object[] {
                aUser.id().value(), aUser.appId().value(), aUser.firstTimeActiveTS(), aUser.lastTimeActiveTS(),
                aUser.amountSpentInCents(), lifecycleSegment, spenderSegment,
                aUser.location(), aUser.build(), aUser.platform(),
                aUser.custom01(), aUser.custom02(), aUser.custom03(),
                aUser.sentimentSum(), aUser.messagesCounts()
        }, schema);
    }

    public static Row toEventRow(final User aUser, final HiveTime time) {

        JSONObject fields = new JSONObject();
        fields.put(HiveEventFields.PROFILE_OLDEST_ACTIVITY_TS.value(), aUser.firstTimeActiveTS());
        fields.put(HiveEventFields.PROFILE_LATEST_ACTIVITY_TS.value(), aUser.lastTimeActiveTS());
        fields.put(HiveEventFields.PROFILE_SPENT.value(), aUser.amountSpentInCents());
        fields.put(HiveEventFields.PROFILE_SENTIMENT_SUM.value(), aUser.sentimentSum());
        fields.put(HiveEventFields.PROFILE_MESSAGE_COUNT.value(), aUser.messagesCounts());

        String lifecycleSegment = Segmentation.lifecycle.fetchSegment(Profile.durationInDays(aUser.lastTimeActiveTS(), aUser.firstTimeActiveTS()));
        String spenderSegment = Segmentation.spender.fetchSegment(aUser.amountSpentInCents());

        return new HiveProfileEnrichedEventTableSchema.RowBuilder()
                .setId(UUID.randomUUID().toString())
                .setAppId(aUser.appId().value())
                .setUserId(aUser.id().value())
                .setCountryCode(aUser.location())
                .setSessionId(UUID.randomUUID().toString())
                .setType(EventType.PROFILE_UPDATED.value())
                .setVersion(AbstractDomainEvent.VERSION)
                .setOccurredOn(aUser.lastTimeActiveTS())
                .setPlatform(aUser.platform())
                .setBuild(aUser.build())
                .setCustom01(aUser.custom01())
                .setCustom02(aUser.custom02())
                .setCustom03(aUser.custom03())
                .setFields(fields.toString())
                .setAge(lifecycleSegment)
                .setSpend(spenderSegment)
                .setYear(Integer.toString(time.year.getValue()))
                .setMonth(time.yyyymmm())
                .setDay(time.yyyymmmdd())
                .get();
    }

    public static User toUser(Row aRow) throws DomainErrorException {

        return new User.Builder()
                .setId(aRow.getString(aRow.fieldIndex(ID)))
                .setAppId(aRow.getString(aRow.fieldIndex(APP_ID)))
                .setFirstTimeActiveTS(aRow.getLong(aRow.fieldIndex(FIRST_TIME_ACTIVE_TS)))
                .setLastTimeActiveTS(aRow.getLong(aRow.fieldIndex(LAST_TIME_ACTIVE_TS)))
                .setAmountSpentInCents(aRow.getLong(aRow.fieldIndex(AMOUNT_SPENT_IN_CENTS)))
                .setLocation(aRow.getString(aRow.fieldIndex(LOCATION)))
                .setBuild(aRow.getString(aRow.fieldIndex(BUILD)))
                .setPlatform(aRow.getString(aRow.fieldIndex(PLATFORM)))
                .setCustom01(aRow.getString(aRow.fieldIndex(CUSTOM01)))
                .setCustom02(aRow.getString(aRow.fieldIndex(CUSTOM02)))
                .setCustom03(aRow.getString(aRow.fieldIndex(CUSTOM03)))
                .setSentimentSum(aRow.getDouble(aRow.fieldIndex(SENTIMENT_SUM)), aRow.getLong(aRow.fieldIndex(MESSAGES_COUNTS)))
                .get();
    }
}
