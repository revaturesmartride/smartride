package com.smartride.userservice.mapper;

import com.smartride.userservice.dto.response.UserResponse;
import com.smartride.userservice.model.UserEntity;

public class UserMapper {
    public static UserResponse toResponse(UserEntity user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .userEmail(user.getUserEmail())
                .userPhone(user.getUserPhone())
                .userRole(user.getUserRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
