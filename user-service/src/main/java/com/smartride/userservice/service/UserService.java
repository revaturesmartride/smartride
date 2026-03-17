package com.smartride.userservice.service;

import com.smartride.userservice.dto.response.UserResponse;

import java.util.List;

interface UserService {
    UserResponse getUserById(Long userId);

    List<UserResponse> getAllUsers();

    void deleteUser(Long userId);
}
