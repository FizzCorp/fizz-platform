package io.fizz.chat.domain.topic;

import io.fizz.common.domain.ApplicationId;

import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.regex.Pattern;

public class TopicId {
    private static final int MIN_LEN = 1;
    static final int MAX_LEN = 150;
    private static final String RESERVED_SPACE = "fizz";
    private static final Pattern TOPIC_WILDCARDS = Pattern.compile("[#\\u0000+]");

    public static final IllegalArgumentException ERROR_INVALID_TOPIC_ID = new IllegalArgumentException("invalid_topic_id");

    private final String value;
    public TopicId(final String aValue) {
        if (Objects.isNull(aValue) || TOPIC_WILDCARDS.matcher(aValue).find()) {
            throw ERROR_INVALID_TOPIC_ID;
        }

        try {
            final String trimmed = aValue.trim();
            int len = trimmed.getBytes("UTF-8").length;
            if (len < MIN_LEN || len > MAX_LEN) {
                throw ERROR_INVALID_TOPIC_ID;
            }

            if (trimmed.startsWith(RESERVED_SPACE)) {
                throw ERROR_INVALID_TOPIC_ID;
            }

            value = trimmed;
        }
        catch (UnsupportedEncodingException ex) {
            throw ERROR_INVALID_TOPIC_ID;
        }
    }

    public String value() {
        return value;
    }

    public String qualifiedValue(final ApplicationId aAppId) {
        return aAppId.value() + value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TopicId topicId = (TopicId) o;
        return Objects.equals(value, topicId.value);
    }

    @Override
    public int hashCode() {

        return Objects.hash(value);
    }
}
