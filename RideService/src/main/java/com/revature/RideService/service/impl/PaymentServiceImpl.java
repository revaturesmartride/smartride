package com.revature.RideService.service.impl;

import com.revature.RideService.dto.request.PaymentRequestDTO;
import com.revature.RideService.entity.Payment;
import com.revature.RideService.entity.PaymentMethod;
import com.revature.RideService.entity.PaymentStatus;
import com.revature.RideService.entity.Ride;
import com.revature.RideService.repository.PaymentRepository;
import com.revature.RideService.repository.RideRepository;
import com.revature.RideService.service.PaymentService;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RideRepository rideRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              RideRepository rideRepository) {
        this.paymentRepository = paymentRepository;
        this.rideRepository = rideRepository;
    }

    @Override
    public Payment processPayment(PaymentRequestDTO request) {

        // fetch ride
        Ride ride = rideRepository.findById(request.getRideId())
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        // validate amount
        if (request.getAmount() <= 0) {
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
                .ride(ride)
                .build();

        return paymentRepository.save(payment);
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