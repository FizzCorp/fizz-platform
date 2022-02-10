package io.fizz.analytics.jobs.profileBuildup;

import com.google.common.collect.Lists;
import io.fizz.analytics.AbstractSparkTest;
import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.Segmentation;
import io.fizz.analytics.common.Utils;
import io.fizz.analytics.common.source.hive.*;
import io.fizz.analytics.domain.User;
import io.fizz.common.domain.*;
import io.fizz.common.domain.events.AbstractDomainEvent;
import io.fizz.analytics.common.InMemoryUserRepository;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.types.StructType;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

class ProfileSegmentTransformerTest extends AbstractSparkTest {
    private final static String VALUE_APP_A = "appA";
    private final static String VALUE_SESSION_1 = "session_1";
    private final static long MILLIS_IN_DAY = 24*60*60*1000;

    @Test
    @DisplayName("it should add day 1 to 3 segmentValue to events for first time user")
    void lifecycleSegmentDay1To3Test() throws Exception {
        final Date origin = new Date();

        testLifecycleSegment(origin, origin, UUID.randomUUID().toString(), true, Segmentation.SEGMENT_DAY_1_3);
        testLifecycleSegment(addDays(origin, -2), origin, UUID.randomUUID().toString(), true, Segmentation.SEGMENT_DAY_1_3);
    }

    @Test
    @DisplayName("it should add day 4 to 7 segmentValue to events for first time user")
    void lifecycleSegmentDay4To7Test() throws Exception {
        final Date origin = new Date();
        testLifecycleSegment(addDays(origin, -3), origin, UUID.randomUUID().toString(), true, Segmentation.SEGMENT_DAY_4_7);
        testLifecycleSegment(addDays(origin, -6), origin,UUID.randomUUID().toString(), true, Segmentation.SEGMENT_DAY_4_7);
    }

    @Test
    @DisplayName("it should add day 8 to 14 segmentValue to events for first time user")
    void lifecycleSegmentDay8To14Test() throws Exception {
        final Date origin = new Date();
        testLifecycleSegment(addDays(origin, -7), origin, UUID.randomUUID().toString(), true, Segmentation.SEGMENT_DAY_8_14);
        testLifecycleSegment(addDays(origin, -13), origin, UUID.randomUUID().toString(), true, Segmentation.SEGMENT_DAY_8_14);
    }

    @Test
    @DisplayName("it should add day 15 to 30 segmentValue to events for first time user")
    void lifecycleSegmentDay15To30Test() throws Exception {
        final Date origin = new Date();
        testLifecycleSegment(addDays(origin, -14), origin, UUID.randomUUID().toString(), true, Segmentation.SEGMENT_DAY_15_30);
        testLifecycleSegment(addDays(origin, -29), origin, UUID.randomUUID().toString(), true, Segmentation.SEGMENT_DAY_15_30);
    }

    @Test
    @DisplayName("it should add day 30+ segmentValue to events for first time user")
    void lifecycleSegmentDay31AndAboveTest() throws Exception {
        final Date origin = new Date();
        testLifecycleSegment(addDays(origin, -30), origin, UUID.randomUUID().toString(), true, Segmentation.SEGMENT_DAY_31_);
    }

    @Test
    @DisplayName("it should update user's profile for multiple sessions")
    void multipleSessionsTest() throws Exception {
        final Date latest = new Date();
        final Date oldest = addDays(latest, -8);

        String userId = UUID.randomUUID().toString();

        testLifecycleSegment(oldest, oldest, userId, true, Segmentation.SEGMENT_DAY_1_3);

        final InMemoryUserRepository repo = new InMemoryUserRepository();
        repo.save(
                new User.Builder()
                        .setId(userId)
                        .setAppId(VALUE_APP_A)
                        .setFirstTimeActiveTS(oldest.getTime())
                        .setLastTimeActiveTS(oldest.getTime())
                        .get()
        );

        testLifecycleSegment(latest, latest, userId, false, Segmentation.SEGMENT_DAY_8_14);
    }

