package com.aquaflow.exception;
public class DarajaApiException extends RuntimeException {
    public DarajaApiException(String message) { super(message); }
    public DarajaApiException(String message, Throwable cause) { super(message, cause); }
}
