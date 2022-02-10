package io.fizz.logger.domain;

import io.fizz.common.Utils;
import io.fizz.logger.infrastructure.ConfigService;

import java.util.Objects;

public class LogCount {
    private static int maxReadCount = ConfigService.config().getNumber("logger.max.read.count").intValue();
    private final Integer value;

    public LogCount(Integer aValue) {
        Utils.assertArgumentRange(aValue, 1, maxReadCount, "invalid_count");
        value = Objects.isNull(aValue) ? maxReadCount : aValue;
    }

    public int value() {
        return value;
    }
}
