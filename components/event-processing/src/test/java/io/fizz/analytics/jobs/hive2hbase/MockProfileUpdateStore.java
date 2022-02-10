package io.fizz.analytics.jobs.hive2hbase;

import io.fizz.analytics.common.source.hive.HiveEventFields;
import io.fizz.analytics.common.source.hive.HiveProfileEnrichedEventTableSchema;
import io.fizz.common.domain.EventType;
import io.fizz.common.domain.events.AbstractDomainEvent;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.types.StructType;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MockProfileUpdateStore {
    private static final long EVENT_TS = 1525794897000L;

    private final SparkSession spark;

    MockProfileUpdateStore(final SparkSession aSpark) {
        spark = aSpark;
    }

    Dataset<Row> scan() {
        final StructType schema = new HiveProfileEnrichedEventTableSchema().schema();

        JSONObject fields1 = new JSONObject();
        fields1.put(HiveEventFields.PROFILE_OLDEST_ACTIVITY_TS.value(), 1516095632);
        fields1.put(HiveEventFields.PROFILE_LATEST_ACTIVITY_TS.value(), 1539682832);
        fields1.put(HiveEventFields.PROFILE_SPENT.value(), 99);
        fields1.put(HiveEventFields.PROFILE_SENTIMENT_SUM.value(), 20);
        fields1.put(HiveEventFields.PROFILE_MESSAGE_COUNT.value(), 200);

        JSONObject fields2 = new JSONObject();
        fields2.put(HiveEventFields.PROFILE_OLDEST_ACTIVITY_TS.value(), 1516095632);
        fields2.put(HiveEventFields.PROFILE_LATEST_ACTIVITY_TS.value(), 1539682832);
        fields2.put(HiveEventFields.PROFILE_SPENT.value(), 9999);
        fields2.put(HiveEventFields.PROFILE_SENTIMENT_SUM.value(), -0.5);
        fields2.put(HiveEventFields.PROFILE_MESSAGE_COUNT.value(), 2000);

        final List<Row> rows = new ArrayList<Row>() {
            {
                add(new HiveProfileEnrichedEventTableSchema.RowBuilder()
                        .setId("1").setAppId("appA")
                        .setType(EventType.PROFILE_UPDATED.value())
                        .setUserId("userA")
                        .setSessionId("session_1")
                        .setVersion(AbstractDomainEvent.VERSION)
                        .setOccurredOn(EVENT_TS)
                        .setPlatform("ios")
                        .setVersion(1)
                        .setBuild("build_1")
                        .setCustom01("A").setCustom02("B").setCustom03("C")
                        .setYear("2018")
                        .setMonth("5")
                        .setDay("8")
                        .setAge("days_8_14")
                        .setSpend("none")
                        .setCountryCode("PK")
                        .setFields(fields1.toString())
                        .get()
                );
                add(new HiveProfileEnrichedEventTableSchema.RowBuilder()
                        .setId("2").setAppId("appA")
                        .setType(EventType.PROFILE_UPDATED.value())
                        .setUserId("userB")
                        .setSessionId("session_2")
                        .setVersion(AbstractDomainEvent.VERSION)
                        .setOccurredOn(EVENT_TS)
                        .setPlatform("android")
                        .setVersion(1)
                        .setBuild("build_2")
                        .setCustom01("D").setCustom02("E").setCustom03("F")
                        .setYear("2018")
                        .setMonth("5")
                        .setDay("8")
                        .setAge("days_8_14")
                        .setSpend("none")
                        .setCountryCode("PK")
                        .setFields(fields2.toString())
                        .get()
                );
            }
        };

        return spark.createDataset(rows, RowEncoder.apply(schema));
    }
}

