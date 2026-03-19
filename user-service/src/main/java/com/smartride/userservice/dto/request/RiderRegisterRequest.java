package com.smartride.userservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class RiderRegisterRequest {
    @NotBlank
    private String userName;
    @Email(message = "Invalid email format")
    private String userEmail;
    @NotBlank
    private String userPassword;
    @NotBlank
    private String userPhone;
}