    @Test
    @DisplayName("it should calculate actor spent amount correctly")
    void monetizationSegmentTest() throws DomainErrorException, IOException {
        final String userId = UUID.randomUUID().toString();
        final Date origin = new Date();
        final StructType schema = new HiveSentimentEventTableSchema().schema();
        final InMemoryProfileSegmentTransformer transformer = new InMemoryProfileSegmentTransformer();
        final Encoder<Row> encoder = RowEncoder.apply(schema);

        HiveTime eventTime = new HiveTime(addDays(origin, -4).getTime());
        final List<Row> events = new ArrayList<Row>() {
            {
                add(createPurchaseEvent("1", userId,10, "product_a", addDays(origin, -5).getTime()));
                add(createPurchaseEvent("2", userId,15, "product_b", addDays(origin, -4).getTime()));
            }
        };
        List<Row> segmentedEvents = transformer.transform(spark.createDataset(events, encoder), eventTime).collectAsList();
        boolean profileUpdateEventFound = false;
        for (final Row event: segmentedEvents) {
            EventType type = EventType.fromValue(HiveProfileEnrichedEventTableSchema.eventType(event));
            if (type == EventType.PROFILE_UPDATED) {
                profileUpdateEventFound = true;
                JSONObject fields = new JSONObject(HiveProfileEnrichedEventTableSchema.fields(event));
                Assertions.assertEquals (fields.getLong(HiveEventFields.PROFILE_SPENT.value()), 25);
            }
            Assertions.assertEquals(HiveProfileEnrichedEventTableSchema.spend(event), Segmentation.SEGMENT_MINNOW.getTitle());
        }
        Assertions.assertTrue (profileUpdateEventFound);

        final InMemoryUserRepository repo = new InMemoryUserRepository();
        repo.save(
                new User.Builder()
                        .setId(userId)
                        .setAppId(VALUE_APP_A)
                        .setAmountSpentInCents(25)
                        .setFirstTimeActiveTS(10)
                        .setLastTimeActiveTS(20)
                        .get()
        );

        final List<Row> events2 = new ArrayList<Row>() {
            {
                add(createPurchaseEvent("3", userId, 20, "product_a", addDays(origin, -3).getTime()));
                add(createPurchaseEvent("4", userId,1500, "product_b", addDays(origin, -2).getTime()));
            }
        };
        profileUpdateEventFound = false;
        segmentedEvents = transformer.transform(spark.createDataset(events2, RowEncoder.apply(schema)), eventTime).collectAsList();
        for (final Row event: segmentedEvents) {
            EventType type = EventType.fromValue(HiveProfileEnrichedEventTableSchema.eventType(event));
            if (type == EventType.PROFILE_UPDATED) {
                profileUpdateEventFound = true;
                JSONObject fields = new JSONObject(HiveProfileEnrichedEventTableSchema.fields(event));
                Assertions.assertEquals (fields.getLong(HiveEventFields.PROFILE_SPENT.value()), 1545);
            }
            else {
                Assertions.assertEquals(HiveProfileEnrichedEventTableSchema.spend(event), Segmentation.SEGMENT_DOLPHIN.getTitle());
            }
        }
        Assertions.assertTrue (profileUpdateEventFound);
    }

