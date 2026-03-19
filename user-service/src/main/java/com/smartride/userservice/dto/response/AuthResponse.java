package com.smartride.userservice.dto.response;

import com.smartride.userservice.model.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private UserRole role;
    private Long userId;
}
