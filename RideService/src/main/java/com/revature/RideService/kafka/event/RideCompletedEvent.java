package com.revature.RideService.kafka.event;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Published when a ride is marked COMPLETED.
 * This is the trigger for PaymentService to initiate fare collection
 * and for NotificationService to prompt the rider to rate the trip.
 * Consumers: PaymentService, NotificationService
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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