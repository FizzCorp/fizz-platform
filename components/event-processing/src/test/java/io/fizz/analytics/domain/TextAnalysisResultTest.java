package io.fizz.analytics.domain;

import org.apache.hadoop.yarn.webapp.hamlet.Hamlet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class TextAnalysisResultTest {

    @Test
    @DisplayName("it should create TextAnalysisResult object with valid params")
    void validTestTextAnalysisResult() {
        try {

            Sentiment sentiment = new Sentiment(0.53);
            List<Keyword> keywords = new ArrayList<>();

            String keywordText1 = "text1";
            Sentiment keywordSentiment1 = new Sentiment(0.92);
            Keyword keyword1 = new Keyword(keywordText1, keywordSentiment1);

            String keywordText2 = "text2";
            Sentiment keywordSentiment2 = new Sentiment(0.42);
            Keyword keyword2 = new Keyword(keywordText2, keywordSentiment2);

            keywords.add(keyword1);
            keywords.add(keyword2);

            TextAnalysisResult analysisResult = new TextAnalysisResult(sentiment, keywords);
            Assertions.assertNotNull(analysisResult);
            Assertions.assertEquals(sentiment, analysisResult.getSentiment());
            Assertions.assertEquals(sentiment.getScore(), analysisResult.getSentiment().getScore());

            Assertions.assertEquals(keywords, analysisResult.getKeywords());
            Assertions.assertEquals(keywords.size(), analysisResult.getKeywords().size());

            Assertions.assertEquals(keyword1, analysisResult.getKeywords().get(0));
            Assertions.assertEquals(keywordText1, analysisResult.getKeywords().get(0).getText());
            Assertions.assertEquals(keywordSentiment1, analysisResult.getKeywords().get(0).getSentiment());
            Assertions.assertEquals(keywordSentiment1.getScore(), analysisResult.getKeywords().get(0).getSentiment().getScore());

            Assertions.assertEquals(keyword2, analysisResult.getKeywords().get(1));
            Assertions.assertEquals(keywordText2, analysisResult.getKeywords().get(1).getText());
            Assertions.assertEquals(keywordSentiment2, analysisResult.getKeywords().get(1).getSentiment());
            Assertions.assertEquals(keywordSentiment2.getScore(), analysisResult.getKeywords().get(1).getSentiment().getScore());
        } catch (IllegalArgumentException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    @DisplayName("it should not create TextAnalysisResult object with null sentiment")
    void invalidTestTextAnalysisResultNullSentiment() {
        try {
            new TextAnalysisResult(null, new ArrayList<>());
            Assertions.fail("TextAnalysisResult should not be created with null sentiment!");
        } catch (IllegalArgumentException ex) {
            Assertions.assertEquals("invalid_sentiment", ex.getMessage());
        }
    }

    @Test
    @DisplayName("it should not create TextAnalysisResult object with null keywords")
    void invalidTestTextAnalysisResultNullKeywords() {
        try {
            new TextAnalysisResult(new Sentiment(0.62), null);
            Assertions.fail("TextAnalysisResult should not be created with null keywords!");
        } catch (IllegalArgumentException ex) {
            Assertions.assertEquals("invalid_keywords", ex.getMessage());
        }
    }

}
