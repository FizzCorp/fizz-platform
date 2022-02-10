package io.fizz.analytics.jobs.eventsETL;

import io.fizz.analytics.AbstractSparkTest;
import io.fizz.analytics.common.source.hive.HiveEventFields;
import io.fizz.analytics.common.source.hive.HiveRawEventTableSchema;
import io.fizz.common.domain.EventType;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

public class TranslationETLTransformerTest extends AbstractSparkTest {
    private static class MockInvalidTranslationStore extends MockTranslationStore {
        public MockInvalidTranslationStore(SparkSession aSpark) {
            super(aSpark);
        }

        @Override
        protected List<Row> testDataFactory() {
            final List <Row> rows = super.testDataFactory();

            rows.add(new GenericRowWithSchema(new Object[]{
                "trans", null, "en", "fr", 8, "2018", "2018-04", "2018-04-15"
            }, schema));
            rows.add(new GenericRowWithSchema(new Object[]{
                "trans", "appC", null, "fr", 8, "2018", "2018-04", "2018-04-15"
            }, schema));
            rows.add(new GenericRowWithSchema(new Object[]{
                "trans", "appC", "en", null, 8, "2018", "2018-04", "2018-04-15"
            }, schema));
            rows.add(new GenericRowWithSchema(new Object[]{
                "trans", "appC", "en", "fr", -20, "2018", "2018-04", "2018-04-15"
            }, schema));

            return rows;
        }
    }

    @Test
    @DisplayName("it should ETL translation events")
    void etlTranslationEvents() {
        final TranslationETLTransformer transformer = new TranslationETLTransformer();
        final Dataset<Row> eventsDS = transformer.transform(new MockInvalidTranslationStore(spark).scan(), null);
        final List<Row> events = eventsDS.collectAsList();
        final String APP_A = "appA";
        final String APP_B = "appB";

        assert (events.size() == 3);
        for (final Row row: events) {
            final String appId = HiveRawEventTableSchema.appId(row);

            assert (HiveRawEventTableSchema.eventType(row) == EventType.TEXT_TRANSLATED.value());
            assert (!Objects.isNull(HiveRawEventTableSchema.userId(row)));
            assert (!Objects.isNull(HiveRawEventTableSchema.sessionId(row)));
            assert (appId.equals(APP_A) || appId.equals(APP_B));

            final JSONObject fields = new JSONObject(HiveRawEventTableSchema.fields(row));

            if (fields.getString(HiveEventFields.TRANS_LANG_FROM.value()).equals("en")) {
                assert (fields.getString(HiveEventFields.TRANS_LANG_TO.value()).equals("[fr]"));
            }
            else
            if (fields.getString(HiveEventFields.TRANS_LANG_FROM.value()).equals("fr")) {
                assert (fields.getString(HiveEventFields.TRANS_LANG_TO.value()).equals("[en]"));
            }

            assert (fields.getInt(HiveEventFields.TRANS_TEXT_LEN.value()) > 0);
        }

        //eventsDS.show();
    }
}
