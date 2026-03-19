package com.revature.RideService.controller;

import com.revature.RideService.dto.request.PaymentRequestDTO;
import com.revature.RideService.entity.Payment;
import com.revature.RideService.service.PaymentService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/process")
    public ResponseEntity<Payment> processPayment(@RequestBody PaymentRequestDTO request) {

        Payment payment = paymentService.processPayment(request);

        return ResponseEntity.ok(payment);
    }

    @GetMapping("/ride/{rideId}")
    public ResponseEntity<Payment> getPaymentByRide(@PathVariable Long rideId) {

        return ResponseEntity.ok(paymentService.getPaymentByRide(rideId));
    }

    @GetMapping("/transaction/{txnId}")
    public ResponseEntity<Payment> getPaymentByTxn(@PathVariable String txnId) {

        return ResponseEntity.ok(paymentService.getPaymentByTransaction(txnId));
    }
}