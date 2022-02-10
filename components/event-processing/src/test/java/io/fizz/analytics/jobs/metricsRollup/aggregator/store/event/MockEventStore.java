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

public class MockEventStore extends AbstractMockTableDataSource {
    public MockEventStore(SparkSession aSpark) {
        super(aSpark);
    }

    static final String VALUE_APP_A = "appA";
    static final String VALUE_APP_B = "appB";
    static final String VALUE_USER_A = "userA";
    static final String VALUE_USER_B = "userB";
    static final String VALUE_USER_C = "userC";
    static final long VALUE_MESSAGE_TS = 1525794897000L;

    @Override
    protected List<Row> testDataFactory() {
        return new ArrayList<Row>() {
            {
                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("1").setAppId(VALUE_APP_A).setUserId(VALUE_USER_A)
                        .setSessionId("session_1").setType(EventType.SESSION_STARTED.value()).setVersion(AbstractDomainEvent.VERSION)
                        .setOccurredOn(VALUE_MESSAGE_TS).setPlatform("android").setBuild("build_2").setCustom01("D")
                        .setCustom02("E").setCustom01("F").setFields("{}").setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("2").setAppId(VALUE_APP_A).setUserId(VALUE_USER_A)
                        .setSessionId("session_1").setType(EventType.SESSION_ENDED.value()).setVersion(AbstractDomainEvent.VERSION)
                        .setOccurredOn(VALUE_MESSAGE_TS).setPlatform("android").setBuild("build_2")
                        .setCustom01("D").setCustom02("E").setCustom03("F").setFields("{\"len\":10}")
                        .setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("3").setAppId(VALUE_APP_A).setUserId(VALUE_USER_B).setSessionId("session_2")
                        .setType(EventType.SESSION_STARTED.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("android").setBuild("build_2").setCustom01("D").setCustom02("E").setCustom03("F")
                        .setFields("{}").setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("4").setAppId(VALUE_APP_A).setUserId(VALUE_USER_B).setSessionId("session_2")
                        .setType(EventType.SESSION_ENDED.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("android").setBuild("build_2").setCustom01("D").setCustom02("E").setCustom03("F").setFields("{\"len\":10}")
                        .setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("5").setAppId(VALUE_APP_B).setUserId(VALUE_USER_C).setSessionId("session_3")
                        .setType(EventType.SESSION_STARTED.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("android").setBuild("build_2").setCustom01("D").setCustom02("E").setCustom03("F")
                        .setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("6").setAppId(VALUE_APP_B).setUserId(VALUE_USER_C).setSessionId("session_3")
                        .setType(EventType.SESSION_ENDED.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("android").setBuild("build_2").setCustom01("D").setCustom02("E").setCustom03("F")
                        .setFields("{\"len\":10}").setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("7").setAppId(VALUE_APP_A).setUserId(VALUE_USER_A).setSessionId("session_1")
                        .setType(EventType.NEW_USER_CREATED.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("android").setBuild("build_2").setCustom01("D").setCustom02("E").setCustom03("F")
                        .setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("8").setAppId(VALUE_APP_A).setUserId(VALUE_USER_B).setSessionId("session_2")
                        .setType(EventType.NEW_USER_CREATED.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("android").setBuild("build_2").setCustom01("D").setCustom02("E").setCustom03("F")
                        .setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("9").setAppId(VALUE_APP_B).setUserId(VALUE_USER_C).setSessionId("session_3")
                        .setType(EventType.NEW_USER_CREATED.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("android").setBuild("build_2").setCustom01("D").setCustom02("E").setCustom03("F")
                        .setFields("{}").setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("10").setAppId(VALUE_APP_A).setUserId(VALUE_USER_A).setSessionId("session_1")
                        .setType(EventType.TEXT_MESSAGE_SENT.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("ios").setBuild("build_1").setCustom01("A").setCustom02("B").setCustom03("C")
                        .setFields("{\"content\":\"message_1\",\"nick\":\"actorA\",\"channel\":\"channelA\",\"sentScore\":0.5}")
                        .setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("11").setAppId(VALUE_APP_A).setUserId(VALUE_USER_B).setSessionId("session_2")
                        .setType(EventType.TEXT_MESSAGE_SENT.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("android").setBuild("build_2").setCustom01("D").setCustom02("E").setCustom03("F")
                        .setFields("{\"content\":\"message_2\",\"nick\":\"actorB\",\"channel\":\"channelB\",\"sentScore\":-0.5}").setYear("2018")
                        .setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("12").setAppId(VALUE_APP_A).setUserId(VALUE_USER_A).setSessionId("session_1")
                        .setType(EventType.PRODUCT_PURCHASED.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("ios").setBuild("build_1").setCustom01("A").setCustom02("B").setCustom03("C")
                        .setFields("{\"amount\":99,\"pid\":\"com.test.appa\",\"receipt\":\"receiptA\"}")
                        .setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("12").setAppId(VALUE_APP_A).setUserId(VALUE_USER_A).setSessionId("session_1")
                        .setType(EventType.PRODUCT_PURCHASED.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("ios").setBuild("build_1").setCustom01("A").setCustom02("B").setCustom03("C")
                        .setFields("{\"amount\":99,\"pid\":\"com.test.appa\",\"receipt\":\"receiptA\"}")
                        .setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("14").setAppId(VALUE_APP_A).setUserId(VALUE_USER_B).setSessionId("session_2")
                        .setType(EventType.PRODUCT_PURCHASED.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("android").setBuild("build_2").setCustom01("D").setCustom02("E").setCustom03("F")
                        .setFields("{\"amount\":101,\"pid\":\"com.test.appa\",\"receipt\":\"receiptB\"}")
                        .setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("15").setAppId(VALUE_APP_A).setUserId(VALUE_USER_B).setSessionId("session_2")
                        .setType(EventType.PRODUCT_PURCHASED.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("android").setBuild("build_2").setCustom01("D").setCustom02("E").setCustom03("F")
                        .setFields("{\"amount\":101,\"pid\":\"com.test.appa\",\"receipt\":\"receiptB\"}")
                        .setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("16").setAppId(VALUE_APP_A).setUserId(VALUE_USER_B).setSessionId("session_2")
                        .setType(EventType.PRODUCT_PURCHASED.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("android").setBuild("build_2").setCustom01("D").setCustom02("E").setCustom03("F")
                        .setFields("{\"amount\":100,\"pid\":\"com.test.appa\"}")
                        .setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("17").setAppId(VALUE_APP_A).setUserId(VALUE_USER_A).setSessionId("session_1")
                        .setType(EventType.TEXT_TRANSLATED.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("ios").setBuild("build_1").setCustom01("A").setCustom02("B").setCustom03("C")
                        .setFields("{\"messageId\":\"17\",\"from\":\"en\",\"to\":\"[\\\"en\\\", \\\"fr\\\", \\\"de\\\", \\\"ur\\\"]\",\"len\":20}")
                        .setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("17").setAppId(VALUE_APP_A).setUserId(VALUE_USER_A).setSessionId("session_1")
                        .setType(EventType.TEXT_TRANSLATED.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("ios").setBuild("build_1").setCustom01("A").setCustom02("B").setCustom03("C")
                        .setFields("{\"messageId\":\"17\",\"from\":\"en\",\"to\":\"[\\\"en\\\", \\\"fr\\\", \\\"de\\\", \\\"ur\\\"]\",\"len\":20}")
                        .setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("19").setAppId(VALUE_APP_A).setUserId(VALUE_USER_A).setSessionId("session_1")
                        .setType(EventType.TEXT_TRANSLATED.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("ios").setBuild("build_1").setCustom01("A").setCustom02("B").setCustom03("C")
                        .setFields("{\"messageId\":\"17\",\"from\":\"en\",\"to\":\"[\\\"en\\\", \\\"fr\\\", \\\"de\\\", \\\"ur\\\"]\",\"len\":20}")
                        .setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("20").setAppId(VALUE_APP_A).setUserId(VALUE_USER_B).setSessionId("session_2")
                        .setType(EventType.TEXT_TRANSLATED.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("android").setBuild("build_2").setCustom01("D").setCustom02("E").setCustom03("F")
                        .setFields("{\"messageId\":\"20\",\"from\":\"en\",\"to\":\"[\\\"en\\\", \\\"fr\\\", \\\"de\\\", \\\"ur\\\"]\",\"len\":20}")
                        .setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("21").setAppId(VALUE_APP_A).setUserId(VALUE_USER_B).setSessionId("session_2")
                        .setType(EventType.TEXT_TRANSLATED.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("android").setBuild("build_2").setCustom01("D").setCustom02("E").setCustom03("F")
                        .setFields("{\"messageId\":\"21\",\"from\":\"en\",\"to\":\"[\\\"en\\\", \\\"fr\\\", \\\"de\\\", \\\"ur\\\"]\",\"len\":20}")
                        .setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("22").setAppId(VALUE_APP_A).setUserId(VALUE_USER_B).setSessionId("session_2")
                        .setType(EventType.TEXT_TRANSLATED.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                        .setPlatform("android").setBuild("build_2").setCustom01("D").setCustom02("E").setCustom03("F")
                        .setFields("{\"messageId\":\"22\",\"from\":\"fr\",\"to\":\"[\\\"en\\\", \\\"fr\\\", \\\"de\\\", \\\"ur\\\"]\",\"len\":2}")
                        .setYear("2018").setMonth("5").setDay("8").get());

                add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("23").setAppId(VALUE_APP_B).setUserId(VALUE_USER_C).setSessionId("session_3")
                        .setType(1).setVersion(1).setOccurredOn(VALUE_MESSAGE_TS).setPlatform("android").setBuild("build_2")
                        .setCustom01("D").setCustom02("E").setCustom03("F").setFields("{\"content\":\"message_2\",\"nick\":\"actorC\",\"channel\":\"channelB\"}")
                        .setYear("2018").setMonth("5").setDay("8").get());
            }
        };
    }

    @Override
    protected Map<MetricValidator.Key, MetricValidator> validatorsFactory() {
        return new HashMap<MetricValidator.Key,MetricValidator>() {
            {
                put(
                        new MetricValidator.Key(VALUE_APP_A, null, null, HiveDefines.MetricId.ACTIVE_USERS_MONTHLY.value()),
                        new MetricValidator().count(2.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_B, null, null, HiveDefines.MetricId.ACTIVE_USERS_MONTHLY.value()),
                        new MetricValidator().count(1.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, null, null, HiveDefines.MetricId.ACTIVE_USERS_MONTHLY_BILLING.value()),
                        new MetricValidator().count(2.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_B, null, null, HiveDefines.MetricId.ACTIVE_USERS_MONTHLY_BILLING.value()),
                        new MetricValidator().count(1.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, null, null, HiveDefines.MetricId.ACTIVE_USERS_MID_MONTHLY_BILLING.value()),
                        new MetricValidator().count(2.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_B, null, null, HiveDefines.MetricId.ACTIVE_USERS_MID_MONTHLY_BILLING.value()),
                        new MetricValidator().count(1.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, null, null, HiveDefines.MetricId.USER_SESSIONS_DAILY.value()),
                        new MetricValidator().count(2.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_B, null, null, HiveDefines.MetricId.USER_SESSIONS_DAILY.value()),
                        new MetricValidator().count(1.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, null, null, HiveDefines.MetricId.USER_SESSIONS_MONTHLY.value()),
                        new MetricValidator().count(2.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_B, null, null, HiveDefines.MetricId.USER_SESSIONS_MONTHLY.value()),
                        new MetricValidator().count(1.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, null, null, HiveDefines.MetricId.USER_SESSIONS_ATTRIBUTES_DAILY.value()),
                        new MetricValidator().sum(20.0).mean(10.0).min(10.0).max(10.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_B, null, null, HiveDefines.MetricId.USER_SESSIONS_ATTRIBUTES_DAILY.value()),
                        new MetricValidator().sum(10.0).mean(10.0).min(10.0).max(10.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, null, null, HiveDefines.MetricId.USER_SESSIONS_ATTRIBUTES_MONTHLY.value()),
                        new MetricValidator().sum(20.0).mean(10.0).min(10.0).max(10.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_B, null, null, HiveDefines.MetricId.USER_SESSIONS_ATTRIBUTES_MONTHLY.value()),
                        new MetricValidator().sum(10.0).mean(10.0).min(10.0).max(10.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, null, null, HiveDefines.MetricId.ACTIVE_USERS_DAILY.value()),
                        new MetricValidator().count(2.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_B, null, null, HiveDefines.MetricId.ACTIVE_USERS_DAILY.value()),
                        new MetricValidator().count(1.0)
                );

                put(
                        new MetricValidator.Key(VALUE_APP_A, null, null, HiveDefines.MetricId.NEW_USERS_DAILY.value()),
                        new MetricValidator().count(2.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, null, null, HiveDefines.MetricId.NEW_USERS_MONTHLY.value()),
                        new MetricValidator().count(2.0)
                );

                put(
                        new MetricValidator.Key(VALUE_APP_B, null, null, HiveDefines.MetricId.NEW_USERS_DAILY.value()),
                        new MetricValidator().count(1.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_B, null, null, HiveDefines.MetricId.NEW_USERS_MONTHLY.value()),
                        new MetricValidator().count(1.0)
                );

                //Active Paying User
                put(
                        new MetricValidator.Key(VALUE_APP_A, null, null, HiveDefines.MetricId.ACTIVE_PAYING_USER_USERS_DAILY.value()),
                        new MetricValidator().count(2.0)
                );

                //-- actions
                put(
                        new MetricValidator.Key(VALUE_APP_A, null, null, HiveDefines.MetricId.CHAT_MSGS_DAILY.value()),
                        new MetricValidator().count(2.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_B, null, null, HiveDefines.MetricId.CHAT_MSGS_DAILY.value()),
                        new MetricValidator().count(1.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, null, null, HiveDefines.MetricId.CHAT_MSGS_MONTHLY.value()),
                        new MetricValidator().count(2.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_B, null, null, HiveDefines.MetricId.CHAT_MSGS_MONTHLY.value()),
                        new MetricValidator().count(1.0)
                );

                //sentiment score mean
                put(
                        new MetricValidator.Key(VALUE_APP_A, null, null, HiveDefines.MetricId.SENTIMENT_DAILY.value()),
                        new MetricValidator().mean(0.0).max(0.5).min(-0.5)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_B, null, null, HiveDefines.MetricId.SENTIMENT_DAILY.value()),
                        new MetricValidator().mean(0.0).max(0.0).min(0.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, null, null, HiveDefines.MetricId.SENTIMENT_MONTHLY.value()),
                        new MetricValidator().mean(0.0).max(0.5).min(-0.5)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_B, null, null, HiveDefines.MetricId.SENTIMENT_MONTHLY.value()),
                        new MetricValidator().mean(0.0).max(0.0).min(0.0)
                );

                //revenue
                put(
                        new MetricValidator.Key(VALUE_APP_A, null, null, HiveDefines.MetricId.REVENUE_DAILY.value()),
                        new MetricValidator().sum(300.0).max(101.0).min(99.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, null, null, HiveDefines.MetricId.REVENUE_MONTHLY.value()),
                        new MetricValidator().sum(300.0).max(101.0).min(99.0)
                );

                //translation
                put(
                        new MetricValidator.Key(VALUE_APP_A, null, null, HiveDefines.MetricId.TRANSLATION_COUNT_DAILY.value()),
                        new MetricValidator().count(6.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, null, null, HiveDefines.MetricId.TRANSLATION_COUNT_MONTHLY.value()),
                        new MetricValidator().count(4.0)
                );

                put(
                        new MetricValidator.Key(VALUE_APP_A, null, null, HiveDefines.MetricId.CHARS_TRANSLATED_DAILY.value()),
                        new MetricValidator().sum(408.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, null, null, HiveDefines.MetricId.CHARS_TRANSLATED_MONTHLY.value()),
                        new MetricValidator().sum(328.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, null, null, HiveDefines.MetricId.CHARS_TRANSLATED_MONTHLY_BILLING.value()),
                        new MetricValidator().sum(328.0)
                );
                put(
                        new MetricValidator.Key(VALUE_APP_A, null, null, HiveDefines.MetricId.CHARS_TRANSLATED_MID_MONTHLY_BILLING.value()),
                        new MetricValidator().sum(328.0)
                );
            }
        };
    }

    @Override
    protected StructType schemaFactory() {
        return new HiveProfileEnrichedEventTableSchema().schema();
    }
}
