package com.revature.RideService.repository;

import com.revature.RideService.entity.Ride;
import com.revature.RideService.entity.RideRequest;
import com.revature.RideService.entity.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface RideRequestRepository extends JpaRepository<RideRequest,Long> {
    // Get rides of a rider
    List<Ride> findByRiderId(Long riderId);
        

    // Get rides by status
    List<Ride> findByStatus(RideStatus status);
}
