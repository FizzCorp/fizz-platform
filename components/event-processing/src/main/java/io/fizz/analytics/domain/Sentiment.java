package io.fizz.analytics.domain;

import io.fizz.common.Utils;

public class Sentiment {

    private final double score;

    public Sentiment(final double aScore) {
        Utils.assertArgumentRange(aScore, -1, 1, "invalid_sentiment");
        score = aScore;
    }

    public double getScore() {
        return score;
    }
}
