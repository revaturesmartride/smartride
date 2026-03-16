package com.revature.RideService.kafka.event;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Published when a payment is successfully processed for a completed ride.
 * Consumers: NotificationService (send receipt to rider),
 *            UserService (update driver earnings)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCompletedEvent {

    // ── Identifiers ───────────────────────────────────────────────────────────
    private Long paymentId;         // Payment.id (PK of payments table)
    private Long rideId;            // the ride this payment belongs to
    private Long riderId;           // who paid
    private Long driverId;          // who receives earnings

    // ── Payment details ───────────────────────────────────────────────────────
    private Double amount;          // Payment.amount
    private String paymentMethod;   // Payment.paymentMethod  e.g. "UPI", "CARD", "CASH"
    private String transactionId;   // Payment.transactionId — unique UUID per transaction

    // ── Meta ──────────────────────────────────────────────────────────────────
    private LocalDateTime paidAt;   // Payment.createdAt
}