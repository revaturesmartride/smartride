package com.revature.RideService.entity;

import jakarta.persistence.*;
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

    private Long riderId;

    private Long driverId;

    private String pickupLocation;

    private String dropLocation;

    private Double distance;

    private Double fare;

    @Enumerated(EnumType.STRING)
    private RideStatus status;

    private LocalDateTime requestedTime;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @OneToOne
    @JoinColumn(name = "ride_request_id")
    private RideRequest rideRequest;

    @OneToOne(mappedBy = "ride", cascade = CascadeType.ALL)
    private Payment payment;

    @OneToOne(mappedBy = "ride", cascade = CascadeType.ALL)
    private RideRating rating;
}