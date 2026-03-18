package com.revature.RideService.service;

import com.revature.RideService.dto.request.PaymentRequestDTO;
import com.revature.RideService.dto.response.PaymentResponse;
import com.revature.RideService.entity.Payment;

public interface PaymentService {

    PaymentResponse processPayment(PaymentRequestDTO request);

    PaymentResponse getPaymentByRide(Long rideId);

    PaymentResponse getPaymentByTransaction(String transactionId);
}
