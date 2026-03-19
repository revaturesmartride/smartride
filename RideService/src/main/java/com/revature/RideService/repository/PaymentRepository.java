package com.revature.RideService.repository;

import com.revature.RideService.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionId(String transactionId);

    Optional<Payment> findByRide_Id(Long rideId);

    List<Payment> findByRiderId(Long riderId);
}