package com.revature.RideService.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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

    @NotNull(message = "Rider ID is required")
    private Long riderId;

    @NotNull(message = "Driver ID is required")
    private Long driverId;

    @NotNull(message = "Payment amount is required")
    @Positive(message = "Amount must be greater than 0")
    private Double amount;

    @NotNull(message = "Payment method is required")
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @NotNull(message = "Payment status is required")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @NotBlank(message = "Transaction ID cannot be empty")
    @Size(max = 100, message = "Transaction ID must not exceed 100 characters")
    private String transactionId;

    @NotNull(message = "Payment creation time is required")
    private LocalDateTime createdAt;

    @NotNull(message = "Ride reference is required")
    @OneToOne
    @JoinColumn(name = "ride_id", nullable = false)
    private Ride ride;
}