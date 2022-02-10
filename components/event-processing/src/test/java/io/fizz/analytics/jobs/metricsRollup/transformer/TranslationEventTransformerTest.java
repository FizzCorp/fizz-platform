package io.fizz.analytics.jobs.metricsRollup.transformer;

import io.fizz.analytics.AbstractSparkTest;
import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.source.hive.HiveEventFields;
import io.fizz.analytics.common.source.hive.HiveProfileEnrichedEventTableSchema;
import io.fizz.common.domain.EventType;
import io.fizz.common.domain.events.AbstractDomainEvent;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.types.StructType;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class TranslationEventTransformerTest extends AbstractSparkTest {
    @Test
    @DisplayName("it should transform rich profile events to translation events")
    void translationTransformerTest() {
        final List<Row> events = new ArrayList<Row>() {
            {
                add(createTranslationEvent("1", 5, "en", "[\"en\", \"fr\", \"de\", \"ur\"]"));
                add(createTranslationEvent("2", 20, "de", "[\"en\", \"fr\", \"de\", \"ko\"]"));
            }
        };

        final StructType schema = new HiveProfileEnrichedEventTableSchema().schema();
        final Encoder<Row> encoder = RowEncoder.apply(schema);

        final TranslationEventTransformer transformer = new TranslationEventTransformer();
        Dataset<Row> eventsDS = transformer.transform(spark.createDataset(events, encoder), null);
        Assertions.assertEquals(8, eventsDS.collectAsList().size());
    }

    private Row createTranslationEvent(final String id, final int length, final String from, final String to) {
        Date occurredOn =  new Date();
        final HiveTime time = new HiveTime(occurredOn);
        final JSONObject fields = new JSONObject();
        fields.put(HiveEventFields.TRANS_LANG_FROM.value(), from);
        fields.put(HiveEventFields.TRANS_LANG_TO.value(), to);
        fields.put(HiveEventFields.TRANS_TEXT_LEN.value(), length);

        return new HiveProfileEnrichedEventTableSchema.RowBuilder()
                .setId(id)
                .setAppId("appA")
                .setUserId("useA")
                .setSessionId("session_1")
                .setType(EventType.TEXT_TRANSLATED.value())
                .setVersion(AbstractDomainEvent.VERSION)
                .setOccurredOn(occurredOn.getTime())
                .setCountryCode("US")
                .setBuild("build_2")
                .setPlatform("ios")
                .setAge("days_1_3")
                .setSpend("none")
                .setFields(fields.toString())
                .setYear(Integer.toString(time.year.getValue()))
                .setMonth(time.yyyymmm())
                .setDay(time.yyyymmmdd())
                .get();
    }
}
