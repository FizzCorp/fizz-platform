package io.fizz.analytics.jobs.textAnalysis;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.AmazonComprehendClientBuilder;
import com.amazonaws.services.comprehend.model.DetectKeyPhrasesRequest;
import com.amazonaws.services.comprehend.model.DetectKeyPhrasesResult;
import com.amazonaws.services.comprehend.model.DetectSentimentRequest;
import com.amazonaws.services.comprehend.model.DetectSentimentResult;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.*;
import io.fizz.analytics.AbstractSparkTest;
import io.fizz.analytics.common.nlp.AbstractTextAnalysisService;
import io.fizz.analytics.domain.TextAnalysisResult;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

class SentimentAnalysisTest extends AbstractSparkTest {
    private static class MockNLPService implements AbstractTextAnalysisService, Serializable {
        @Override
        public CompletableFuture<TextAnalysisResult> analyze(String aText) {
            return CompletableFuture.completedFuture(new MockAnalysisResultsStore().testDataFactory());
        }
    }

    @Test
    @DisplayName("it should score messages")
    void testMessageScoring() {
        final MockAnalyzedMessagesStore store = new MockAnalyzedMessagesStore(spark, false);
        final Dataset<Row> messagesDS = store.scan();
        final SentimentAnalysis analysis = new SentimentAnalysis(spark, new MockNLPService());
        final Dataset<Row> scoredMsgsDS = analysis.transform(messagesDS, null);
        final List<Row> scoredMessages = scoredMsgsDS.collectAsList();

        for (final Row row: scoredMessages) {
            final String sentimentField = row.getString(row.fieldIndex(SentimentAnalysis.COL_OUTPUT));
            final JSONObject sentiment = new JSONObject(sentimentField);
            assert (sentiment.getDouble(SentimentAnalysis.KEY_SCORE) == MockAnalyzedMessagesStore.SENTIMENT_SCORE);

            final JSONArray keywords = sentiment.getJSONArray(SentimentAnalysis.KEY_KEYWORDS);

            final JSONObject keyword1 = keywords.getJSONObject(0);
            assert (keyword1.getString(SentimentAnalysis.KEY_TEXT).equals("message_1"));
            assert (keyword1.getDouble(SentimentAnalysis.KEY_SCORE) == MockAnalyzedMessagesStore.SENTIMENT_SCORE);

            final JSONObject keyword2 = keywords.getJSONObject(1);
            assert (keyword2.getString(SentimentAnalysis.KEY_TEXT).equals("message_2"));
            assert (keyword2.getDouble(SentimentAnalysis.KEY_SCORE) == MockAnalyzedMessagesStore.SENTIMENT_SCORE);
        }
    }
}
