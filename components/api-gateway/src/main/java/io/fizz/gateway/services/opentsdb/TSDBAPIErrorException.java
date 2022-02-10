package io.fizz.gateway.services.opentsdb;

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

    TSDBAPIErrorException(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }
}
