package com.revature.RideService.service.impl;

import com.revature.RideService.dto.request.PaymentRequestDTO;
import com.revature.RideService.dto.response.PaymentResponse;
import com.revature.RideService.entity.Payment;
import com.revature.RideService.entity.PaymentMethod;
import com.revature.RideService.entity.PaymentStatus;
import com.revature.RideService.entity.Ride;
import com.revature.RideService.entity.RideStatus;
import com.revature.RideService.exception.PaymentFailedException;
import com.revature.RideService.kafka.producer.PaymentEventProducer;
import com.revature.RideService.repository.PaymentRepository;
import com.revature.RideService.repository.RideRepository;
import com.revature.RideService.service.PaymentService;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RideRepository rideRepository;
    private final PaymentEventProducer paymentEventProducer;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              RideRepository rideRepository,
                              PaymentEventProducer paymentEventProducer) {
        this.paymentRepository = paymentRepository;
        this.rideRepository = rideRepository;
        this.paymentEventProducer = paymentEventProducer;
    }

    @Override
    public Payment processPayment(PaymentRequestDTO request) {

        if (request == null || request.getRideId() == null) {
            throw new IllegalArgumentException("Ride ID must not be null");
        }

        // fetch ride
        Ride ride = rideRepository.findById(request.getRideId())
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        if (ride.getStatus() != RideStatus.COMPLETED) {
            throw new PaymentFailedException("Ride not completed: " + request.getRideId(), request.getRideId());
        }

        // prevent duplicate payment for same ride
        if (paymentRepository.findByRide_Id(request.getRideId()).isPresent()) {
            throw new PaymentFailedException("Payment already exists for rideId: " + request.getRideId(), request.getRideId());
        }

        // validate amount
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new IllegalArgumentException("Invalid payment amount");
        }

        // generate transaction id
        String transactionId = UUID.randomUUID().toString();

        Payment payment = Payment.builder()
                .riderId(request.getRiderId())
                .driverId(request.getDriverId())
                .amount(request.getAmount())
                .transactionId(transactionId)
                .paymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()))
                .paymentStatus(PaymentStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .ride(ride)
                .build();

        Payment saved = paymentRepository.save(payment);

        paymentEventProducer.sendPaymentSuccessEvent(
                PaymentResponse.builder()
                        .paymentId(saved.getId())
                        .rideId(ride.getId())
                        .riderId(saved.getRiderId())
                        .driverId(saved.getDriverId())
                        .amount(saved.getAmount())
                        .paymentMethod(saved.getPaymentMethod() != null ? saved.getPaymentMethod().name() : null)
                        .paymentStatus(saved.getPaymentStatus() != null ? saved.getPaymentStatus().name() : null)
                        .transactionId(saved.getTransactionId())
                        .createdAt(saved.getCreatedAt())
                        .build()
        );

        return saved;
    }

    @Override
    public Payment getPaymentByRide(Long rideId) {

        return paymentRepository.findByRide_Id(rideId)
                .orElseThrow(() -> new RuntimeException("Payment not found for ride"));
    }

    @Override
    public Payment getPaymentByTransaction(String transactionId) {

        return paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }
}