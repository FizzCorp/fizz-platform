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

class ActionETLTransformerTest extends AbstractSparkTest {
    class MockInvalidActionStore extends MockActionStore {
        public MockInvalidActionStore(SparkSession aSpark) {
            super(aSpark);
        }

        @Override
        protected List<Row> testDataFactory() {
            List<Row> rows = super.testDataFactory();

            rows.add(new GenericRowWithSchema(new Object[]{
                "action", "appA", null, "userD", "1", "roomA", "", "", "1524983609", "", "{\"msg\":\"hello how are you?\"}", "2018", "2018-04", "2018-04-15"
            }, schema));
            rows.add(new GenericRowWithSchema(new Object[]{
                "action", "appA", "userE", "userF", "1", null, "", "", "1524983609", "", "{\"msg\":\"hello how are you?\"}", "2018", "2018-04", "2018-04-15"
            }, schema));
            rows.add(new GenericRowWithSchema(new Object[]{
                "action", "appA", "userF", "userH", "1", "roomA", "", "", "2018-04-15", "", "{\"msg\":\"hello how are you?\"}", "2018", "2018-04", "2018-04-15"
            }, schema));

            return rows;
        }
    }

    @Test
    @DisplayName("it should ETL action events")
    void etlActionEvents() {
        final ActionETLTransformer transformer = new ActionETLTransformer();
        final HiveTime time = new HiveTime(2018, 5, 1);
        final Dataset<Row> actionsDS = transformer.transform(new MockInvalidActionStore(spark).scan(), time);
        final List<Row> actions = actionsDS.collectAsList();
        final String APP_A = "appA";
        final String APP_B = "appB";
        final String USER_A = "userA";
        final String USER_B = "userB";
        final String USER_C = "userC";
        final String ROOM_A = "roomA";
        final String ROOM_B = "roomB";
        final long timestamp = 1524983609;

        final HashSet<String> userIdSet = new HashSet<String>() {
            {
                add(USER_A); add(USER_B); add(USER_C);
            }
        };

        assert (actions.size() == 3);

        for (final Row row: actions) {
            final String appId = HiveRawEventTableSchema.appId(row);
            final String userId = HiveRawEventTableSchema.userId(row);

            assert (HiveRawEventTableSchema.eventType(row) == EventType.TEXT_MESSAGE_SENT.value());
            assert (HiveRawEventTableSchema.version(row) <= AbstractDomainEvent.VERSION);
            assert (appId.equals(APP_A) || appId.equals(APP_B));
            assert (userIdSet.contains(userId));
            assert (HiveRawEventTableSchema.occurredOn(row) == timestamp);

            final JSONObject fields = new JSONObject(HiveRawEventTableSchema.fields(row));
            if (appId.equals(APP_A) && userId.equals(USER_A)) {
                validateFields(fields, USER_A, ROOM_A, "hello how are you?");
            }
            else
            if (appId.equals(APP_A) && userId.equals(USER_B)) {
                validateFields(fields, USER_B, ROOM_A, "i am fine thanks.");
            }
            else {
                validateFields(fields, USER_C, ROOM_B, "hello");
            }
        }
        //actionsDS.show();
    }

    private void validateFields(final JSONObject fields, final String nick, final String channel, final String content) {
        final String KEY_NICK = HiveEventFields.TEXT_MESSAGE_NICK.value();
        final String KEY_CONTENT = HiveEventFields.TEXT_MESSAGE_CONTENT.value();
        final String KEY_CHANNEL = HiveEventFields.TEXT_MESSAGE_CHANNEL.value();
        assert (fields.getString(KEY_NICK).equals(nick));
        assert (fields.getString(KEY_CONTENT).equals(content));
        assert (fields.getString(KEY_CHANNEL).equals(channel));
    }
}
