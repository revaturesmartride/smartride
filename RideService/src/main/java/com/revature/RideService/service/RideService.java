package com.revature.RideService.service;

import com.revature.RideService.dto.request.RideRequestDTO;
import com.revature.RideService.dto.response.RideResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface RideService {

    RideResponseDTO requestRide(RideRequestDTO dto);

    RideResponseDTO acceptRide(Long rideId, Long driverId);

    RideResponseDTO startRide(Long rideId);

    RideResponseDTO completeRide(Long rideId);

    RideResponseDTO getRide(Long rideId);

    List<RideResponseDTO> getRidesByRider(Long riderId);

    List<RideResponseDTO> getRidesByDriver(Long driverId);
    RideResponseDTO cancelRide(Long rideId);
}