    @Test
    @DisplayName("it should validate profile update event")
    void profileUpdateEventTest() throws DomainErrorException, IOException {
        final String userId = UUID.randomUUID().toString();
        final Date origin = new Date();
        final StructType schema = new HiveSentimentEventTableSchema().schema();
        final InMemoryProfileSegmentTransformer transformer = new InMemoryProfileSegmentTransformer();
        final Encoder<Row> encoder = RowEncoder.apply(schema);

        final long t1 = addDays(origin, -5).getTime();
        final long t2 = addDays(origin, -4).getTime();

        HiveTime eventTime = new HiveTime(t1);
        long processingTime = Utils.fromYYYYMMDD(eventTime.yyyymmmdd().toString()).getTime();

        final List<Row> events = createTestDataset(userId, t1, t2).collectAsList();
        Dataset<Row> eventsDS = transformer.transform(spark.createDataset(events, encoder), eventTime);
        Dataset<Row> updateProfileEventDS = eventsDS.filter((FilterFunction<Row>) row -> HiveProfileEnrichedEventTableSchema.eventType(row) == EventType.PROFILE_UPDATED.value());
        List<Row> updateProfileEvents = updateProfileEventDS.collectAsList();

        Assertions.assertTrue(updateProfileEvents.size() > 0);
        
        for (final Row event: updateProfileEvents) {
            JSONObject fields = new JSONObject(HiveProfileEnrichedEventTableSchema.fields(event));
            Assertions.assertEquals (fields.getLong(HiveEventFields.PROFILE_OLDEST_ACTIVITY_TS.value()), processingTime);
            Assertions.assertEquals (fields.getLong(HiveEventFields.PROFILE_LATEST_ACTIVITY_TS.value()), t2);
            Assertions.assertEquals (fields.getLong(HiveEventFields.PROFILE_SPENT.value()), 0);
            Assertions.assertEquals (HiveProfileEnrichedEventTableSchema.spend(event), Segmentation.SEGMENT_NONE.getTitle());
            Assertions.assertEquals (HiveProfileEnrichedEventTableSchema.age(event), Segmentation.SEGMENT_DAY_1_3.getTitle());
            Assertions.assertEquals (HiveProfileEnrichedEventTableSchema.countryCode(event), "PK");
            Assertions.assertEquals (HiveProfileEnrichedEventTableSchema.build(event), "build_1");
            Assertions.assertEquals (HiveProfileEnrichedEventTableSchema.platform(event), "android");
        }

        final InMemoryUserRepository repo = new InMemoryUserRepository();
        repo.save(
                new User.Builder()
                        .setId(userId)
                        .setAppId(VALUE_APP_A)
                        .setAmountSpentInCents(0)
                        .setFirstTimeActiveTS(t1)
                        .setLastTimeActiveTS(t2)
                        .setLocation("PK")
                        .setBuild("build_1")
                        .setPlatform("android")
                        .get()
        );

        final long t3 = addDays(origin, -3).getTime();
        final long t4 = addDays(origin, -2).getTime();

        eventTime = new HiveTime(t4);

        final List<Row> events2 = new ArrayList<Row>() {
            {
                add(createPurchaseEvent("1", userId,100, "product_a", t3));
                add(createPurchaseEvent("2", userId,150, "product_b", t4));
            }
        };

        eventsDS = transformer.transform(spark.createDataset(events2, RowEncoder.apply(schema)), eventTime);
        updateProfileEventDS = eventsDS.filter((FilterFunction<Row>) row -> HiveProfileEnrichedEventTableSchema.eventType(row) == EventType.PROFILE_UPDATED.value());
        updateProfileEvents = updateProfileEventDS.collectAsList();

        Assertions.assertTrue(updateProfileEvents.size() > 0);

        for (final Row event: updateProfileEvents) {
            JSONObject fields = new JSONObject(HiveProfileEnrichedEventTableSchema.fields(event));
            Assertions.assertEquals (fields.getLong(HiveEventFields.PROFILE_OLDEST_ACTIVITY_TS.value()), t1);
            Assertions.assertEquals (fields.getLong(HiveEventFields.PROFILE_LATEST_ACTIVITY_TS.value()), t4);
            Assertions.assertEquals (fields.getLong(HiveEventFields.PROFILE_SPENT.value()), 250);
            Assertions.assertEquals (HiveProfileEnrichedEventTableSchema.spend(event), Segmentation.SEGMENT_MINNOW.getTitle());
            Assertions.assertEquals (HiveProfileEnrichedEventTableSchema.age(event), Segmentation.SEGMENT_DAY_4_7.getTitle());
            Assertions.assertEquals (HiveProfileEnrichedEventTableSchema.countryCode(event), "US");
            Assertions.assertEquals (HiveProfileEnrichedEventTableSchema.build(event), "build_2");
            Assertions.assertEquals (HiveProfileEnrichedEventTableSchema.platform(event), "ios");
        }
    }

    @Test
    @DisplayName("it should validate profile update event")
    void profileUpdateEventSpendSegment1Test() throws DomainErrorException, IOException {
        profileUpdateEventSpendSegmentTest(Segmentation.SEGMENT_MINNOW.getTitle(), Segmentation.SEGMENT_DOLPHIN.getTitle(),
                100, 150, 1000, 1500);
    }

