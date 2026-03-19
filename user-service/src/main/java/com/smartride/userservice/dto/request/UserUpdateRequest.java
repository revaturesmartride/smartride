package com.smartride.userservice.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserUpdateRequest {
    private String userName;

    @Email(message = "Invalid email format")
    private String userEmail;

    private String userPhone;
}
