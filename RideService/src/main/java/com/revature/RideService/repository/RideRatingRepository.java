package com.revature.RideService.repository;

import com.revature.RideService.entity.RideRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RideRatingRepository extends JpaRepository<RideRating, Long> {

    List<RideRating> findByDriverId(Long driverId);

    List<RideRating> findByRide_Id(Long rideId);
}