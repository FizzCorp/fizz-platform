package io.fizz.analytics.common.opentsdb;

public class TSDBAPIErrorException extends Exception {
    private final int statusCode;
    private final String message;

    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    TSDBAPIErrorException(int aStatusCode, String aMessage) {
        this.statusCode = aStatusCode;
        this.message = aMessage;
    }
}
