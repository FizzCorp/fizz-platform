package io.fizz.analytics.jobs.eventsETL;

import io.fizz.analytics.common.AbstractTransformer;
import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.source.hive.HiveEventFields;
import io.fizz.analytics.common.source.hive.HiveRawEventTableSchema;
import io.fizz.common.domain.CountryCode;
import io.fizz.common.domain.EventType;
import io.fizz.common.domain.events.AbstractDomainEvent;
import io.fizz.analytics.common.source.hive.HiveRawEventTableSchema;
import io.fizz.analytics.common.source.hive.HiveSessionTableSchema;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class SessionETLTransformer implements AbstractTransformer<Row,Row>, Serializable {
    private static final String SESSION_TYPE_START = "session_start";
    private static final String SESSION_TYPE_END = "session_end";

    @Override
    public Dataset<Row> transform(Dataset<Row> sourceDS, HiveTime time) {
        if (Objects.isNull(sourceDS)) {
            throw new IllegalArgumentException("invalid source data set");
        }

        final int EVENT_VER = AbstractDomainEvent.VERSION;

        return sourceDS
        .map((MapFunction<Row,Row>)row -> {
            try {
                final EventType eventType = mapEventType(row);
                final String sessionId = extractSessionId(row);
                final long occurredOn = extractTimestamp(eventType, row);
                final String fields = extractFields(eventType, row);

                return new HiveRawEventTableSchema.RowBuilder()
                        .setId(UUID.randomUUID().toString())
                        .setAppId(row.getString(row.fieldIndex(HiveSessionTableSchema.COL_APP_ID)))
                        .setCountryCode(CountryCode.unknown().value())
                        .setUserId(row.getString(row.fieldIndex(HiveSessionTableSchema.COL_USER_APP_ID)))
                        .setSessionId(sessionId)
                        .setType(eventType.value())
                        .setVersion(EVENT_VER)
                        .setOccurredOn(occurredOn)
                        .setFields(fields)
                        .setYear(row.getString(row.fieldIndex(HiveSessionTableSchema.COL_YEAR)))
                        .setMonth(row.getString(row.fieldIndex(HiveSessionTableSchema.COL_MONTH)))
                        .setDay(row.getString(row.fieldIndex(HiveSessionTableSchema.COL_DAY)))
                        .get();
            }
            catch (Exception ex) {
                return new HiveRawEventTableSchema.RowBuilder()
                        .setId("")
                        .setAppId("")
                        .setCountryCode("")
                        .setUserId("")
                        .setSessionId("")
                        .setType(EventType.INVALID.value())
                        .setVersion(EVENT_VER)
                        .get();
            }
        }, RowEncoder.apply(new HiveRawEventTableSchema().schema()))
        .filter((FilterFunction<Row>)row -> HiveRawEventTableSchema.eventType(row) != EventType.INVALID.value());
    }

    private EventType mapEventType(final Row aRow) throws Exception {
        final String sessionType = aRow.getString(aRow.fieldIndex(HiveSessionTableSchema.COL_SESSION_TYPE));
        if (sessionType.equals(SESSION_TYPE_START)) {
            return EventType.SESSION_STARTED;
        }
        else
        if (sessionType.equals(SESSION_TYPE_END)) {
            return EventType.SESSION_ENDED;
        }
        else {
            throw new Exception("invalid_session_type");
        }
    }

    private String extractSessionId(final Row aRow) throws Exception {
        final String id = aRow.getString(aRow.fieldIndex(HiveSessionTableSchema.COL_SESSION_ID));
        if (Objects.isNull(id)) {
            throw new Exception("invalid_session_id");
        }
        return id;
    }

    private long extractTimestamp(final EventType aType, final Row aRow) throws Exception {
        String rowTS;
        if (aType == EventType.SESSION_STARTED) {
            rowTS = aRow.getString(aRow.fieldIndex(HiveSessionTableSchema.COL_START_TIME));
        }
        else {
            rowTS = aRow.getString(aRow.fieldIndex(HiveSessionTableSchema.COL_END_TIME));
        }

        if (Objects.isNull(rowTS)) {
            return System.currentTimeMillis();
        }

        final long ts = Long.parseLong(rowTS);
        if (ts > System.currentTimeMillis()) {
            throw new Exception("cannot provide a future date");
        }

        return ts;
    }

    private String extractFields(final EventType type, final Row aRow) {
        final JSONObject json = new JSONObject();

        if (type == EventType.SESSION_STARTED) {
            return json.toString();
        }

        final int durationMS = aRow.getInt(aRow.fieldIndex(HiveSessionTableSchema.COL_DURATION));

        json.put(HiveEventFields.SESSION_LENGTH.value(), durationMS/1000);

        return json.toString();
    }
}
