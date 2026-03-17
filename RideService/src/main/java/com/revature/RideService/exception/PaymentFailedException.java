package com.revature.RideService.exception;

public class PaymentFailedException extends RuntimeException {

    private final String transactionId;
    private final String errorCode;

    // Basic constructor
    public PaymentFailedException(String message) {
        super(message);
        this.transactionId = null;
        this.errorCode = null;
    }

    // Constructor with errorCode
    public PaymentFailedException(String message, String errorCode) {
        super(message);
        this.transactionId = null;
        this.errorCode = errorCode;
    }

    // Constructor with transactionId + errorCode
    public PaymentFailedException(String message, String transactionId, String errorCode) {
        super(message);
        this.transactionId = transactionId;
        this.errorCode = errorCode;
    }

    // Constructor with cause (for debugging)
    public PaymentFailedException(String message, Throwable cause) {
        super(message, cause);
        this.transactionId = null;
        this.errorCode = null;
    }

    // Getters
    public String getTransactionId() {
        return transactionId;
    }

    public String getErrorCode() {
        return errorCode;
    }
}