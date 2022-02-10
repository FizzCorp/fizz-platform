package io.fizz.analytics.jobs.eventsETL;

import io.fizz.analytics.common.AbstractTransformer;
import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.Utils;
import io.fizz.analytics.common.source.hive.HiveEventFields;
import io.fizz.analytics.common.source.hive.HiveRawEventTableSchema;
import io.fizz.common.domain.CountryCode;
import io.fizz.common.domain.EventType;
import io.fizz.common.domain.events.AbstractDomainEvent;
import io.fizz.analytics.common.source.hive.HiveTransTableSchema;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class TranslationETLTransformer implements AbstractTransformer<Row,Row>, Serializable {
    private static final String TRANS_USER_ID = "fizz-translate";
    private static final String TRANS_USER_SESSION = "fizz-translate-session";
    private static final int EVENT_VER = AbstractDomainEvent.VERSION;

    private static final String KEY_FROM_LANG = HiveEventFields.TRANS_LANG_FROM.value();
    private static final String KEY_TO_LANG = HiveEventFields.TRANS_LANG_TO.value();
    private static final String KEY_LENGTH = HiveEventFields.TRANS_TEXT_LEN.value();

    @Override
    public Dataset<Row> transform(Dataset<Row> sourceDS, HiveTime time) {
        if (Objects.isNull(sourceDS)) {
            throw new IllegalArgumentException("invalid source data set");
        }

        return sourceDS
        .map((MapFunction<Row, Row>) row -> {
            try {
                return new HiveRawEventTableSchema.RowBuilder()
                        .setId(UUID.randomUUID().toString())
                        .setAppId(extractAppId(row))
                        .setUserId(TRANS_USER_ID)
                        .setCountryCode(CountryCode.unknown().value())
                        .setSessionId(TRANS_USER_SESSION)
                        .setType(EventType.TEXT_TRANSLATED.value())
                        .setVersion(EVENT_VER)
                        .setOccurredOn(Utils.fromYYYYMMDD(HiveTransTableSchema.day(row)).getTime())
                        .setFields(extractFields(row))
                        .setYear(HiveTransTableSchema.year(row))
                        .setMonth(HiveTransTableSchema.month(row))
                        .setDay(HiveTransTableSchema.day(row))
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
        final String appId = HiveTransTableSchema.appId(aRow);
        if (Objects.isNull(appId)) {
            throw new Exception("invalid app id");
        }
        return appId;
    }


    private String extractFields(final Row aRow) throws Exception {
        final String srcLang = HiveTransTableSchema.sourceLang(aRow);
        final String destLang = HiveTransTableSchema.destLang(aRow);
        final int length = HiveTransTableSchema.length(aRow);

        if (Objects.isNull(srcLang)) {
            throw new Exception("invalid source language");
        }
        if (Objects.isNull(destLang)) {
            throw new Exception("invalid destination language");
        }
        if (length < 0 ) {
            throw new Exception("invalid translated text length");
        }

        final JSONObject json = new JSONObject();
        json.put(KEY_FROM_LANG, srcLang);
        json.put(KEY_TO_LANG, Arrays.toString(new String[]{destLang}));
        json.put(KEY_LENGTH, length);

        return json.toString();
    }
}
