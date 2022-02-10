package io.fizz.analytics.jobs.streamProcessing.store;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.fizz.analytics.common.HiveTime;
import io.fizz.common.LoggingService;
import io.fizz.analytics.common.source.hive.HiveEventFields;
import io.fizz.analytics.common.source.hive.HiveRawEventTableSchema;
import io.fizz.common.domain.events.*;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.types.StructType;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InMemoryRecordStore implements AbstractRecordStore<Row> {
    private static final LoggingService.Log logger = LoggingService.getLogger(InMemoryRecordStore.class);
    private static final StructType schema = new HiveRawEventTableSchema().schema();

    private final List<Row> cache = new ArrayList<>();
    private final SparkSession spark;

    private final Object lock = new Object();

    public InMemoryRecordStore(final SparkSession aSpark) {
        if (Objects.isNull(aSpark)) {
            throw new IllegalArgumentException("invalid spark session specified.");
        }

        spark = aSpark;
    }

    @Override
    public void put(List<String> aRecords) {
        if (Objects.isNull(aRecords) || aRecords.size() <= 0) {
            return;
        }

        synchronized (lock) {
            final Gson gson = new GsonBuilder()
                    .registerTypeAdapter(AbstractDomainEvent.class, new DomainEventDeserializer())
                    .create();

            for (final String data: aRecords) {
                final AbstractDomainEvent event = gson.fromJson(data, AbstractDomainEvent.class);
                if (Objects.isNull(event)) {
                    logger.warn("Unsupported event encountered: " + data);
                    continue;
                }

                cache.add(parseFields(event));
            }
        }
    }

    @Override
    public Dataset<Row> createDataset() {
        return spark.createDataset(cache, RowEncoder.apply(schema));
    }

    private Row parseFields(final AbstractDomainEvent aEvent) {
        final HiveRawEventTableSchema.RowBuilder builder = new HiveRawEventTableSchema.RowBuilder();
        final HiveTime time = new HiveTime(aEvent.occurredOn());

        builder
        .setId(aEvent.id())
        .setAppId(aEvent.appId().value())
        .setUserId(aEvent.userId().value())
        .setSessionId(aEvent.sessionId())
        .setType(aEvent.type().value())
        .setVersion(aEvent.version())
        .setOccurredOn(aEvent.occurredOn());

        if (!Objects.isNull(aEvent.countryCode())) {
            builder.setCountryCode(aEvent.countryCode().value());
        }
        if (!Objects.isNull(aEvent.platform())) {
            builder.setPlatform(aEvent.platform().value());
        }
        if (!Objects.isNull(aEvent.build())) {
            builder.setBuild(aEvent.build());
        }
        if (!Objects.isNull(aEvent.custom01())) {
            builder.setCustom01(aEvent.custom01());
        }
        if (!Objects.isNull(aEvent.custom02())) {
            builder.setCustom02(aEvent.custom02());
        }
        if (!Objects.isNull(aEvent.custom03())) {
            builder.setCustom03(aEvent.custom03());
        }

        switch (aEvent.type()) {
            case SESSION_STARTED:
                builder.setFields(parseFields((SessionStarted)aEvent));
                break;
            case SESSION_ENDED:
                builder.setFields(parseFields((SessionEnded) aEvent));
                break;
            case PRODUCT_PURCHASED:
                builder.setFields(parseFields((ProductPurchased)aEvent));
                break;
            case TEXT_MESSAGE_SENT:
                builder.setFields(parseFields((TextMessageSent) aEvent));
                break;
            case TEXT_TRANSLATED:
                builder.setFields(parseFields((TextMessageTranslated)aEvent));
                break;
            default:
                logger.error("unsupported event type found while parsing fields.");
                break;
        }

        builder.setYear(Integer.toString(time.year.getValue()));
        builder.setMonth(time.yyyymmm());
        builder.setDay(time.yyyymmmdd());

        return builder.get();
    }

    private String parseFields(final SessionStarted aEvent) {
        return "{}";
    }

    private String parseFields(final SessionEnded aEvent) {
        final JSONObject obj = new JSONObject();
        obj.put(HiveEventFields.SESSION_LENGTH.value(), aEvent.duration());
        return obj.toString();
    }

    private String parseFields(final TextMessageSent aEvent) {
        final JSONObject obj = new JSONObject();

        obj.put(HiveEventFields.TEXT_MESSAGE_CONTENT.value(), aEvent.content());
        obj.put(HiveEventFields.TEXT_MESSAGE_CHANNEL.value(), aEvent.channelId());
        if (!Objects.isNull(aEvent.nick())) {
            obj.put(HiveEventFields.TEXT_MESSAGE_NICK.value(), aEvent.nick());
        }

        return obj.toString();
    }

    private String parseFields(final ProductPurchased aEvent) {
        final JSONObject obj = new JSONObject();

        obj.put(HiveEventFields.PURCHASE_PRODUCT_ID.value(), aEvent.getProductId());
        obj.put(HiveEventFields.PURCHASE_AMOUNT.value(), aEvent.getAmountInCents());
        obj.put(HiveEventFields.PURCHASE_RECEIPT.value(), aEvent.getReceipt());

        return obj.toString();
    }

    private String parseFields(final TextMessageTranslated aEvent) {
        final JSONObject obj = new JSONObject();

        obj.put(HiveEventFields.TRANS_LANG_FROM.value(), aEvent.from());
        obj.put(HiveEventFields.TRANS_LANG_TO.value(), aEvent.to());
        obj.put(HiveEventFields.TRANS_TEXT_LEN.value(), aEvent.length());
        obj.put(HiveEventFields.TRANS_TEXT_MESSAGE_ID.value(), aEvent.messageId());

        return obj.toString();
    }
}
