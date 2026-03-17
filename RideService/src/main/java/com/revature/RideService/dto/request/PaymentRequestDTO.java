package com.revature.RideService.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PaymentRequestDTO {
    @NotNull(message = "Rider ID is required")
    private Long riderId;

    @NotNull(message = "Driver ID is required")
    private Long driverId;

    @NotNull(message = "Ride ID is required")
    private Long rideId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    private Double amount;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
}