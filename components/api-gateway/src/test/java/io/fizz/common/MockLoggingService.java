package io.fizz.common;

import java.util.ArrayList;
import java.util.List;

public class MockLoggingService extends LoggingService {
    static class MockLog extends LoggingService.Log {
        private List<String> logStream = new ArrayList<>();

        MockLog(Class<?> clazz) {
            super(clazz);
        }

        @Override
        void write(final String message) {
            logStream.add(message);
        }

        @Override
        String getTimestamp() {
            return "<time>";
        }

        List<String> getLogStream() {
            return logStream;
        }
    }

    public static MockLog getLogger(Class<?> clazz) {
        return new MockLog(clazz);
    }
}
