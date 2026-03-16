package com.revature.RideService.repository;

import com.revature.RideService.entity.Ride;
import com.revature.RideService.entity.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RideRepository extends JpaRepository<Ride,Long> {
    // Find ride by ride request
    Optional<Ride> findByRideRequestId(Long rideRequestId);

    // Get rides of a rider
    List<Ride> findByRiderId(Long riderId);

    // Get rides of a driver
    List<Ride> findByDriverId(Long driverId);

    // Get rides by status
    List<Ride> findByStatus(RideStatus status);

    // Get active ride of driver (driver can only have one active ride)
    Optional<Ride> findByDriverIdAndStatus(Long driverId, RideStatus status);

    // Get active ride of rider
    Optional<Ride> findByRiderIdAndStatus(Long riderId, RideStatus status);


}
