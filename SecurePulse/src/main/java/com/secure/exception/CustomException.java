package com.secure.exception;

/**
 * Custom exception class to handle application-specific exceptions.
 */
public class CustomException extends RuntimeException {
    public CustomException(String message) {
        super(message);
    }
}
