package io.fizz.analytics.jobs.textAnalysis;

import io.fizz.analytics.AbstractSparkTest;
import io.fizz.analytics.common.source.hive.HiveKeywordsTableSchema;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

public class AnalyzedMessagesToKeywordsTest extends AbstractSparkTest {
    @Test
    @DisplayName("it should transform scored messages to keyword table rows")
    void transformScoredMessages() {
        final Dataset<Row> scoredMessagesDS = new MockAnalyzedMessagesStore(spark, true).scan();
        final ScoredMsgsToKeywords transformer = new ScoredMsgsToKeywords();
        final Dataset<Row> keywordsDS = transformer.transform(scoredMessagesDS, null);
        final List<Row> keywords = keywordsDS.collectAsList();

        for (final Row keyword: keywords) {
            assert (HiveKeywordsTableSchema.keyword(keyword).endsWith("message_1"));
            assert (HiveKeywordsTableSchema.sentimentScore(keyword) == MockAnalyzedMessagesStore.SENTIMENT_SCORE);
            assert (HiveKeywordsTableSchema.year(keyword).equals("2018"));
            assert (HiveKeywordsTableSchema.month(keyword).equals("5"));
            assert (HiveKeywordsTableSchema.day(keyword).equals("8"));
        }
    }
}
