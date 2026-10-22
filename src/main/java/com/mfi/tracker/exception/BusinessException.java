package com.mfi.tracker.exception;

/** A business rule was broken (for example a payment above the amount due). */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
