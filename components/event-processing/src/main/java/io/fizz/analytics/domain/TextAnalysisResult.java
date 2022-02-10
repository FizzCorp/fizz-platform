package io.fizz.analytics.domain;

import io.fizz.common.Utils;

import java.util.List;

public class TextAnalysisResult {
    private final Sentiment sentiment;
    private final List<Keyword> keywords;

    public TextAnalysisResult(final Sentiment aSentiment, final List<Keyword> aKeywords) {
        Utils.assertRequiredArgument(aSentiment, "invalid_sentiment");
        Utils.assertRequiredArgument(aKeywords, "invalid_keywords");

        sentiment = aSentiment;
        keywords = aKeywords;
    }

    public Sentiment getSentiment() {
        return sentiment;
    }

    public List<Keyword> getKeywords() {
        return keywords;
    }
}
