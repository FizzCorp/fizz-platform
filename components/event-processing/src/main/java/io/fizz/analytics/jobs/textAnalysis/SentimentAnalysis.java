package io.fizz.analytics.jobs.textAnalysis;

import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.*;
import io.fizz.analytics.common.AbstractTransformer;
import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.nlp.AbstractTextAnalysisService;
import io.fizz.analytics.common.nlp.NLPCircuitBreaker;
import io.fizz.analytics.common.nlp.NLPCircuitBreakerBuilder;
import io.fizz.analytics.common.source.hive.*;
import io.fizz.analytics.domain.Keyword;
import io.fizz.analytics.domain.TextAnalysisResult;
import io.fizz.common.ConfigService;
import org.apache.spark.api.java.function.MapPartitionsFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.json.JSONArray;
import org.json.JSONObject;
import scala.Serializable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SentimentAnalysis implements AbstractTransformer<Row,Row>, Serializable {
    private static final int BATCH_SIZE = ConfigService.instance().getNumber("nlu.rpc.batch.size").intValue();

    static final String KEY_SCORE = HiveDefines.SENTIMENT_FIELDS.SCORE;
    static final String KEY_TEXT = HiveDefines.SENTIMENT_FIELDS.TEXT;
    static final String KEY_KEYWORDS = HiveDefines.SENTIMENT_FIELDS.KEYWORDS;
    static final String COL_OUTPUT = "sentiment";

    final private AbstractTextAnalysisService nlpService;

    SentimentAnalysis(final SparkSession aSpark, final AbstractTextAnalysisService aNLPService) {
        if (Objects.isNull(aSpark)) {
            throw new IllegalArgumentException("invalid spark session specified.");
        }
        if (Objects.isNull(aNLPService)) {
            throw new IllegalArgumentException("invalid NLP service specified.");
        }

        nlpService = aNLPService;
    }

    @Override
    public Dataset<Row> transform(Dataset<Row> aSourceDS, HiveTime aTime) {
        if (Objects.isNull(aSourceDS)) {
            throw new IllegalArgumentException("invalid source data set");
        }

        return aSourceDS.mapPartitions((MapPartitionsFunction<Row, Row>) iterator -> {
            final List<Row> inputRows = new ArrayList<>();
            final List<String> inputTexts = new ArrayList<>();

            while (iterator.hasNext()) {
                Row row = iterator.next();
                inputRows.add(row);
                inputTexts.add(extractContent(row));
            }
            final List<String> analyzedTexts = analyzeText(inputTexts);

            return transformToAnalyzeText(inputRows, analyzedTexts).iterator();
        }, RowEncoder.apply(new HiveAnalyzeTextEventTableSchema().schema()));
    }

    private String extractContent(Row row) {
        final JSONObject fields = new JSONObject(HiveProfileEnrichedEventTableSchema.fields(row));
        return fields.getString(HiveEventFields.TEXT_MESSAGE_CONTENT.value());
    }

    private List<String> analyzeText(final List<String> aTexts) throws ExecutionException, InterruptedException {
        final List<String> result = new ArrayList<>();
        final Queue<String> textQueue = new LinkedList<>(aTexts);

        while (!textQueue.isEmpty()) {
            final List<CompletableFuture<String>> futures = new ArrayList<>();

            for (int i = 0; i < BATCH_SIZE && !textQueue.isEmpty(); i++) {
                String text = textQueue.poll();
                final CompletableFuture<String> analyzedText = analyzeText(text);
                futures.add(analyzedText);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).get();
            for (final CompletableFuture<String> future: futures) {
                result.add(future.get());
            }
        }

        return result;
    }

    private CompletableFuture<String> analyzeText(final String aText) {
        NLPCircuitBreaker circuitBreaker = new NLPCircuitBreakerBuilder(nlpService).build();
        final CompletableFuture<String> future = new CompletableFuture<>();

        CompletableFuture<TextAnalysisResult> analyzedFuture = circuitBreaker.text(aText).enqueue();
        analyzedFuture.handle((TextAnalysisResult result, Throwable ex) -> {
            if (Objects.isNull(result)) {
                return future.complete(defaultResponse());
            }
            if (Objects.isNull(result.getSentiment())) {
                return future.complete(defaultResponse());
            }

            final JSONObject data = new JSONObject();
            data.put(KEY_SCORE, result.getSentiment().getScore());
            final JSONArray keywords = new JSONArray();
            if (!Objects.isNull(result.getKeywords())) {
                for (final Keyword keyword : result.getKeywords()) {
                    final String text = keyword.getText();
                    final Double score = keyword.getSentiment().getScore();
                    keywords.put(new HashMap<String, Object>() {
                        {
                            put(KEY_TEXT, text);
                            put(KEY_SCORE, score);
                        }
                    });
                }
            }
            data.put(KEY_KEYWORDS, keywords);
            return future.complete(data.toString());
        });
        return future;
    }

    private List<Row> transformToAnalyzeText(List<Row> rows, List<String> analyzeTexts) {
        if (rows.size() != analyzeTexts.size()) {
            throw new IllegalArgumentException("invalid data set provided");
        }

        List<Row> transformedRows = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            transformedRows.add(transformToAnalyzeText(rows.get(i), analyzeTexts.get(i)));
        }
        return transformedRows;
    }

    private Row transformToAnalyzeText(Row row, String analyzeText) {
        return new HiveAnalyzeTextEventTableSchema.RowBuilder()
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
                .setFields(HiveRawEventTableSchema.fields(row))
                .setSentiment(analyzeText)
                .setYear(HiveRawEventTableSchema.year(row))
                .setMonth(HiveRawEventTableSchema.month(row))
                .setDay(HiveRawEventTableSchema.day(row))
                .get();
    }

    public static String defaultResponse() {
        final JSONObject data = new JSONObject();
        data.put(KEY_SCORE, 0);
        data.put(KEY_KEYWORDS, new JSONArray());
        return data.toString();
    }
}
