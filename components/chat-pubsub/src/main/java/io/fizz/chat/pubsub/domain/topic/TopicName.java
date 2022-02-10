package io.fizz.chat.pubsub.domain.topic;

import io.fizz.common.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TopicName {
    private static final int MIN_LEN = 1;
    private static final int MAX_LEN = 150;
    private static final int MAX_LEVELS = 4;
    private static final Pattern INVALID_SYMBOLS = Pattern.compile("[#/\\u0000+]");

    private final List<String> levels = new ArrayList<>();

    public TopicName append(final String aLevel) {
        Utils.assertRequiredArgumentLength(aLevel, MIN_LEN, MAX_LEN, "invalid_topic_level");

        if (INVALID_SYMBOLS.matcher(aLevel).find()) {
            throw new IllegalArgumentException("invalid_topic_level");
        }

        if (levels.size() >= MAX_LEVELS) {
            throw new IllegalArgumentException("max_topic_levels_reached");
        }

        levels.add(aLevel);

        return this;
    }

    public String value(final char aDelimiter) {
        final StringBuilder builder = new StringBuilder();
        for (String level: levels) {
            if (builder.length() > 0) {
                builder.append(aDelimiter);
            }
            builder.append(level);
        }

        return builder.toString();
    }
}
