package io.fizz.analytics.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SentimentTest {

    @Test
    @DisplayName("it should create Sentiment object with valid params")
    void validTestSentiment() {
        try {
            Sentiment sentiment1 = new Sentiment(-1.0);
            Assertions.assertNotNull(sentiment1);
            Assertions.assertEquals(-1.0, sentiment1.getScore());

            Sentiment sentiment2 = new Sentiment(0.0);
            Assertions.assertNotNull(sentiment2);
            Assertions.assertEquals(0.0, sentiment2.getScore());

            Sentiment sentiment3 = new Sentiment(1.0);
            Assertions.assertNotNull(sentiment3);
            Assertions.assertEquals(1.0, sentiment3.getScore());
        } catch (IllegalArgumentException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    @DisplayName("it should not create Sentiment with invalid range")
    void invalidTestSentimentInvalidRange1() {
        try {
            new Sentiment(-1.1);
            Assertions.fail("Invalid Sentiment object should not be created!");
        } catch (IllegalArgumentException ex) {
            Assertions.assertEquals("invalid_sentiment", ex.getMessage());
        }
    }

    @Test
    @DisplayName("it should not create Sentiment with invalid range")
    void invalidTestSentimentInvalidRange2() {
        try {
            new Sentiment(1.1);
            Assertions.fail("Invalid Sentiment object should not be created!");
        } catch (IllegalArgumentException ex) {
            Assertions.assertEquals("invalid_sentiment", ex.getMessage());
        }
    }
}
