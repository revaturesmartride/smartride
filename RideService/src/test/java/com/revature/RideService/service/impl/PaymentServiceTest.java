package com.revature.RideService.service.impl;

import com.revature.RideService.dto.request.PaymentRequestDTO;
import com.revature.RideService.entity.*;
import com.revature.RideService.exception.PaymentFailedException;
import com.revature.RideService.kafka.producer.PaymentEventProducer;
import com.revature.RideService.repository.PaymentRepository;
import com.revature.RideService.repository.RideRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RideRepository rideRepository;

    @Mock
    private PaymentEventProducer paymentEventProducer;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Ride ride;

    @BeforeEach
    void setup() {
        ride = new Ride();
        ride.setId(1L);
        ride.setStatus(RideStatus.valueOf("COMPLETED"));
    }

    // Success Case
    @Test
    void testProcessPayment_success() {

        PaymentRequestDTO request = PaymentRequestDTO.builder()
                .rideId(1L)
                .riderId(10L)
                .driverId(20L)
                .amount(100.0)
                .paymentMethod("UPI")
                .build();

        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Payment response = paymentService.processPayment(request);

        assertNotNull(response);
        assertEquals(100.0, response.getAmount());
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(paymentEventProducer, times(1)).sendPaymentSuccessEvent(any());
    }

    // Ride Not Found
    @Test
    void testProcessPayment_rideNotFound() {

        PaymentRequestDTO request = PaymentRequestDTO.builder()
                .rideId(1L)
                .amount(100.0)
                .paymentMethod("UPI")
                .build();

        when(rideRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                paymentService.processPayment(request));

        assertEquals("Ride not found", exception.getMessage());
    }

    // Invalid Amount
    @Test
    void testProcessPayment_invalidAmount() {

        PaymentRequestDTO request = PaymentRequestDTO.builder()
                .rideId(1L)
                .amount(0.0)
                .paymentMethod("UPI")
                .build();

        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                paymentService.processPayment(request));

        assertEquals("Invalid payment amount", exception.getMessage());
    }

    // Ride Not Completed
    @Test
    void testProcessPayment_rideNotCompleted() {

        ride.setStatus(RideStatus.STARTED);

        PaymentRequestDTO request = PaymentRequestDTO.builder()
                .rideId(1L)
                .amount(100.0)
                .paymentMethod("UPI")
                .build();

        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));

        PaymentFailedException exception = assertThrows(PaymentFailedException.class, () ->
                paymentService.processPayment(request));

        assertTrue(exception.getMessage().contains("Ride not completed"));
    }

    // Duplicate Payment
    @Test
    void testProcessPayment_duplicatePayment() {

        PaymentRequestDTO request = PaymentRequestDTO.builder()
                .rideId(1L)
                .amount(100.0)
                .paymentMethod("UPI")
                .build();

        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));
        when(paymentRepository.findByRide_Id(1L)).thenReturn(Optional.of(new Payment()));

        PaymentFailedException exception = assertThrows(PaymentFailedException.class, () ->
                paymentService.processPayment(request));

        assertTrue(exception.getMessage().contains("Payment already exists"));
    }
}