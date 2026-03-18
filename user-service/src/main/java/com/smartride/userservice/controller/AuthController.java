package com.smartride.userservice.controller;

import com.smartride.userservice.dto.request.DriverRegisterRequest;
import com.smartride.userservice.dto.request.LoginRequest;
import com.smartride.userservice.dto.request.RiderRegisterRequest;
import com.smartride.userservice.dto.response.AuthResponse;
import com.smartride.userservice.service.impl.AuthServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthServiceImpl authService;
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/register/rider")
    public ResponseEntity<AuthResponse> registerRider(@RequestBody RiderRegisterRequest request) {
        AuthResponse response = authService.registerRider(request);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/register/driver")
    public ResponseEntity<AuthResponse> registerDriver(@RequestBody DriverRegisterRequest request) {
        AuthResponse response = authService.registerDriver(request);
        return ResponseEntity.ok(response);
    }


}