    @Test
    @DisplayName("it should validate profile update event")
    void profileUpdateEventSpendSegment2Test() throws DomainErrorException, IOException {
        profileUpdateEventSpendSegmentTest(Segmentation.SEGMENT_DOLPHIN.getTitle(), Segmentation.SEGMENT_WHALE.getTitle(),
                1000, 1500, 10000, 15000);
    }

    @Test
    @DisplayName("it should validate profile update event")
    void profileUpdateEventWitInvalidValuesTest() throws DomainErrorException, IOException {
        final String userId = UUID.randomUUID().toString();
        final Date origin = new Date();
        final StructType schema = new HiveSentimentEventTableSchema().schema();
        final InMemoryProfileSegmentTransformer transformer = new InMemoryProfileSegmentTransformer();
        final Encoder<Row> encoder = RowEncoder.apply(schema);

        final long t1 = addDays(origin, -5).getTime();
        final long t2 = addDays(origin, -4).getTime();

        HiveTime eventTime = new HiveTime(t1);
        long processingTime = Utils.fromYYYYMMDD(eventTime.yyyymmmdd()).getTime();

        final List<Row> events = createTestDatasetInvalidValues(userId, t1, t2).collectAsList();
        Dataset<Row> eventsDS = transformer.transform(spark.createDataset(events, encoder), eventTime);
        Dataset<Row> updateProfileEventDS = eventsDS.filter((FilterFunction<Row>) row -> HiveProfileEnrichedEventTableSchema.eventType(row) == EventType.PROFILE_UPDATED.value());
        List<Row> updateProfileEvents = updateProfileEventDS.collectAsList();

        Assertions.assertTrue(updateProfileEvents.size() > 0);

        for (final Row event: updateProfileEvents) {
            JSONObject fields = new JSONObject(HiveProfileEnrichedEventTableSchema.fields(event));
            Assertions.assertEquals (fields.getLong(HiveEventFields.PROFILE_OLDEST_ACTIVITY_TS.value()), processingTime);
            Assertions.assertEquals (fields.getLong(HiveEventFields.PROFILE_LATEST_ACTIVITY_TS.value()), t2);
            Assertions.assertEquals (fields.getLong(HiveEventFields.PROFILE_SPENT.value()), 0);
            Assertions.assertEquals (HiveProfileEnrichedEventTableSchema.spend(event), Segmentation.SEGMENT_NONE.getTitle());
            Assertions.assertEquals (HiveProfileEnrichedEventTableSchema.age(event), Segmentation.SEGMENT_DAY_1_3.getTitle());
            Assertions.assertEquals (HiveProfileEnrichedEventTableSchema.countryCode(event), "unknown");
            Assertions.assertEquals (HiveProfileEnrichedEventTableSchema.build(event), "unknown");
            Assertions.assertEquals (HiveProfileEnrichedEventTableSchema.platform(event), "unknown");
        }
    }

    @Test
    @DisplayName("it should validate profile update event")
    void profileUpdateEventCustomAttributeTest() throws DomainErrorException, IOException {
        final String userId = UUID.randomUUID().toString();
        final Date origin = new Date();
        final StructType schema = new HiveSentimentEventTableSchema().schema();
        final InMemoryProfileSegmentTransformer transformer = new InMemoryProfileSegmentTransformer();
        final Encoder<Row> encoder = RowEncoder.apply(schema);

        final long t1 = addDays(origin, -5).getTime();
        final long t2 = addDays(origin, -4).getTime();

        HiveTime eventTime = new HiveTime(t2);

        final List<Row> events = createTestDataset(userId, t1, t2).collectAsList();
        Dataset<Row> eventsDS = transformer.transform(spark.createDataset(events, encoder), eventTime);
        Dataset<Row> updateProfileEventDS = eventsDS.filter((FilterFunction<Row>) row -> HiveProfileEnrichedEventTableSchema.eventType(row) == EventType.PROFILE_UPDATED.value());
        List<Row> updateProfileEvents = updateProfileEventDS.collectAsList();

        Assertions.assertTrue(updateProfileEvents.size() > 0);

        for (final Row event: updateProfileEvents) {
            Assertions.assertEquals (HiveProfileEnrichedEventTableSchema.custom01(event), "B");
            Assertions.assertEquals (HiveProfileEnrichedEventTableSchema.custom02(event), "123!");
            Assertions.assertNull(HiveProfileEnrichedEventTableSchema.custom03(event));
        }
    }

