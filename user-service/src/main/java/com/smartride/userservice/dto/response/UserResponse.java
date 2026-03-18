package com.smartride.userservice.dto.response;

import com.smartride.userservice.model.UserRole;
import com.smartride.userservice.model.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
@Data
@Builder
public class UserResponse {
    private Long userId;
    private String userName;
    private String userEmail;
    private String userPhone;
    private UserRole userRole;
    private UserStatus status;
    private Timestamp createdAt;
}
