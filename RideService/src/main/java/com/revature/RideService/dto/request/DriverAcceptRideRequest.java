package com.revature.RideService.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverAcceptRideRequest {

    private Long rideId;

    private Long driverId;
}