package com.revature.RideService.kafka.producer;

import com.revature.RideService.dto.response.PaymentResponse;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventProducer {

    private final KafkaTemplate<String, PaymentResponse> kafkaTemplate;

    public PaymentEventProducer(KafkaTemplate<String, PaymentResponse> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPaymentSuccessEvent(PaymentResponse response) {

        kafkaTemplate.send("payment-success", response);
    }
}