package io.fizz.analytics.domain;

import io.fizz.common.Utils;

public class Keyword {

    private static int MAX_MSG_CONTENT = 2048;

    private final String text;
    private final Sentiment sentiment;

    public Keyword(final String aText, final Sentiment aSentiment) {
        Utils.assertRequiredArgumentLength(aText, 1, MAX_MSG_CONTENT, "invalid_text");
        Utils.assertRequiredArgument(aSentiment, "invalid_sentiment");

        text = aText;
        sentiment = aSentiment;
    }

    public String getText() {
        return text;
    }

    public Sentiment getSentiment() {
        return sentiment;
    }
}
