package com.revature.RideService.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ride_ratings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long riderId;

    private Long driverId;

    private Integer rating;

    private String comment;

    private LocalDateTime createdAt;

    @OneToOne
    @JoinColumn(name = "ride_id")
    private Ride ride;
}