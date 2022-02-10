package io.fizz.analytics.common;

import io.fizz.common.ConfigService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ConfigServiceTest {
    @Test
    @DisplayName("it should get test data")
    void testDataTest() {
        final ConfigService service = ConfigService.instance();

        assert (service.getString("test.string").equals("test string"));
        assert (service.getNumber("test.number").intValue() == 123);
        assert (!service.getBoolean("test.boolean"));
    }
}
