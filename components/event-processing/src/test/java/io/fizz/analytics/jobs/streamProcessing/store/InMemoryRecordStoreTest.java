package io.fizz.analytics.jobs.streamProcessing.store;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.fizz.analytics.AbstractSparkTest;
import io.fizz.analytics.common.source.hive.HiveEventFields;
import io.fizz.analytics.common.source.hive.HiveProfileEnrichedEventTableSchema;
import io.fizz.common.domain.*;
import io.fizz.common.domain.events.*;
import org.apache.spark.sql.Row;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

class InMemoryRecordStoreTest extends AbstractSparkTest {
    private static final String VALUE_APP_ID = "appA";
    private static final String VALUE_PLATFORM = "ios";
    private static final String VALUE_BUILD = "build_1";
    private static final String VALUE_USER_1 = "user_1";
    private static final String VALUE_SESSION_1 = "session_1";
    private static final int VALUE_DURATION = 10;
    private static final String VALUE_TEXT_CONTENT = "text message";
    private static final String VALUE_CHANNEL_ID = "channel_1";
    private static final String VALUE_USER_NICK = "sender_nick";
    private static final String VALUE_COUNTRY_CODE = "PK";
    private static final int VALUE_AMOUNT = 100;
    private static final String VALUE_PRODUCT_ID = "com.test.inapp";
    private static final String VALUE_RECEIPT = "test.receipt";
    private static final String VALUE_FROM = "en";
    private static final String[] VALUE_TO = {"fr", "de"};
    private static final String VALUE_MESSAGE_ID = "test.message.id";
    private static final int VALUE_LENGTH = 40;

