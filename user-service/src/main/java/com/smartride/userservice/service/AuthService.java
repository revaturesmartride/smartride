package com.smartride.userservice.service;

import com.smartride.userservice.dto.request.DriverRegisterRequest;
import com.smartride.userservice.dto.request.LoginRequest;
import com.smartride.userservice.dto.request.RiderRegisterRequest;
import com.smartride.userservice.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse registerRider(RiderRegisterRequest request);

    AuthResponse registerDriver(DriverRegisterRequest request);

    AuthResponse login(LoginRequest request);
}
