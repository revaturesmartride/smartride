package com.smartride.userservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RiderRegisterRequest {
    @NotBlank
    private String userName;
    @Email
    private String userEmail;
    @NotBlank
    private String userPassword;
    private String userPhone;
}
