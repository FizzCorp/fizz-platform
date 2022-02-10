package io.fizz.common;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.List;

public class LoggingServiceTest {

    @Test
    void testLogLevelDefault() {
        final MockLoggingService.MockLog logger = MockLoggingService.getLogger(LoggingServiceTest.class);

        logger.trace("trace");
        logger.debug("debug");
        logger.info("info");
        logger.warn("warn");
        logger.error("error");
        logger.fatal("fatal");

        List<String> logStream = logger.getLogStream();

        Assert.assertEquals(5, logStream.size());
        Assert.assertEquals("<time> LoggingServiceTest [DEBUG]: debug", logStream.get(0));
        Assert.assertEquals("<time> LoggingServiceTest [INFO]: info", logStream.get(1));
        Assert.assertEquals("<time> LoggingServiceTest [WARN]: warn", logStream.get(2));
        Assert.assertEquals("<time> LoggingServiceTest [ERROR]: error", logStream.get(3));
        Assert.assertEquals("<time> LoggingServiceTest [FATAL]: fatal", logStream.get(4));
    }

    @Test
    void testLogLevelTrace() {
        final MockLoggingService.MockLog logger = MockLoggingService.getLogger(LoggingServiceTest.class);
        logger.setLogLevel("trace");

        logger.trace("trace");
        logger.debug("debug");
        logger.info("info");
        logger.warn("warn");
        logger.error("error");
        logger.fatal("fatal");

        List<String> logStream = logger.getLogStream();

        Assert.assertEquals(6, logStream.size());
        Assert.assertEquals("<time> LoggingServiceTest [TRACE]: trace", logStream.get(0));
        Assert.assertEquals("<time> LoggingServiceTest [DEBUG]: debug", logStream.get(1));
        Assert.assertEquals("<time> LoggingServiceTest [INFO]: info", logStream.get(2));
        Assert.assertEquals("<time> LoggingServiceTest [WARN]: warn", logStream.get(3));
        Assert.assertEquals("<time> LoggingServiceTest [ERROR]: error", logStream.get(4));
        Assert.assertEquals("<time> LoggingServiceTest [FATAL]: fatal", logStream.get(5));
    }

    @Test
    void testLogLevelDebug() {
        final MockLoggingService.MockLog logger = MockLoggingService.getLogger(LoggingServiceTest.class);
        logger.setLogLevel("DEBUG");

        logger.trace("trace");
        logger.debug("debug");
        logger.info("info");
        logger.warn("warn");
        logger.error("error");
        logger.fatal("fatal");

        List<String> logStream = logger.getLogStream();

        Assert.assertEquals(5, logStream.size());
        Assert.assertEquals("<time> LoggingServiceTest [DEBUG]: debug", logStream.get(0));
        Assert.assertEquals("<time> LoggingServiceTest [INFO]: info", logStream.get(1));
        Assert.assertEquals("<time> LoggingServiceTest [WARN]: warn", logStream.get(2));
        Assert.assertEquals("<time> LoggingServiceTest [ERROR]: error", logStream.get(3));
        Assert.assertEquals("<time> LoggingServiceTest [FATAL]: fatal", logStream.get(4));
    }

    @Test
    void testLogLevelInfo() {
        final MockLoggingService.MockLog logger = MockLoggingService.getLogger(LoggingServiceTest.class);
        logger.setLogLevel("INFO");

        logger.trace("trace");
        logger.debug("debug");
        logger.info("info");
        logger.warn("warn");
        logger.error("error");
        logger.fatal("fatal");

        List<String> logStream = logger.getLogStream();

        Assert.assertEquals(4, logStream.size());
        Assert.assertEquals("<time> LoggingServiceTest [INFO]: info", logStream.get(0));
        Assert.assertEquals("<time> LoggingServiceTest [WARN]: warn", logStream.get(1));
        Assert.assertEquals("<time> LoggingServiceTest [ERROR]: error", logStream.get(2));
        Assert.assertEquals("<time> LoggingServiceTest [FATAL]: fatal", logStream.get(3));
    }

    @Test
    void testLogLevelWarn() {
        final MockLoggingService.MockLog logger = MockLoggingService.getLogger(LoggingServiceTest.class);
        logger.setLogLevel("WARN");

        logger.trace("trace");
        logger.debug("debug");
        logger.info("info");
        logger.warn("warn");
        logger.error("error");
        logger.fatal("fatal");

        List<String> logStream = logger.getLogStream();

        Assert.assertEquals(3, logStream.size());
        Assert.assertEquals("<time> LoggingServiceTest [WARN]: warn", logStream.get(0));
        Assert.assertEquals("<time> LoggingServiceTest [ERROR]: error", logStream.get(1));
        Assert.assertEquals("<time> LoggingServiceTest [FATAL]: fatal", logStream.get(2));
    }

    @Test
    void testLogLevelError() {
        final MockLoggingService.MockLog logger = MockLoggingService.getLogger(LoggingServiceTest.class);
        logger.setLogLevel("ERROR");

        logger.trace("trace");
        logger.debug("debug");
        logger.info("info");
        logger.warn("warn");
        logger.error("error");
        logger.fatal("fatal");

        List<String> logStream = logger.getLogStream();

        Assert.assertEquals(2, logStream.size());
        Assert.assertEquals("<time> LoggingServiceTest [ERROR]: error", logStream.get(0));
        Assert.assertEquals("<time> LoggingServiceTest [FATAL]: fatal", logStream.get(1));
    }

    @Test
    void testLogLevelFatal() {
        final MockLoggingService.MockLog logger = MockLoggingService.getLogger(LoggingServiceTest.class);
        logger.setLogLevel("FATAL");

        logger.trace("trace");
        logger.debug("debug");
        logger.info("info");
        logger.warn("warn");
        logger.error("error");
        logger.fatal("fatal");

        List<String> logStream = logger.getLogStream();

        Assert.assertEquals(1, logStream.size());
        Assert.assertEquals("<time> LoggingServiceTest [FATAL]: fatal", logStream.get(0));
    }
}
