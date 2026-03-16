package com.revature.RideService.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

    private Long rideId;

    private Long riderId;

    private Double amount;

    private String paymentMethod;
}