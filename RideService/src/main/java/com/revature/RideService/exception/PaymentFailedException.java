package com.revature.RideService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a payment cannot be processed.
 * Covers scenarios such as:
 *   - Attempting to pay for a non-COMPLETED ride
 *   - Duplicate payment for the same ride
 *   - Payment gateway / transaction failure
 * Maps to HTTP 402 PAYMENT_REQUIRED.
 *
 * Usage:
 *   throw new PaymentFailedException("Payment already processed for rideId: " + rideId);
 *   throw new PaymentFailedException("Cannot process payment — ride is not COMPLETED");
 */
@ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
public class PaymentFailedException extends RuntimeException {

    private final Long rideId;

    // ── Constructor with message only ─────────────────────────────────────────
    public PaymentFailedException(String message) {
        super(message);
        this.rideId = null;
    }

    // ── Constructor with message + rideId (useful for GlobalExceptionHandler) ─
    public PaymentFailedException(String message, Long rideId) {
        super(message);
        this.rideId = rideId;
    }

    public Long getRideId() {
        return rideId;
    }
}
