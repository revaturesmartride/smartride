package com.revature.RideService.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ride_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Rider ID is required")
    private Long riderId;

    @NotBlank(message = "Pickup location cannot be empty")
    @Size(max = 255, message = "Pickup location must not exceed 255 characters")
    private String pickupLocation;

    @NotBlank(message = "Drop location cannot be empty")
    @Size(max = 255, message = "Drop location must not exceed 255 characters")
    private String dropLocation;

    @NotBlank(message = "Ride request status is required")
    @Size(max = 50)
    private String status;

    @NotNull(message = "Requested time is required")
    private LocalDateTime requestedTime;

    @OneToOne(mappedBy = "rideRequest", cascade = CascadeType.ALL)
    private Ride ride;
}