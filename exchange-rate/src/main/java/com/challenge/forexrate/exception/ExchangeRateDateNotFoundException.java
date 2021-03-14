package com.challenge.forexrate.exception;

public class ExchangeRateDateNotFoundException extends RuntimeException {
    public ExchangeRateDateNotFoundException(String message) {
        super(message);
    }
}
