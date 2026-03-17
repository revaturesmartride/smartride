package com.smartride.userservice.dto.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String userEmail;
    private String userPassword;
}