    @Test
    @DisplayName("it should create data set with specified records")
    void datasetFromRecordsTest() throws Exception {
        final InMemoryRecordStore store = new InMemoryRecordStore(spark);
        final Gson gson = new GsonBuilder().create();
        final List<String> records = new ArrayList<>();

        final SessionStarted sessionStarted = new SessionStarted.Builder()
                .setVersion(AbstractDomainEvent.VERSION)
                .setId(UUID.randomUUID().toString())
                .setCountryCode(new CountryCode(VALUE_COUNTRY_CODE))
                .setAppId(new ApplicationId(VALUE_APP_ID))
                .setUserId(new UserId(VALUE_USER_1))
                .setSessionId(VALUE_SESSION_1)
                .setOccurredOn(new Date().getTime())
                .setPlatform(new Platform(VALUE_PLATFORM))
                .setBuild(VALUE_BUILD)
                .get();
        records.add(gson.toJson(sessionStarted));

        final SessionEnded sessionEnded = new SessionEnded.Builder()
                .setVersion(AbstractDomainEvent.VERSION)
                .setId(UUID.randomUUID().toString())
                .setCountryCode(new CountryCode(VALUE_COUNTRY_CODE))
                .setAppId(new ApplicationId(VALUE_APP_ID))
                .setUserId(new UserId(VALUE_USER_1))
                .setSessionId(VALUE_SESSION_1)
                .setOccurredOn(new Date().getTime())
                .setPlatform(new Platform(VALUE_PLATFORM))
                .setBuild(VALUE_BUILD)
                .setDuration(VALUE_DURATION)
                .get();
        records.add(gson.toJson(sessionEnded));

        final TextMessageSent textMessageSent = new TextMessageSent.Builder()
                .setVersion(AbstractDomainEvent.VERSION)
                .setId(UUID.randomUUID().toString())
                .setCountryCode(new CountryCode(VALUE_COUNTRY_CODE))
                .setAppId(new ApplicationId(VALUE_APP_ID))
                .setUserId(new UserId(VALUE_USER_1))
                .setSessionId(VALUE_SESSION_1)
                .setOccurredOn(new Date().getTime())
                .setPlatform(new Platform(VALUE_PLATFORM))
                .setBuild(VALUE_BUILD)
                .setContent(VALUE_TEXT_CONTENT)
                .setChannelId(VALUE_CHANNEL_ID)
                .setNick(VALUE_USER_NICK)
                .get();
        records.add(gson.toJson(textMessageSent));

        final ProductPurchased productPurchased = new ProductPurchased.Builder()
                .setVersion(AbstractDomainEvent.VERSION)
                .setId(UUID.randomUUID().toString())
                .setCountryCode(new CountryCode(VALUE_COUNTRY_CODE))
                .setAppId(new ApplicationId(VALUE_APP_ID))
                .setUserId(new UserId(VALUE_USER_1))
                .setSessionId(VALUE_SESSION_1)
                .setOccurredOn(new Date().getTime())
                .setPlatform(new Platform(VALUE_PLATFORM))
                .setBuild(VALUE_BUILD)
                .setAmountInCents(VALUE_AMOUNT)
                .setProductId(VALUE_PRODUCT_ID)
                .setReceipt(VALUE_RECEIPT)
                .get();

        records.add(gson.toJson(productPurchased));

        final TextMessageTranslated textMessageTranslated = new TextMessageTranslated.Builder()
                .setVersion(AbstractDomainEvent.VERSION)
                .setId(UUID.randomUUID().toString())
                .setCountryCode(new CountryCode(VALUE_COUNTRY_CODE))
                .setAppId(new ApplicationId(VALUE_APP_ID))
                .setUserId(new UserId(VALUE_USER_1))
                .setSessionId(VALUE_SESSION_1)
                .setOccurredOn(new Date().getTime())
                .setPlatform(new Platform(VALUE_PLATFORM))
                .setBuild(VALUE_BUILD)
                .setFrom(VALUE_FROM)
                .setTo(VALUE_TO)
                .setMessageId(VALUE_MESSAGE_ID)
                .setLength(VALUE_LENGTH)
                .get();

        records.add(gson.toJson(textMessageTranslated));

        store.put(records);
        final List<Row> rows = store.createDataset().collectAsList();

        for (final Row row: rows) {
            assert (HiveProfileEnrichedEventTableSchema.appId(row).equals(VALUE_APP_ID));
            assert (HiveProfileEnrichedEventTableSchema.userId(row).equals(VALUE_USER_1));
            assert (HiveProfileEnrichedEventTableSchema.version(row) == AbstractDomainEvent.VERSION);
            assert (HiveProfileEnrichedEventTableSchema.sessionId(row).equals(VALUE_SESSION_1));
            assert (HiveProfileEnrichedEventTableSchema.platform(row).equals(VALUE_PLATFORM));
            assert (HiveProfileEnrichedEventTableSchema.build(row).equals(VALUE_BUILD));

            final EventType type = EventType.fromValue(HiveProfileEnrichedEventTableSchema.eventType(row));
            final JSONObject fields = new JSONObject(HiveProfileEnrichedEventTableSchema.fields(row));
            if (type == EventType.SESSION_STARTED) {
                System.out.println("SessionStarted event found");
            }
            else if (type == EventType.SESSION_ENDED) {
                assert (fields.getInt(HiveEventFields.SESSION_LENGTH.value()) == VALUE_DURATION);
            }
            else if (type == EventType.TEXT_MESSAGE_SENT) {
                assert (fields.getString(HiveEventFields.TEXT_MESSAGE_CONTENT.value()).equals(VALUE_TEXT_CONTENT));
                assert (fields.getString(HiveEventFields.TEXT_MESSAGE_CHANNEL.value()).equals(VALUE_CHANNEL_ID));
                assert (fields.getString(HiveEventFields.TEXT_MESSAGE_NICK.value()).equals(VALUE_USER_NICK));
            }
            else if (type == EventType.PRODUCT_PURCHASED) {
               assert  (fields.getInt(HiveEventFields.PURCHASE_AMOUNT.value()) == VALUE_AMOUNT);
               assert (fields.getString(HiveEventFields.PURCHASE_PRODUCT_ID.value()).equals(VALUE_PRODUCT_ID));
               assert (fields.getString(HiveEventFields.PURCHASE_RECEIPT.value()).equals(VALUE_RECEIPT));
            }
            else if (type == EventType.TEXT_TRANSLATED) {
                assert (fields.getString(HiveEventFields.TRANS_LANG_FROM.value()).equals(VALUE_FROM));
                assert (fields.getString(HiveEventFields.TRANS_LANG_TO.value()).equals(Arrays.toString(VALUE_TO)));
                assert (fields.getInt(HiveEventFields.TRANS_TEXT_LEN.value()) == VALUE_LENGTH);
                assert (fields.getString(HiveEventFields.TRANS_TEXT_MESSAGE_ID.value()).equals(VALUE_MESSAGE_ID));
            }
            else {
                assert (false);
            }
        }
    }
}
