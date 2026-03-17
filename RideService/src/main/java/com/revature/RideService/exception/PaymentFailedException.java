package com.revature.RideService.exception;

public class PaymentFailedException extends RuntimeException {

    public PaymentFailedException(String message) {
        super(message);
    }

    public PaymentFailedException(String paymentId, String reason) {
        super("Payment failed for payment ID: " + paymentId + ". Reason: " + reason);
    }
}