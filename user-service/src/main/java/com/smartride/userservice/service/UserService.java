package com.smartride.userservice.service;

import com.smartride.userservice.dto.request.UserUpdateRequest;
import com.smartride.userservice.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse getUserById(Long userId);

    List<UserResponse> getAllUsers();

    void deleteUser(Long userId);

    UserResponse updateUser(Long userId, UserUpdateRequest request);
}