    @Test
    @DisplayName("it should validate profile update event")
    void sentimentScoredTest() throws DomainErrorException, IOException {
        final String userId = UUID.randomUUID().toString();
        final Date origin = new Date();
        final StructType schema = new HiveSentimentEventTableSchema().schema();
        final InMemoryProfileSegmentTransformer transformer = new InMemoryProfileSegmentTransformer();
        final Encoder<Row> encoder = RowEncoder.apply(schema);

        final long time = addDays(origin, -5).getTime();
        HiveTime eventTime = new HiveTime(time);

        final List<Row> events = Lists.newArrayList(createTextMessageSentEvent(userId, time, 0.5));
        Dataset<Row> eventDS = spark.createDataset(events, encoder);
        Dataset<Row> enrichedEventsDS = transformer.transform(eventDS, eventTime);
        Dataset<Row> messageDS = enrichedEventsDS.filter((FilterFunction<Row>) row -> HiveProfileEnrichedEventTableSchema.eventType(row) == EventType.TEXT_MESSAGE_SENT.value());
        List<Row> messages = messageDS.collectAsList();

        Assertions.assertTrue(messages.size() > 0);

        for (final Row event: messages) {
            JSONObject fields = new JSONObject(HiveProfileEnrichedEventTableSchema.fields(event));
            Assertions.assertTrue (fields.has(HiveEventFields.SENTIMENT_SCORE.value()));
            Assertions.assertEquals(fields.getDouble(HiveEventFields.SENTIMENT_SCORE.value()), 0.5);
        }
    }

    private void profileUpdateEventSpendSegmentTest(String preSegment, String postSegment,
                                                    long amount1, long amount2, long amount3, long amount4) throws DomainErrorException, IOException {
        final String userId = UUID.randomUUID().toString();
        final Date origin = new Date();
        final StructType schema = new HiveSentimentEventTableSchema().schema();
        final InMemoryProfileSegmentTransformer transformer = new InMemoryProfileSegmentTransformer();
        final Encoder<Row> encoder = RowEncoder.apply(schema);

        final long t1 = addDays(origin, -5).getTime();
        final long t2 = addDays(origin, -4).getTime();

        HiveTime eventTime = new HiveTime(t1);

        final List<Row> events = new ArrayList<Row>() {
            {
                add(createPurchaseEvent("1", userId, amount1, "product_a", t1));
                add(createPurchaseEvent("2", userId, amount2, "product_b", t2));
            }
        };

        Dataset<Row> eventsDS = transformer.transform(spark.createDataset(events, encoder), eventTime);
        Dataset<Row> updateProfileEventDS = eventsDS.filter((FilterFunction<Row>) row -> HiveProfileEnrichedEventTableSchema.eventType(row) == EventType.PROFILE_UPDATED.value());
        List<Row> updateProfileEvents = updateProfileEventDS.collectAsList();

        Assertions.assertTrue(updateProfileEvents.size() > 0);

        for (final Row event: updateProfileEvents) {
            Assertions.assertEquals (HiveProfileEnrichedEventTableSchema.spend(event), preSegment);
        }

        final InMemoryUserRepository repo = new InMemoryUserRepository();
        repo.save(
                new User.Builder()
                        .setId(userId)
                        .setAppId(VALUE_APP_A)
                        .setAmountSpentInCents(amount1 + amount2)
                        .setFirstTimeActiveTS(t1)
                        .setLastTimeActiveTS(t2)
                        .setLocation("PK")
                        .setBuild("build_1")
                        .setPlatform("android")
                        .get()
        );

        final long t3 = addDays(origin, -3).getTime();
        final long t4 = addDays(origin, -2).getTime();

        eventTime = new HiveTime(t4);

        final List<Row> events2 = new ArrayList<Row>() {
            {
                add(createPurchaseEvent("1", userId, amount3, "product_a", t3));
                add(createPurchaseEvent("2", userId,amount4, "product_b", t4));
            }
        };

        eventsDS = transformer.transform(spark.createDataset(events2, RowEncoder.apply(schema)), eventTime);
        updateProfileEventDS = eventsDS.filter((FilterFunction<Row>) row -> HiveProfileEnrichedEventTableSchema.eventType(row) == EventType.PROFILE_UPDATED.value());
        updateProfileEvents = updateProfileEventDS.collectAsList();

        Assertions.assertTrue(updateProfileEvents.size() > 0);

        for (final Row event: updateProfileEvents) {
            Assertions.assertEquals (HiveProfileEnrichedEventTableSchema.spend(event), postSegment);
        }
    }

