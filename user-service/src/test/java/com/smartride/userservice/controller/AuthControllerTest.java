package com.smartride.userservice.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartride.userservice.dto.request.DriverRegisterRequest;
import com.smartride.userservice.dto.request.LoginRequest;
import com.smartride.userservice.dto.request.RiderRegisterRequest;
import com.smartride.userservice.dto.response.AuthResponse;
import com.smartride.userservice.model.UserRole;
import com.smartride.userservice.security.CustomUserDetailsService;
import com.smartride.userservice.security.JwtAuthenticationFilter;
import com.smartride.userservice.security.JwtTokenProvider;
import com.smartride.userservice.service.AuthService;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------------- LOGIN ----------------

    @Test
    @DisplayName("Login successfully")
    void login_success() throws Exception {

        log.info("Testing POST /api/auth/login");

        LoginRequest request = LoginRequest.builder()
                .userEmail("john@test.com")
                .userPassword("password")
                .build();

        AuthResponse response = AuthResponse.builder()
                .token("dummy-jwt-token")
                .role(UserRole.RIDER)
                .userId(1L)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("dummy-jwt-token"));

        log.info("Login test passed");
    }

    // ---------------- REGISTER RIDER ----------------

    @Test
    @DisplayName("Register rider successfully")
    void registerRider_success() throws Exception {

        log.info("Testing POST /api/auth/register/rider");

        RiderRegisterRequest request = RiderRegisterRequest.builder()
                .userEmail("rider@test.com")
                .userPassword("password")
                .build();

        AuthResponse response = AuthResponse.builder()
                .token("rider-token")
                .role(UserRole.RIDER)
                .userId(2L)
                .build();

        when(authService.registerRider(any(RiderRegisterRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/auth/register/rider")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("rider-token"));

        log.info("Register rider test passed");
    }

    // ---------------- REGISTER DRIVER ----------------

    @Test
    @DisplayName("Register driver successfully")
    void registerDriver_success() throws Exception {

        log.info("Testing POST /api/auth/register/driver");

        DriverRegisterRequest request = DriverRegisterRequest.builder()
                .userEmail("driver@test.com")
                .userPassword("password")
                .build();

        AuthResponse response = AuthResponse.builder()
                .token("driver-token")
                .role(UserRole.DRIVER)
                .userId(3L)
                .build();

        when(authService.registerDriver(any(DriverRegisterRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/auth/register/driver")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("driver-token"));

        log.info("Register driver test passed");
    }

}