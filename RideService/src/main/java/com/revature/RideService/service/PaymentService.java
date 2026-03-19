package com.revature.RideService.service;

import com.revature.RideService.dto.request.PaymentRequestDTO;
import com.revature.RideService.entity.Payment;

public interface PaymentService {

    Payment processPayment(PaymentRequestDTO request);

    Payment getPaymentByRide(Long rideId);

    Payment getPaymentByTransaction(String transactionId);
}