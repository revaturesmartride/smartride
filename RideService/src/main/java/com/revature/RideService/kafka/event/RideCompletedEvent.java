package com.revature.RideService.kafka.event;

public class RideCompletedEvent {


    private Long rideId;
    private Long riderId;
    private Long driverId;


    private String pickupLocation;
    private String dropLocation;


    // Both come from Ride entity fields (Double distance, Double fare)
    private Double distance;        // Ride.distance
    private Double fare;            // Ride.fare — calculated by FareCalculator


    private LocalDateTime startTime;
    private LocalDateTime endTime;


}
