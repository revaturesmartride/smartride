package com.revature.RideService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.RideService.dto.request.PaymentRequestDTO;
import com.revature.RideService.entity.Payment;
import com.revature.RideService.entity.PaymentMethod;
import com.revature.RideService.entity.PaymentStatus;
import com.revature.RideService.entity.Ride;
import com.revature.RideService.service.PaymentService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    // Test Process Payment API
    @Test
    void testProcessPayment_success() throws Exception {

        PaymentRequestDTO request = PaymentRequestDTO.builder()
                .rideId(1L)
                .riderId(10L)
                .driverId(20L)
                .amount(100.0)
                .paymentMethod("UPI")
                .build();

        Payment response = Payment.builder()
                .id(1L)
                .riderId(10L)
                .driverId(20L)
                .amount(100.0)
                .paymentMethod(PaymentMethod.UPI)
                .paymentStatus(PaymentStatus.SUCCESS)
                .transactionId("txn123")
                .createdAt(java.time.LocalDateTime.of(2026, 3, 19, 10, 0))
                .ride(Ride.builder().id(1L).build())
                .build();

        when(paymentService.processPayment(any(PaymentRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/payments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.amount").value(100.0));
    }

    // Test Get Payment By Ride
    @Test
    void testGetPaymentByRide() throws Exception {

        Payment response = Payment.builder()
                .id(1L)
                .amount(100.0)
                .paymentStatus(PaymentStatus.SUCCESS)
                .createdAt(java.time.LocalDateTime.of(2026, 3, 19, 10, 0))
                .ride(Ride.builder().id(1L).build())
                .build();

        when(paymentService.getPaymentByRide(1L)).thenReturn(response);

        mockMvc.perform(get("/api/payments/ride/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ride.id").value(1L));
    }

    // Test Get Payment By Transaction
    @Test
    void testGetPaymentByTransaction() throws Exception {

        Payment response = Payment.builder()
                .id(1L)
                .transactionId("txn123")
                .paymentStatus(PaymentStatus.SUCCESS)
                .createdAt(java.time.LocalDateTime.of(2026, 3, 19, 10, 0))
                .ride(Ride.builder().id(1L).build())
                .build();

        when(paymentService.getPaymentByTransaction("txn123")).thenReturn(response);

        mockMvc.perform(get("/api/payments/transaction/txn123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("txn123"));
    }
}