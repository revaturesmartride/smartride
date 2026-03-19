package com.revature.RideService.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingResponse {



    private Long rideId;

    private Integer rating;

    private String comment;
}