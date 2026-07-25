package com.example.application.shared.exception;

/**
 * These two exception files existed in the repo but were completely empty (0
 * bytes) - the architecture review that referenced them was describing an
 * aspirational design that was never actually implemented. This is the real
 * implementation.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
