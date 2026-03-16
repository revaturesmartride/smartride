package com.revature.RideService.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long riderId;

    private Long driverId;

    private Double amount;
@Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
@Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private String transactionId;

    private LocalDateTime createdAt;

    @OneToOne
    @JoinColumn(name = "ride_id")
    private Ride ride;
}