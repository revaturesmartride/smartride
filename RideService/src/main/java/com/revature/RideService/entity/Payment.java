package com.revature.RideService.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_ride_id", columnList = "ride_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Rider who made the payment
    @Column(nullable = false)
    private Long riderId;

    // Driver who receives the payment
    @Column(nullable = false)
    private Long driverId;

    // Total fare amount
    @Column(nullable = false)
    private Double amount;

    // Payment method used
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    // Status of payment
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    // Unique transaction ID from payment gateway
    @Column(unique = true)
    private String transactionId;

    // Timestamp when payment was created
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Each ride has one payment
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_id", nullable = false, unique = true)
    private Ride ride;

    // Automatically set creation timestamp
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}