package com.revature.RideService.controller;

import com.revature.RideService.dto.request.PaymentRequestDTO;
import com.revature.RideService.dto.response.PaymentResponse;
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
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequestDTO request) {

        PaymentResponse payment = paymentService.processPayment(request);

        return ResponseEntity.ok(payment);
    }

    @GetMapping("/ride/{rideId}")
    public ResponseEntity<PaymentResponse> getPaymentByRide(@PathVariable Long rideId) {

        return ResponseEntity.ok(paymentService.getPaymentByRide(rideId));
    }

    @GetMapping("/transaction/{txnId}")
    public ResponseEntity<PaymentResponse> getPaymentByTxn(@PathVariable String txnId) {

        return ResponseEntity.ok(paymentService.getPaymentByTransaction(txnId));
    }
}