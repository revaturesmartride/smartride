package com.smartride.userservice.controller;

import com.smartride.userservice.dto.request.DriverRegisterRequest;
import com.smartride.userservice.dto.request.LoginRequest;
import com.smartride.userservice.dto.request.RiderRegisterRequest;
import com.smartride.userservice.dto.response.AuthResponse;
import com.smartride.userservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getUserEmail());

        AuthResponse response = authService.login(request);

        log.info("Login successful for email: {}", request.getUserEmail());

        return ResponseEntity.ok(response);
    }
    @PostMapping("/register/rider")
    public ResponseEntity<AuthResponse> registerRider(@RequestBody RiderRegisterRequest request) {
        log.info("Rider registration request received for email: {}", request.getUserEmail());

        AuthResponse response = authService.registerRider(request);

        log.info("Rider registered successfully with email: {}", request.getUserEmail());

        return ResponseEntity.ok(response);
    }
    @PostMapping("/register/driver")
    public ResponseEntity<AuthResponse> registerDriver(@RequestBody DriverRegisterRequest request) {
        log.info("Driver registration request received for email: {}", request.getUserEmail());

        AuthResponse response = authService.registerDriver(request);

        log.info("Driver registered successfully with email: {}", request.getUserEmail());

        return ResponseEntity.ok(response);
    }


}
