package com.revature.RideService.kafka.consumer;

import com.revature.RideService.kafka.event.RideCompletedEvent;
import com.revature.RideService.dto.request.PaymentRequestDTO;
import com.revature.RideService.service.PaymentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class RideEventConsumer {

    private final PaymentService paymentService;

    public RideEventConsumer(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = "ride-completed", groupId = "payment-group")
    public void consumeRideCompletedEvent(RideCompletedEvent event) {

        PaymentRequestDTO request = PaymentRequestDTO.builder()
                .rideId(event.getRideId())
                .riderId(event.getRiderId())
                .driverId(event.getDriverId())
                .amount(event.getAmount())
                .paymentMethod("UPI") // default or logic
                .build();

        paymentService.processPayment(request);
    }
}