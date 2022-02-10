package io.fizz.logger.domain;

import io.fizz.common.Utils;

public class LogId {
    private final String value;

    public LogId(final String aValue) {
        Utils.assertRequiredArgument(aValue, "invalid_log_id");
        Utils.assertRequiredArgumentLength(aValue, 128, "invalid_log_id");
        Utils.assertRequiedArgumentMatches(aValue, "[A-Za-z0-9_-]+", "invalid_log_id");

        value = aValue;
    }

    public String value() {
        return value;
    }
}
