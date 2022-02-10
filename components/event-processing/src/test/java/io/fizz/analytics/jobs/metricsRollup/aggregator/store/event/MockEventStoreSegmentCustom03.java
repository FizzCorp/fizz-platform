package io.fizz.analytics.jobs.metricsRollup.aggregator.store.event;

import io.fizz.analytics.common.source.hive.HiveDefines;
import io.fizz.analytics.common.source.hive.HiveProfileEnrichedEventTableSchema;
import io.fizz.common.domain.EventType;
import io.fizz.common.domain.events.AbstractDomainEvent;
import io.fizz.analytics.jobs.AbstractMockTableDataSource;
import io.fizz.analytics.jobs.metricsRollup.common.MetricValidator;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockEventStoreSegmentCustom03 extends AbstractMockTableDataSource {
    public MockEventStoreSegmentCustom03(SparkSession aSpark) {
        super(aSpark);
    }

    private static final String VALUE_APP_A = "appA";
    private static final String VALUE_AGE_A = "days_1_3";
    private static final String VALUE_AGE_B = "days_31_1";
    private static final String VALUE_CUSTOM03_A = "testE";
    private static final String VALUE_CUSTOM03_B = "testF";
    private static final String VALUE_USER_A = "userA";
    private static final String VALUE_USER_B = "userB";
    private static final String VALUE_USER_C = "userC";
    private static final long VALUE_MESSAGE_TS = 1525794897000L;

    @Override
    protected List<Row> testDataFactory() {
        return new ArrayList<Row>() {
            {
                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("1").setAppId(VALUE_APP_A).setUserId(VALUE_USER_A)
                        .setSessionId("session_1").setType(EventType.SESSION_STARTED.value()).setVersion(AbstractDomainEvent.VERSION)
                        .setOccurredOn(VALUE_MESSAGE_TS).setPlatform("android").setBuild("build_2").setCustom01("D")
                        .setCustom02("E").setCustom03(VALUE_CUSTOM03_A).setFields("{}").setAge(VALUE_AGE_A)
                        .setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("2").setAppId(VALUE_APP_A).setUserId(VALUE_USER_A)
                        .setSessionId("session_1").setType(EventType.SESSION_ENDED.value()).setVersion(AbstractDomainEvent.VERSION)
                        .setOccurredOn(VALUE_MESSAGE_TS).setPlatform("android").setBuild("build_2")
                        .setCustom01("D").setCustom02("E").setCustom03(VALUE_CUSTOM03_A).setFields("{\"len\":10}").setAge(VALUE_AGE_A)
                        .setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("3").setAppId(VALUE_APP_A).setUserId(VALUE_USER_B).setSessionId("session_2")
                        .setType(EventType.SESSION_STARTED.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("android").setBuild("build_2").setCustom01("D").setCustom02("E").setCustom03(VALUE_CUSTOM03_A)
                        .setFields("{}").setAge(VALUE_AGE_A).setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("4").setAppId(VALUE_APP_A).setUserId(VALUE_USER_B).setSessionId("session_2")
                        .setType(EventType.SESSION_ENDED.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("android").setBuild("build_2").setCustom01("D").setCustom02("E").setCustom03(VALUE_CUSTOM03_A)
                        .setFields("{\"len\":10}").setAge(VALUE_AGE_A)
                        .setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("5").setAppId(VALUE_APP_A).setUserId(VALUE_USER_C).setSessionId("session_3")
                        .setType(EventType.SESSION_STARTED.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("android").setBuild("build_2").setCustom01("D").setCustom02("E").setCustom03(VALUE_CUSTOM03_B)
                        .setAge(VALUE_AGE_B).setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("6").setAppId(VALUE_APP_A).setUserId(VALUE_USER_C).setSessionId("session_3")
                        .setType(EventType.SESSION_ENDED.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("android").setBuild("build_2").setCustom01("D").setCustom02("E").setCustom03(VALUE_CUSTOM03_B)
                        .setFields("{\"len\":10}").setAge(VALUE_AGE_B).setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("7").setAppId(VALUE_APP_A).setUserId(VALUE_USER_A).setSessionId("session_1")
                        .setType(EventType.NEW_USER_CREATED.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("android").setBuild("build_2").setCustom01("D").setCustom02("E").setCustom03(VALUE_CUSTOM03_A)
                        .setAge(VALUE_AGE_A).setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("8").setAppId(VALUE_APP_A).setUserId(VALUE_USER_B).setSessionId("session_2")
                        .setType(EventType.NEW_USER_CREATED.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("android").setBuild("build_2").setCustom01("D").setCustom02("E").setCustom03(VALUE_CUSTOM03_B)
                        .setAge(VALUE_AGE_B).setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("9").setAppId(VALUE_APP_A).setUserId(VALUE_USER_C).setSessionId("session_3")
                        .setType(EventType.NEW_USER_CREATED.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("android").setBuild("build_2").setCustom01("D").setCustom02("E").setCustom03(VALUE_CUSTOM03_A)
                        .setAge(VALUE_AGE_A).setFields("{}").setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("10").setAppId(VALUE_APP_A).setUserId(VALUE_USER_A).setSessionId("session_1")
                        .setType(EventType.TEXT_MESSAGE_SENT.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("ios").setBuild("build_1").setCustom01("A").setCustom02("B").setCustom03(VALUE_CUSTOM03_A)
                        .setAge(VALUE_AGE_A).setFields("{\"content\":\"message_1\",\"nick\":\"actorA\",\"channel\":\"channelA\",\"sentScore\":0.5}")
                        .setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("11").setAppId(VALUE_APP_A).setUserId(VALUE_USER_A).setSessionId("session_3")
                        .setType(EventType.TEXT_MESSAGE_SENT.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("ios").setBuild("build_1").setCustom01("A").setCustom02("B").setCustom03(VALUE_CUSTOM03_A).setAge(VALUE_AGE_A)
                        .setFields("{\"content\":\"message_1\",\"nick\":\"actorA\",\"channel\":\"channelA\",\"sentScore\":-0.5}")
                        .setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("12").setAppId(VALUE_APP_A).setUserId(VALUE_USER_B).setSessionId("session_2")
                        .setType(EventType.TEXT_MESSAGE_SENT.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("android").setBuild("build_2").setCustom01("D").setCustom02("E").setCustom03(VALUE_CUSTOM03_B).setAge(VALUE_AGE_B)
                        .setFields("{\"content\":\"message_2\",\"nick\":\"actorB\",\"channel\":\"channelB\",\"sentScore\":0.0}").setYear("2018")
                        .setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("13").setAppId(VALUE_APP_A).setUserId(VALUE_USER_A).setSessionId("session_1")
                        .setType(EventType.PRODUCT_PURCHASED.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("ios").setBuild("build_1").setCustom01("A").setCustom02("B").setCustom03(VALUE_CUSTOM03_A)
                        .setFields("{\"amount\":99,\"pid\":\"com.test.appa\"}")
                        .setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("14").setAppId(VALUE_APP_A).setUserId(VALUE_USER_B).setSessionId("session_2")
                        .setType(EventType.PRODUCT_PURCHASED.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("android").setBuild("build_2").setCustom01("D").setCustom02("E").setCustom03(VALUE_CUSTOM03_A)
                        .setFields("{\"amount\":101,\"pid\":\"com.test.appa\"}")
                        .setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("15").setAppId(VALUE_APP_A).setUserId(VALUE_USER_B).setSessionId("session_2")
                        .setType(EventType.PRODUCT_PURCHASED.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("android").setBuild("build_2").setCustom01("D").setCustom02("E").setCustom03(VALUE_CUSTOM03_B)
                        .setFields("{\"amount\":101,\"pid\":\"com.test.appa\"}")
                        .setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("16").setAppId(VALUE_APP_A).setUserId(VALUE_USER_C).setSessionId("session_3")
                        .setType(1).setVersion(1).setOccurredOn(VALUE_MESSAGE_TS).setPlatform("android").setBuild("build_2")
                        .setCustom01("D").setCustom02("E").setCustom03(VALUE_CUSTOM03_A)
                        .setFields("{\"content\":\"message_2\",\"nick\":\"actorC\",\"channel\":\"channelB\"}").setAge(VALUE_AGE_A)
                        .setYear("2018").setMonth("5").setDay("8").get());
            }
        };
    }

    @Override
    protected Map<MetricValidator.Key, MetricValidator> validatorsFactory() {
        return new HashMap<MetricValidator.Key,MetricValidator>() {
            {
                put(
                        new MetricValidator.Key(VALUE_APP_A, HiveDefines.MetricSegments.SEGMENT_CUSTOM_03, VALUE_CUSTOM03_A, HiveDefines.MetricId.USER_SESSIONS_MONTHLY.value()),
                        new MetricValidator().count(2.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, HiveDefines.MetricSegments.SEGMENT_CUSTOM_03, VALUE_CUSTOM03_B, HiveDefines.MetricId.USER_SESSIONS_MONTHLY.value()),
                        new MetricValidator().count(1.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, HiveDefines.MetricSegments.SEGMENT_CUSTOM_03, VALUE_CUSTOM03_A, HiveDefines.MetricId.ACTIVE_USERS_MONTHLY.value()),
                        new MetricValidator().count(2.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, HiveDefines.MetricSegments.SEGMENT_CUSTOM_03, VALUE_CUSTOM03_B, HiveDefines.MetricId.ACTIVE_USERS_MONTHLY.value()),
                        new MetricValidator().count(1.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, HiveDefines.MetricSegments.SEGMENT_CUSTOM_03, VALUE_CUSTOM03_A, HiveDefines.MetricId.USER_SESSIONS_DAILY.value()),
                        new MetricValidator().count(2.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, HiveDefines.MetricSegments.SEGMENT_CUSTOM_03, VALUE_CUSTOM03_B, HiveDefines.MetricId.USER_SESSIONS_DAILY.value()),
                        new MetricValidator().count(1.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, HiveDefines.MetricSegments.SEGMENT_CUSTOM_03, VALUE_CUSTOM03_A, HiveDefines.MetricId.ACTIVE_USERS_DAILY.value()),
                        new MetricValidator().count(2.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, HiveDefines.MetricSegments.SEGMENT_CUSTOM_03, VALUE_CUSTOM03_B, HiveDefines.MetricId.ACTIVE_USERS_DAILY.value()),
                        new MetricValidator().count(1.0)
                );

                put(
                        new MetricValidator.Key(VALUE_APP_A, HiveDefines.MetricSegments.SEGMENT_CUSTOM_03, VALUE_CUSTOM03_A, HiveDefines.MetricId.NEW_USERS_DAILY.value()),
                        new MetricValidator().count(2.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, HiveDefines.MetricSegments.SEGMENT_CUSTOM_03, VALUE_CUSTOM03_A, HiveDefines.MetricId.NEW_USERS_MONTHLY.value()),
                        new MetricValidator().count(2.0)
                );

                put(
                        new MetricValidator.Key(VALUE_APP_A, HiveDefines.MetricSegments.SEGMENT_CUSTOM_03, VALUE_CUSTOM03_B, HiveDefines.MetricId.NEW_USERS_DAILY.value()),
                        new MetricValidator().count(1.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, HiveDefines.MetricSegments.SEGMENT_CUSTOM_03, VALUE_CUSTOM03_B, HiveDefines.MetricId.NEW_USERS_MONTHLY.value()),
                        new MetricValidator().count(1.0)
                );

                //Active Paying User
                put(
                        new MetricValidator.Key(VALUE_APP_A, HiveDefines.MetricSegments.SEGMENT_CUSTOM_03, VALUE_CUSTOM03_A, HiveDefines.MetricId.ACTIVE_PAYING_USER_USERS_DAILY.value()),
                        new MetricValidator().count(2.0)
                );

                put(
                        new MetricValidator.Key(VALUE_APP_A, HiveDefines.MetricSegments.SEGMENT_CUSTOM_03, VALUE_CUSTOM03_B, HiveDefines.MetricId.ACTIVE_PAYING_USER_USERS_DAILY.value()),
                        new MetricValidator().count(1.0)
                );

                //-- actions
                put(
                        new MetricValidator.Key(VALUE_APP_A, HiveDefines.MetricSegments.SEGMENT_CUSTOM_03, VALUE_CUSTOM03_A, HiveDefines.MetricId.CHAT_MSGS_DAILY.value()),
                        new MetricValidator().count(2.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, HiveDefines.MetricSegments.SEGMENT_CUSTOM_03, VALUE_CUSTOM03_B, HiveDefines.MetricId.CHAT_MSGS_DAILY.value()),
                        new MetricValidator().count(1.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, HiveDefines.MetricSegments.SEGMENT_CUSTOM_03, VALUE_CUSTOM03_A, HiveDefines.MetricId.CHAT_MSGS_MONTHLY.value()),
                        new MetricValidator().count(2.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, HiveDefines.MetricSegments.SEGMENT_CUSTOM_03, VALUE_CUSTOM03_B, HiveDefines.MetricId.CHAT_MSGS_MONTHLY.value()),
                        new MetricValidator().count(1.0)
                );

                //sentiment score mean
                put(
                        new MetricValidator.Key(VALUE_APP_A, HiveDefines.MetricSegments.SEGMENT_CUSTOM_03, VALUE_CUSTOM03_A, HiveDefines.MetricId.SENTIMENT_DAILY.value()),
                        new MetricValidator().mean(0.0).max(0.5).min(-0.5)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, HiveDefines.MetricSegments.SEGMENT_CUSTOM_03, VALUE_CUSTOM03_B, HiveDefines.MetricId.SENTIMENT_DAILY.value()),
                        new MetricValidator().mean(0.0).max(0.0).min(0.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, HiveDefines.MetricSegments.SEGMENT_CUSTOM_03, VALUE_CUSTOM03_A, HiveDefines.MetricId.SENTIMENT_MONTHLY.value()),
                        new MetricValidator().mean(0.0).max(0.5).min(-0.5)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, HiveDefines.MetricSegments.SEGMENT_CUSTOM_03, VALUE_CUSTOM03_B, HiveDefines.MetricId.SENTIMENT_MONTHLY.value()),
                        new MetricValidator().mean(0.0).max(0.0).min(0.0)
                );

                //revenue
                put(
                        new MetricValidator.Key(VALUE_APP_A, HiveDefines.MetricSegments.SEGMENT_CUSTOM_03, VALUE_CUSTOM03_A, HiveDefines.MetricId.REVENUE_DAILY.value()),
                        new MetricValidator().sum(200.0).max(101.0).min(99.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, HiveDefines.MetricSegments.SEGMENT_CUSTOM_03, VALUE_CUSTOM03_B, HiveDefines.MetricId.REVENUE_DAILY.value()),
                        new MetricValidator().sum(101.0).max(101.0).min(101.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, HiveDefines.MetricSegments.SEGMENT_CUSTOM_03, VALUE_CUSTOM03_A, HiveDefines.MetricId.REVENUE_MONTHLY.value()),
                        new MetricValidator().sum(200.0).max(101.0).min(99.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, HiveDefines.MetricSegments.SEGMENT_CUSTOM_03, VALUE_CUSTOM03_B, HiveDefines.MetricId.REVENUE_MONTHLY.value()),
                        new MetricValidator().sum(101.0).max(101.0).min(101.0)
                );
            }
        };
    }

    @Override
    protected StructType schemaFactory() {
        return new HiveProfileEnrichedEventTableSchema().schema();
    }
}
