package io.fizz.common;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import io.fizz.common.infastructure.ConfigService;

public class LoggingService {
    private enum LogLevel {
        TRACE(1),
        DEBUG(2),
        INFO(3),
        WARN(4),
        ERROR(5),
        FATAL(6);

        private final int value;
        LogLevel(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private static final String LOG_LEVEL = ConfigService.config().getString("log.level");
    private static final DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");
    static public class Log implements Serializable {
        final String name;
        private LogLevel logLevel;

        public Log(Class<?> clazz) {
            name = clazz.getSimpleName();
            setLogLevel(LOG_LEVEL);
        }

        void setLogLevel(String aLogLevel) {
            logLevel = Arrays.stream(LogLevel.values())
                    .filter(e -> e.name().equalsIgnoreCase(aLogLevel)).findAny().orElse(null);
        }

        public void trace(Exception ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            trace(sw.toString());
        }

        public void trace(String message) {
            if (logLevel.compareTo(LogLevel.TRACE) <= 0) {
                print("TRACE", message);
            }
        }

        public void debug(Object message) {
            if (logLevel.compareTo(LogLevel.DEBUG) <= 0) {
                print("DEBUG", message);
            }
        }

        public void info(Object message) {
            if (logLevel.compareTo(LogLevel.INFO) <= 0) {
                print("INFO", message);
            }
        }

        public void warn(Object message) {
            if (logLevel.compareTo(LogLevel.WARN) <= 0) {
                print("WARN", message);
            }
        }

        public void error(Object message) {
            if (logLevel.compareTo(LogLevel.ERROR) <= 0) {
                print("ERROR", message);
            }
        }

        public void fatal(Object message) {
            if (logLevel.compareTo(LogLevel.FATAL) <= 0) {
                print("FATAL", message);
            }
        }

        private void print(final String category, final Object message) {
            if (Objects.isNull(message)) {
                fatal("Invalid message object specified.");
            }
            else {
                write(String.format(getTimestamp() + " " + name + " [%s]: %s", category, message.toString()));
            }
        }

        void write(final String message) {
            System.out.println(message);
        }

        String getTimestamp() {
            Date date = new Date();
            return dateFormat.format(date);
        }
    }

    public static Log getLogger(Class<?> clazz) {
        return new Log(clazz);
    }
}
