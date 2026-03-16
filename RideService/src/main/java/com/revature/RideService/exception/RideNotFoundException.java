package com.revature.RideService.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a Ride or RideRequest cannot be found by the given id.
 * Maps to HTTP 404 NOT FOUND.
 *
 * Usage:
 *   throw new RideNotFoundException("Ride not found with id: " + rideId);
 *   throw new RideNotFoundException("RideRequest not found with id: " + rideRequestId);
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class RideNotFoundException extends RuntimeException {

    private final Long resourceId;

    // ── Constructor with message only ─────────────────────────────────────────
    public RideNotFoundException(String message) {
        super(message);
        this.resourceId = null;
    }

    // ── Constructor with message + id (useful for GlobalExceptionHandler) ─────
    public RideNotFoundException(String message, Long resourceId) {
        super(message);
        this.resourceId = resourceId;
    }

    public Long getResourceId() {
        return resourceId;
    }
}