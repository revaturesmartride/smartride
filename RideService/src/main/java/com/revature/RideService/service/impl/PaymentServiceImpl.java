package com.revature.RideService.service.impl;

import com.revature.RideService.dto.request.PaymentRequestDTO;
import com.revature.RideService.dto.response.PaymentResponse;
import com.revature.RideService.entity.Payment;
import com.revature.RideService.entity.PaymentMethod;
import com.revature.RideService.entity.PaymentStatus;
import com.revature.RideService.entity.Ride;
import com.revature.RideService.kafka.producer.PaymentEventProducer;
import com.revature.RideService.repository.PaymentRepository;
import com.revature.RideService.repository.RideRepository;
import com.revature.RideService.service.PaymentService;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RideRepository rideRepository;
    private final PaymentEventProducer paymentEventProducer;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              RideRepository rideRepository, PaymentEventProducer paymentEventProducer) {
        this.paymentRepository = paymentRepository;
        this.rideRepository = rideRepository;
        this.paymentEventProducer = paymentEventProducer;
    }

    @Override
    public PaymentResponse processPayment(PaymentRequestDTO request) {

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

        Payment savedPayment = paymentRepository.save(payment);

        PaymentResponse response = mapToDTO(savedPayment);

        paymentEventProducer.sendPaymentSuccessEvent(response);

        return response;
    }

    @Override
    public PaymentResponse getPaymentByRide(Long rideId) {

        Payment payment = paymentRepository.findByRide_Id(rideId)
                .orElseThrow(() -> new RuntimeException("Payment not found for ride"));

        return mapToDTO(payment);


    }

    @Override
    public PaymentResponse getPaymentByTransaction(String transactionId) {

        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        return mapToDTO(payment);
    }

    private PaymentResponse mapToDTO(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .rideId(payment.getRide().getId())
                .riderId(payment.getRiderId())
                .driverId(payment.getDriverId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod().name())
                .paymentStatus(payment.getPaymentStatus().name())
                .transactionId(payment.getTransactionId())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
