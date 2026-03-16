package com.revature.RideService.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideRequestDTO {

    private Long riderId;

    private String pickupLocation;

    private String dropLocation;
}