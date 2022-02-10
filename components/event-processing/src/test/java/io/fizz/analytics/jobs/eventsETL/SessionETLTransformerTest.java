package io.fizz.analytics.jobs.eventsETL;

import io.fizz.analytics.AbstractSparkTest;
import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.source.hive.HiveEventFields;
import io.fizz.analytics.common.source.hive.HiveRawEventTableSchema;
import io.fizz.common.domain.EventType;
import io.fizz.common.domain.events.AbstractDomainEvent;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

class SessionETLTransformerTest extends AbstractSparkTest {
    private static class MockInvalidSessionStore extends MockSessionStore {
        public MockInvalidSessionStore(SparkSession aSpark) {
            super(aSpark);
        }

        @Override
        protected List<Row> testDataFactory() {
            final List<Row> rows = super.testDataFactory();

            rows.add(new GenericRowWithSchema(new Object[]{
                "session", "appA", "userA", "userA", "session_end", null, "1517516490202", "1517516490764", "CA", "", 1100, "2018", "2018-04", "2018-04-15"
            }, schema));
            rows.add(new GenericRowWithSchema(new Object[]{
                "session", "appA", "userA", "userA", "session_start", "session_6", null, null, "CA", "", 1100, "2018", "2018-04", "2018-04-15"
            }, schema));
            rows.add(new GenericRowWithSchema(new Object[]{
                "session", "appA", "userA", "userA", "session_end", "session_6", null, null, "CA", "", 1100, "2018", "2018-04", "2018-04-15"
            }, schema));
            rows.add(new GenericRowWithSchema(new Object[]{
                "session", "appA", "userA", "userA", "session_end", "session_7", "1517516490202", "1517516490764", "CA", "", 1100, "2018", "2018-04", "2018-04-15"
            }, schema));
            rows.add(new GenericRowWithSchema(new Object[]{
                "session", "appA", "userA", "userA", "session", "session_8", null, null, "CA", "", 1100, "2018", "2018-04", "2018-04-15"
            }, schema));

            return rows;
        }
    }


    @Test
    @DisplayName("it should perform ETL on session events")
    void validSessionETL() {
        final HiveTime time = new HiveTime(2018, 5, 1);
        final SessionETLTransformer transformer = new SessionETLTransformer();
        final Dataset<Row> eventsDS = transformer.transform(new MockInvalidSessionStore(spark).scan(), time);

        final List<Row> events = eventsDS.collectAsList();
        final HashSet<String> sessions = new HashSet<String>() {
            {
                add("session_1"); add("session_2"); add("session_3"); add("session_4"); add("session_6"); add("session_7");
            }
        };

        for (final Row row: events) {
            final String appId = HiveRawEventTableSchema.appId(row);
            final String userId = HiveRawEventTableSchema.userId(row);
            final String sessionId = HiveRawEventTableSchema.sessionId(row);
            final EventType eventType = EventType.fromValue(HiveRawEventTableSchema.eventType(row));
            final int version = HiveRawEventTableSchema.version(row);

            assert (!Objects.isNull(appId));
            assert (!Objects.isNull(userId));
            assert (!Objects.isNull(sessionId));
            assert (eventType == EventType.SESSION_STARTED || eventType == EventType.SESSION_ENDED);
            assert (version <= AbstractDomainEvent.VERSION);
            assert (sessions.contains(sessionId));

            if (appId.equals("appA") && sessionId.equals("session_7")) {
                final JSONObject json = new JSONObject(HiveRawEventTableSchema.fields(row));
                assert (json.getInt(HiveEventFields.SESSION_LENGTH.value()) == 1);
            }
        }

        //eventsDS.show();
    }
}
