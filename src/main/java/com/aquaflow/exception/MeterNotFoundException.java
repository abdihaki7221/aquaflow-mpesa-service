package com.aquaflow.exception;
public class MeterNotFoundException extends RuntimeException {
    public MeterNotFoundException(String meterNumber) { super("Meter not found: " + meterNumber); }
}
