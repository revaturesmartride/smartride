package com.revature.RideService.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideResponseDTO {

    private Long rideId;

    private Long riderId;

    private Long driverId;

    private String pickupLocation;

    private String dropLocation;

    private Double distance;

    private Double fare;

    private String status;

    private LocalDateTime requestedTime;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}