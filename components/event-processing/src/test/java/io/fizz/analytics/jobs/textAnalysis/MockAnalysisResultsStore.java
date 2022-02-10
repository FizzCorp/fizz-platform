package io.fizz.analytics.jobs.textAnalysis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalysisResults;
import io.fizz.analytics.domain.Keyword;
import io.fizz.analytics.domain.Sentiment;
import io.fizz.analytics.domain.TextAnalysisResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MockAnalysisResultsStore implements Serializable {
    private static final double SENTIMENT_SCORE = 0.5;

    public TextAnalysisResult testDataFactory() {
        List<Keyword> keywords = new ArrayList<>();
        keywords.add(createKeyword("message_1"));
        keywords.add(createKeyword("message_2"));

        Sentiment sentiment = new Sentiment(SENTIMENT_SCORE);
        return new TextAnalysisResult(sentiment, keywords);
    }

    public TextAnalysisResult delayedTestDataFactory(int aWait) {
        try {
            Thread.sleep(aWait);
        } catch (InterruptedException ignored) {}
        return testDataFactory();
    }

    private Keyword createKeyword(final String text) {
        return new Keyword(text, new Sentiment(SENTIMENT_SCORE));
    }
}
