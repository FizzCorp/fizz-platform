package io.fizz.analytics.jobs.eventsETL;

import io.fizz.analytics.common.AbstractTransformer;
import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.source.hive.HiveEventFields;
import io.fizz.analytics.common.source.hive.HiveRawEventTableSchema;
import io.fizz.analytics.common.source.hive.HiveRawEventTableSchema;
import io.fizz.common.domain.CountryCode;
import io.fizz.common.domain.EventType;
import io.fizz.common.domain.events.AbstractDomainEvent;
import io.fizz.analytics.common.source.hive.HiveActionTableSchema;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class ActionETLTransformer implements AbstractTransformer<Row,Row>, Serializable {
    @Override
    public Dataset<Row> transform(Dataset<Row> sourceDS, HiveTime time) {
        if (Objects.isNull(sourceDS)) {
            throw new IllegalArgumentException("invalid source data set");
        }

        final int EVENT_VER = AbstractDomainEvent.VERSION;

        return sourceDS
        .filter((FilterFunction<Row>)row -> HiveActionTableSchema.actionType(row).equals("1"))
        .map((MapFunction<Row,Row>)row -> {
            try {
                return new HiveRawEventTableSchema.RowBuilder()
                        .setId(UUID.randomUUID().toString())
                        .setAppId(extractAppId(row))
                        .setCountryCode(CountryCode.unknown().value())
                        .setUserId(extractSenderId(row))
                        .setSessionId(UUID.randomUUID().toString())
                        .setType(EventType.TEXT_MESSAGE_SENT.value())
                        .setVersion(EVENT_VER)
                        .setOccurredOn(extractTimestamp(row))
                        .setFields(extractFields(row))
                        .setYear(HiveActionTableSchema.year(row))
                        .setMonth(HiveActionTableSchema.month(row))
                        .setDay(HiveActionTableSchema.day(row))
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

    private String extractAppId(final Row aRow) throws Exception {
        final String appId = HiveActionTableSchema.appId(aRow);
        if (Objects.isNull(appId)) {
            throw new Exception("invalid application id");
        }

        return appId;
    }

    private String extractSenderId(final Row aRow) throws Exception {
        final String senderId = HiveActionTableSchema.senderId(aRow);
        if (Objects.isNull(senderId)) {
            throw new Exception("invalid sender id");
        }
        return senderId;
    }

    private String extractFields(final Row aRow) throws Exception {
        final JSONObject json = new JSONObject();
        final String roomId = HiveActionTableSchema.roomId(aRow);
        if (Objects.isNull(roomId)) {
            throw new Exception("invalid channel id");
        }

        final String fields = aRow.getString(aRow.fieldIndex(HiveActionTableSchema.COL_FIELDS));
        if (Objects.isNull(fields)) {
            throw new Exception("invalid fields");
        }
        final JSONObject fieldsJSON = new JSONObject(fields);
        final String msg = fieldsJSON.getString("msg");
        if (Objects.isNull(msg)) {
            throw new Exception("invalid message");
        }

        json.put(HiveEventFields.TEXT_MESSAGE_CONTENT.value(), msg);
        json.put(HiveEventFields.TEXT_MESSAGE_CHANNEL.value(), roomId);
        json.put(HiveEventFields.TEXT_MESSAGE_NICK.value(), HiveActionTableSchema.nick(aRow));

        return json.toString();
    }

    private long extractTimestamp(final Row aRow) throws Exception {
        final String rowTS = HiveActionTableSchema.timestamp(aRow);
        if (Objects.isNull(rowTS)) {
            return System.currentTimeMillis();
        }

        final long ts = Long.parseLong(rowTS);
        if (ts > System.currentTimeMillis()) {
            throw new Exception("cannot provide a future date");
        }

        return ts;
    }
}
