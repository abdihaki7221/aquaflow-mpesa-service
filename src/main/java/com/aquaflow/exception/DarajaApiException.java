package com.aquaflow.exception;

public class DarajaApiException extends RuntimeException {

    private final int statusCode;

    public DarajaApiException(String message) {
        super(message);
        this.statusCode = 500;
    }

    public DarajaApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public DarajaApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 500;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