    private void testLifecycleSegment(final Date aOldestEvent, final Date aLatestEvent,
                                      final String aUserId, final boolean firstSession, final Segmentation.AbstractSegment aSegment) throws Exception {
        final long oldestTS = aOldestEvent.getTime();
        final long latestTS = aLatestEvent.getTime();

        HiveTime eventTime = new HiveTime(latestTS);

        final Dataset<Row> eventsDS = createTestDataset(aUserId, oldestTS, latestTS);
        final InMemoryProfileSegmentTransformer transformer = new InMemoryProfileSegmentTransformer();
        final List<Row> segmentedEvents = transformer.transform(eventsDS, eventTime).collectAsList();

        assert (segmentedEvents.size() == (firstSession ? 4 : 3));

        for (final Row event: segmentedEvents) {
            final EventType type = EventType.fromValue(HiveProfileEnrichedEventTableSchema.eventType(event));

            assert (HiveProfileEnrichedEventTableSchema.age(event).equals(aSegment.getTitle()));
            assert (type == EventType.SESSION_STARTED
                    || type == EventType.NEW_USER_CREATED
                    || type == EventType.PROFILE_UPDATED);
        }
    }

    private Dataset<Row> createTestDataset(final String aUserId, long oldestTS, long latestTS) {
        final StructType schema = new HiveSentimentEventTableSchema().schema();
        final HiveTime oldestTime = new HiveTime(oldestTS);
        final HiveTime latestTime = new HiveTime(latestTS);
        final List<Row> events = new ArrayList<Row>(){
            {
                add(new HiveSentimentEventTableSchema.RowBuilder()
                        .setId("1")
                        .setAppId(VALUE_APP_A)
                        .setUserId(aUserId)
                        .setSessionId(VALUE_SESSION_1)
                        .setType(EventType.SESSION_STARTED.value())
                        .setVersion(AbstractDomainEvent.VERSION)
                        .setOccurredOn(oldestTS)
                        .setCountryCode("PK")
                        .setBuild("build_1")
                        .setPlatform("android")
                        .setFields("{}")
                        .setCustom01("A")
                        .setCustom02(null)
                        .setCustom03("!!")
                        .setYear(Integer.toString(oldestTime.year.getValue()))
                        .setMonth(oldestTime.yyyymmm())
                        .setDay(oldestTime.yyyymmmdd())
                        .get());

                add(new HiveSentimentEventTableSchema.RowBuilder()
                        .setId("2")
                        .setAppId(VALUE_APP_A)
                        .setUserId(aUserId)
                        .setSessionId(VALUE_SESSION_1)
                        .setType(EventType.SESSION_STARTED.value())
                        .setVersion(AbstractDomainEvent.VERSION)
                        .setOccurredOn(latestTS)
                        .setCountryCode("PK")
                        .setBuild("build_1")
                        .setPlatform("android")
                        .setFields("{}")
                        .setCustom01("B")
                        .setCustom02("123!")
                        .setCustom03(null)
                        .setYear(Integer.toString(latestTime.year.getValue()))
                        .setMonth(latestTime.yyyymmm())
                        .setDay(latestTime.yyyymmmdd())
                        .get());
            }
        };

        return spark.createDataset(events, RowEncoder.apply(schema));
    }

