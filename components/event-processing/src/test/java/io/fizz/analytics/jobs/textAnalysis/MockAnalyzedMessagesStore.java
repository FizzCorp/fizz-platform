package io.fizz.analytics.jobs.textAnalysis;

import io.fizz.analytics.common.source.hive.HiveProfileEnrichedEventTableSchema;
import io.fizz.analytics.common.source.hive.HiveRawEventTableSchema;
import io.fizz.common.domain.EventType;
import io.fizz.common.domain.events.AbstractDomainEvent;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.StructType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class MockAnalyzedMessagesStore {
    static final double SENTIMENT_SCORE = 0.5;
    static final long MESSAGE_TS = 1525794897000L;

    private final SparkSession spark;
    private final boolean mockSentiment;

    MockAnalyzedMessagesStore(final SparkSession aSpark, final boolean aMockSentiment) {
        spark = aSpark;
        mockSentiment = aMockSentiment;
    }

    Dataset<Row> scan() {
        final StructType schema = new HiveRawEventTableSchema().schema();

        final List<Row> rows = new ArrayList<Row>() {
            {
                add(new HiveRawEventTableSchema.RowBuilder()
                        .setId("1").setAppId("appA")
                        .setUserId("userA")
                        .setSessionId("session_1")
                        .setType(EventType.TEXT_MESSAGE_SENT.value())
                        .setVersion(AbstractDomainEvent.VERSION)
                        .setOccurredOn(MESSAGE_TS)
                        .setPlatform("ios")
                        .setVersion(1)
                        .setBuild("build_1")
                        .setCustom01("A").setCustom02("B").setCustom03("C")
                        .setFields("{\"content\":\"message_1\",\"nick\":\"actorA\",\"channel\":\"channelA\"}")
                        .setYear("2018")
                        .setMonth("5")
                        .setDay("8")
                        .get()
                );
                add(new HiveRawEventTableSchema.RowBuilder()
                        .setId("2").setAppId("appA")
                        .setUserId("userB")
                        .setSessionId("session_2")
                        .setType(EventType.TEXT_MESSAGE_SENT.value())
                        .setVersion(AbstractDomainEvent.VERSION)
                        .setOccurredOn(MESSAGE_TS)
                        .setPlatform("android")
                        .setVersion(1)
                        .setBuild("build_2")
                        .setCustom01("D").setCustom02("E").setCustom03("F")
                        .setFields("{\"content\":\"message_2\",\"nick\":\"actorB\",\"channel\":\"channelB\"}")
                        .setYear("2018")
                        .setMonth("5")
                        .setDay("8")
                        .get()
                );
            }
        };

        final Dataset<Row> messagesDS = spark.createDataset(rows, RowEncoder.apply(schema));
        if (!mockSentiment) {
            return messagesDS;
        }

        final JSONObject sentiment = new JSONObject();
        sentiment.put(SentimentAnalysis.KEY_SCORE, SENTIMENT_SCORE);
        final JSONArray keywords = new JSONArray();
        keywords.put(new HashMap<String,Object>(){
            {
                put(SentimentAnalysis.KEY_TEXT, "message_1");
                put(SentimentAnalysis.KEY_SCORE, SENTIMENT_SCORE);
            }
        });
        sentiment.put(SentimentAnalysis.KEY_KEYWORDS, keywords);

        return messagesDS.withColumn(SentimentAnalysis.COL_OUTPUT, functions.lit(sentiment.toString()));
    }
}
