package com.smartride.userservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @Email(message = "Invalid email format")
    @NotBlank
    private String userEmail;
    @NotBlank
    private String userPassword;
}
