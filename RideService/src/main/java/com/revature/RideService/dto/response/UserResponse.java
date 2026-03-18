package com.revature.RideService.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * Mirrors com.smartride.userservice.dto.response.UserResponse.
 * Used to deserialize the Feign response from UserService.
 * Fields match exactly — do not rename.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long      userId;
    private String    userName;
    private String    userEmail;
    private String    userPhone;
    private String    userRole;    // RIDER / DRIVER / ADMIN
    private String    status;      // ACTIVE / INACTIVE / SUSPENDED
    private Timestamp createdAt;
}