package com.revature.RideService.exception;

public class RideNotFoundException extends RuntimeException {

    public RideNotFoundException(String message) {
        super(message);
    }

    public RideNotFoundException(Long rideId) {
        super("Ride not found with ID: " + rideId);
    }
}
