package com.revature.RideService.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingRequest {

    private Long rideId;

    private Long riderId;

    private Long driverId;

    private Integer rating;

    private String comment;
}