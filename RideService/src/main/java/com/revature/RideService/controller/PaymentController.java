package com.revature.RideService.controller;

import com.revature.RideService.dto.request.PaymentRequestDTO;
import com.revature.RideService.entity.Payment;
import com.revature.RideService.service.PaymentService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }


    //Process Payment
    @PostMapping("/process")
    public ResponseEntity<Payment> processPayment(@Valid @RequestBody PaymentRequestDTO request) {

        Payment response= paymentService.processPayment(request);

        return ResponseEntity.ok(response);
    }

    //Get Payment by ride
    @GetMapping("/ride/{rideId}")
    public ResponseEntity<Payment> getPaymentByRide(@PathVariable Long rideId) {

        return ResponseEntity.ok(paymentService.getPaymentByRide(rideId));
    }

    //Get payment by transaction

    @GetMapping("/transaction/{txnId}")
    public ResponseEntity<Payment> getPaymentByTxn(@PathVariable String txnId) {

        return ResponseEntity.ok(paymentService.getPaymentByTransaction(txnId));
    }
}