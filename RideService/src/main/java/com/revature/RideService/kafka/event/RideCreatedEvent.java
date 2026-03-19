package com.revature.RideService.kafka.event;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Published when a rider submits a new ride request.
 * At this point only a RideRequest row exists — no Ride row yet.
 * Consumers: NotificationService (notify nearby drivers), DriverMatchingService
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideCreatedEvent {


    private Long rideRequestId;     // RideRequest.id (PK of ride_requests table)
    private Long riderId;           // who requested the ride


    private String pickupLocation;
    private String dropLocation;


    private String status;          // always "PENDING" at this stage
    private LocalDateTime requestedTime;
}
