package com.revature.RideService.dto.request;

import lombok.*;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {

    private Long riderId;
    private Long driverId;
    private Long rideId;
    private Double amount;
    private String paymentMethod;


}