package com.revature.RideService.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;



    @Entity
    @Table(name = "rides")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class Ride {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @NotNull(message = "Rider ID is required")
        private Long riderId;

        @NotNull(message = "Driver ID is required")
        private Long driverId;

        @NotBlank(message = "Pickup location cannot be empty")
        @Size(max = 255, message = "Pickup location must not exceed 255 characters")
        private String pickupLocation;

        @NotBlank(message = "Drop location cannot be empty")
        @Size(max = 255, message = "Drop location must not exceed 255 characters")
        private String dropLocation;

        @Positive(message = "Distance must be greater than 0")
        private Double distance;

        @PositiveOrZero(message = "Fare cannot be negative")
        private Double fare;

        @NotNull(message = "Ride status is required")
        @Enumerated(EnumType.STRING)
        private RideStatus status;

        @NotNull(message = "Requested time is required")
        private LocalDateTime requestedTime;

        private LocalDateTime startTime;

        private LocalDateTime endTime;

        @NotNull(message = "Ride request reference is required")
        @OneToOne
        @JoinColumn(name = "ride_request_id", nullable = false)
        private RideRequest rideRequest;

        @OneToOne(mappedBy = "ride", cascade = CascadeType.ALL)
        private Payment payment;

        @OneToOne(mappedBy = "ride", cascade = CascadeType.ALL)
        private RideRating rating;
    }