    private Dataset<Row> createTestDatasetInvalidValues(final String aUserId, long oldestTS, long latestTS) {
        final StructType schema = new HiveSentimentEventTableSchema().schema();
        final HiveTime oldestTime = new HiveTime(oldestTS);
        final HiveTime latestTime = new HiveTime(latestTS);
        final List<Row> events = new ArrayList<Row>(){
            {
                add(new HiveSentimentEventTableSchema.RowBuilder()
                        .setId("1")
                        .setAppId(VALUE_APP_A)
                        .setUserId(aUserId)
                        .setSessionId(VALUE_SESSION_1)
                        .setType(EventType.SESSION_STARTED.value())
                        .setVersion(AbstractDomainEvent.VERSION)
                        .setOccurredOn(oldestTS)
                        .setCountryCode(null)
                        .setBuild(null)
                        .setPlatform(null)
                        .setFields("{}")
                        .setCustom01("A")
                        .setCustom02(null)
                        .setCustom03("!!")
                        .setYear(Integer.toString(oldestTime.year.getValue()))
                        .setMonth(oldestTime.yyyymmm())
                        .setDay(oldestTime.yyyymmmdd())
                        .get());

                add(new HiveSentimentEventTableSchema.RowBuilder()
                        .setId("2")
                        .setAppId(VALUE_APP_A)
                        .setUserId(aUserId)
                        .setSessionId(VALUE_SESSION_1)
                        .setType(EventType.SESSION_STARTED.value())
                        .setVersion(AbstractDomainEvent.VERSION)
                        .setOccurredOn(latestTS)
                        .setCountryCode(null)
                        .setBuild(null)
                        .setPlatform(null)
                        .setFields("{}")
                        .setCustom01("B")
                        .setCustom02("123!")
                        .setCustom03(null)
                        .setYear(Integer.toString(latestTime.year.getValue()))
                        .setMonth(latestTime.yyyymmm())
                        .setDay(latestTime.yyyymmmdd())
                        .get());
            }
        };

        return spark.createDataset(events, RowEncoder.apply(schema));
    }

    private Row createPurchaseEvent(final String id, final String aUserId, long aAmountSpentInCents, final String aProductId, long aOccurredOn) {
        final HiveTime time = new HiveTime(aOccurredOn);
        final JSONObject fields = new JSONObject();
        fields.put(HiveEventFields.PURCHASE_PRODUCT_ID.value(), aProductId);
        fields.put(HiveEventFields.PURCHASE_AMOUNT.value(), aAmountSpentInCents);

        return new HiveSentimentEventTableSchema.RowBuilder()
                .setId(id)
                .setAppId(VALUE_APP_A)
                .setUserId(aUserId)
                .setSessionId(VALUE_SESSION_1)
                .setType(EventType.PRODUCT_PURCHASED.value())
                .setVersion(AbstractDomainEvent.VERSION)
                .setOccurredOn(aOccurredOn)
                .setCountryCode("US")
                .setBuild("build_2")
                .setPlatform("ios")
                .setFields(fields.toString())
                .setYear(Integer.toString(time.year.getValue()))
                .setMonth(time.yyyymmm())
                .setDay(time.yyyymmmdd())
                .get();
    }

    private Row createTextMessageSentEvent(final String aUserId, long aOccurredOn,
                                           final double aSentimentScore) {

        final HiveTime time = new HiveTime(aOccurredOn);
        final JSONObject fields = new JSONObject();
        fields.put(HiveEventFields.TEXT_MESSAGE_CONTENT.value(), "MessageA");
        fields.put(HiveEventFields.TEXT_MESSAGE_CHANNEL.value(), "channel_1");
        fields.put(HiveEventFields.TEXT_MESSAGE_NICK.value(), "abc");
        fields.put(HiveEventFields.SENTIMENT_SCORE.value(), aSentimentScore);

        return new HiveSentimentEventTableSchema.RowBuilder()
                .setId("2")
                .setAppId(VALUE_APP_A)
                .setUserId(aUserId)
                .setSessionId(VALUE_SESSION_1)
                .setType(EventType.TEXT_MESSAGE_SENT.value())
                .setVersion(AbstractDomainEvent.VERSION)
                .setOccurredOn(aOccurredOn)
                .setCountryCode("PK")
                .setBuild("build_1")
                .setPlatform("android")
                .setFields(fields.toString())
                .setCustom01("B")
                .setCustom02("123!")
                .setCustom03(null)
                .setYear(Integer.toString(time.year.getValue()))
                .setMonth(time.yyyymmm())
                .setDay(time.yyyymmmdd())
                .get();
    }

    private Date addDays(final Date time, int days) {
        return new Date(time.getTime() + days * MILLIS_IN_DAY);
    }
}
