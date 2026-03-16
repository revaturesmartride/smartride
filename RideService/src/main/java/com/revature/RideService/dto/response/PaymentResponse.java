package com.revature.RideService.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private Long paymentId;

    private Long rideId;

    private Double amount;

    private String paymentMethod;

    private String paymentStatus;

    private String transactionId;
}