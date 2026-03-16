package com.revature.RideService.entity;

import jakarta.persistence.*;
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

    private Long riderId;

    private String pickupLocation;

    private String dropLocation;

    private String status;

    private LocalDateTime requestedTime;

    @OneToOne(mappedBy = "rideRequest", cascade = CascadeType.ALL)
    private Ride ride;
}