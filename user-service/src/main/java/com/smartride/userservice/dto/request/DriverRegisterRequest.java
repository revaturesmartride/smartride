package com.smartride.userservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverRegisterRequest {
    @NotBlank
    private String userName;
    @Email(message = "Invalid email format")
    @NotBlank
    private String userEmail;
    @NotBlank
    private String userPassword;
    @NotBlank
    private String userPhone;
    private String licenceNumber;
    private String vehicleId;
}
