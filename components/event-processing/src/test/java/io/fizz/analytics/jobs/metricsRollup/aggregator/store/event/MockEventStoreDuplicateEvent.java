package io.fizz.analytics.jobs.metricsRollup.aggregator.store.event;

import io.fizz.analytics.common.source.hive.HiveProfileEnrichedEventTableSchema;
import io.fizz.common.domain.EventType;
import io.fizz.common.domain.events.AbstractDomainEvent;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.List;

public class MockEventStoreDuplicateEvent extends MockEventStore {
    public MockEventStoreDuplicateEvent(SparkSession aSpark) {
        super(aSpark);
    }

    @Override
    protected List<Row> testDataFactory() {
        List<Row> data = super.testDataFactory();
        data.add(new HiveProfileEnrichedEventTableSchema.RowBuilder().setId("16").setAppId(VALUE_APP_A).setUserId(VALUE_USER_B).setSessionId("session_2")
                .setType(EventType.PRODUCT_PURCHASED.value()).setVersion(AbstractDomainEvent.VERSION).setOccurredOn(VALUE_MESSAGE_TS)
                .setPlatform("android").setBuild("build_2").setCustom01("D").setCustom02("E").setCustom03("F")
                .setFields("{\"amount\":100,\"pid\":\"com.test.appa\"}")
                .setYear("2018").setMonth("5").setDay("8").get());
        return data;
    }
}
