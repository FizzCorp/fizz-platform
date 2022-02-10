package io.fizz.analytics.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class KeywordTest {

    @Test
    @DisplayName("it should create Keyword object with valid params")
    void validTestKeyword() {
        try {
            String text = "test";
            Sentiment sentiment = new Sentiment(0.53);
            Keyword keyword = new Keyword(text, sentiment);
            Assertions.assertNotNull(keyword);
            Assertions.assertEquals(text, keyword.getText());
            Assertions.assertEquals(sentiment, keyword.getSentiment());
            Assertions.assertEquals(sentiment.getScore(), keyword.getSentiment().getScore());
        } catch (IllegalArgumentException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    @DisplayName("it should not create Keyword object with null text")
    void invalidTestKeywordNullText() {
        try {
            new Keyword(null, new Sentiment(0.25));
            Assertions.fail("Keyword should not be created with null text!");
        } catch (IllegalArgumentException ex) {
            Assertions.assertEquals("invalid_text", ex.getMessage());
        }
    }

    @Test
    @DisplayName("it should not create Keyword object with invalid text length")
    void invalidTestKeywordInvalidText() {
        try {
            new Keyword("", new Sentiment(0.25));
            Assertions.fail("Keyword should not be created with invalid text!");
        } catch (IllegalArgumentException ex) {
            Assertions.assertEquals("invalid_text", ex.getMessage());
        }
    }

    @Test
    @DisplayName("it should not create Keyword object with null sentiment")
    void invalidTestKeywordNullSentiment() {
        try {
            new Keyword("test", null);
            Assertions.fail("Keyword should not be created with null sentiment!");
        } catch (IllegalArgumentException ex) {
            Assertions.assertEquals("invalid_sentiment", ex.getMessage());
        }
    }
}
