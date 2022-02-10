package io.fizz.analytics.common.opentsdb;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TSDBAPIErrorExceptionTest {
    @Test
    @DisplayName("it should store valid information")
    void basicValidityTest() {
        int statusCode = 403;
        String message = "test exception";
        final TSDBAPIErrorException ex = new TSDBAPIErrorException(statusCode, message);

        assert (ex.getMessage().equals(message));
        assert (ex.getStatusCode() == statusCode);
    }
}
