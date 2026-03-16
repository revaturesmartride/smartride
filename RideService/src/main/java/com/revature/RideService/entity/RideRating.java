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

    // Rider who gave the rating
    @Column(nullable = false)
    private Long riderId;

    // Driver who received the rating
    @Column(nullable = false)
    private Long driverId;

    // Rating value (1–5)
    @Column(nullable = false)
    private Integer rating;

    // Optional feedback from rider
    @Column(length = 500)
    private String comment;

    // Timestamp when rating was created
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Relationship with Ride entity
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_id", nullable = false, unique = true)
    private Ride ride;

    // Automatically set timestamp before saving
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}