package com.revature.RideService.kafka.event;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Published when a driver marks the ride as started (IN_PROGRESS).
 * Consumers: NotificationService (notify rider trip has begun)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideStartedEvent {

    // ── Identifiers ───────────────────────────────────────────────────────────
    private Long rideId;
    private Long riderId;
    private Long driverId;

    // ── Trip details ──────────────────────────────────────────────────────────
    private String pickupLocation;
    private String dropLocation;

    // ── Meta ──────────────────────────────────────────────────────────────────
    private LocalDateTime startTime;
}
