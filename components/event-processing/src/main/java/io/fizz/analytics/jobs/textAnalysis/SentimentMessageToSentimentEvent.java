package io.fizz.analytics.jobs.textAnalysis;

import io.fizz.analytics.common.AbstractTransformer;
import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.source.hive.*;
import io.fizz.common.domain.EventType;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

public class SentimentMessageToSentimentEvent implements AbstractTransformer<Row,Row> {
    @Override
    public Dataset<Row> transform(Dataset<Row> aSourceDS, HiveTime time) {
        if (Objects.isNull(aSourceDS)) {
            throw new IllegalArgumentException("invalid source data set");
        }

        return aSourceDS
                .map((MapFunction<Row, Row>) row -> {
                    final JSONObject outputFields = new JSONObject(HiveRawEventTableSchema.fields(row));

                    if (HiveRawEventTableSchema.eventType(row) == EventType.TEXT_MESSAGE_SENT.value()) {
                        final String analysis = HiveAnalyzeTextEventTableSchema.sentiment(row);
                        final JSONObject analysisData = Objects.isNull(analysis) ? null : new JSONObject(analysis);

                        outputFields.put(HiveEventFields.SENTIMENT_SCORE.value(), parseSentimentScore(analysisData));
                        outputFields.put(HiveEventFields.TEXT_MESSAGE_KEYWORDS.value(), parseKeywords(analysisData));
                    }

                    return new HiveSentimentEventTableSchema.RowBuilder()
                            .setId(HiveRawEventTableSchema.id(row))
                            .setAppId(HiveRawEventTableSchema.appId(row))
                            .setCountryCode(HiveRawEventTableSchema.countryCode(row))
                            .setUserId(HiveRawEventTableSchema.userId(row))
                            .setSessionId(HiveRawEventTableSchema.sessionId(row))
                            .setType(HiveRawEventTableSchema.eventType(row))
                            .setVersion(HiveRawEventTableSchema.version(row))
                            .setOccurredOn(HiveRawEventTableSchema.occurredOn(row))
                            .setPlatform(HiveRawEventTableSchema.platform(row))
                            .setBuild(HiveRawEventTableSchema.build(row))
                            .setCustom01(HiveRawEventTableSchema.custom01(row))
                            .setCustom02(HiveRawEventTableSchema.custom02(row))
                            .setCustom03(HiveRawEventTableSchema.custom03(row))
                            .setFields(outputFields.toString())
                            .setYear(HiveRawEventTableSchema.year(row))
                            .setMonth(HiveRawEventTableSchema.month(row))
                            .setDay(HiveRawEventTableSchema.day(row))
                            .get();
                }, RowEncoder.apply(new HiveSentimentEventTableSchema().schema()));
    }

    private static double parseSentimentScore(final JSONObject aAnalysisData) {
        return Objects.isNull(aAnalysisData) ? 0.0 : aAnalysisData.getDouble(SentimentAnalysis.KEY_SCORE);
    }

    private static JSONArray parseKeywords(final JSONObject aAnalysisData) {
        if (Objects.isNull(aAnalysisData) || !aAnalysisData.has(SentimentAnalysis.KEY_KEYWORDS)) {
            return new JSONArray();
        }

        final JSONArray keywords = aAnalysisData.getJSONArray(HiveDefines.SENTIMENT_FIELDS.KEYWORDS);
        final JSONArray outKeywords = new JSONArray();
        for (int ki = 0; ki < keywords.length(); ki++) {
            final JSONObject obj = keywords.getJSONObject(ki);
            outKeywords.put(obj.getString(SentimentAnalysis.KEY_TEXT).toLowerCase());
        }

        return outKeywords;
    }
}
