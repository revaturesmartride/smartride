package com.revature.RideService.kafka.event;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Published when a driver accepts a PENDING ride request.
 * At this point the Ride row has been created and linked to the RideRequest.
 * Consumers: NotificationService (notify rider that driver is assigned)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideAssignedEvent {

    // ── Identifiers ───────────────────────────────────────────────────────────
    private Long rideId;            // Ride.id (PK of rides table — newly created)
    private Long rideRequestId;     // RideRequest.id this ride was created from
    private Long riderId;           // rider who originally requested
    private Long driverId;          // driver who accepted

    // ── Trip details ──────────────────────────────────────────────────────────
    private String pickupLocation;
    private String dropLocation;

    // ── Meta ──────────────────────────────────────────────────────────────────
    private LocalDateTime assignedAt;
}