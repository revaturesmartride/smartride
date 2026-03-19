package com.smartride.userservice.controller;

import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartride.userservice.dto.request.UserUpdateRequest;
import com.smartride.userservice.dto.response.UserResponse;
import com.smartride.userservice.model.UserRole;
import com.smartride.userservice.model.UserStatus;
import com.smartride.userservice.security.CustomUserDetailsService;
import com.smartride.userservice.security.JwtAuthenticationFilter;
import com.smartride.userservice.security.JwtTokenProvider;
import com.smartride.userservice.service.UserService;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------------- GET USER ----------------

    @Test
    @DisplayName("Get user by ID successfully")
    void getUser_success() throws Exception {

        log.info("Testing GET /api/users/{id}");

        UserResponse response = UserResponse.builder()
                .userId(1L)
                .userName("John")
                .userEmail("john@test.com")
                .userPhone("9999999999")
                .userRole(UserRole.RIDER)
                .status(UserStatus.ACTIVE)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();

        when(userService.getUserById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("John"))
                .andExpect(jsonPath("$.userEmail").value("john@test.com"));

        log.info("GET user test passed");
    }

    // ---------------- UPDATE USER ----------------

    @Test
    @DisplayName("Update user successfully")
    void updateUser_success() throws Exception {

        log.info("Testing PATCH /api/users/{id}");

        UserUpdateRequest request = UserUpdateRequest.builder()
                .userName("UpdatedName")
                .build();

        UserResponse response = UserResponse.builder()
                .userId(1L)
                .userName("UpdatedName")
                .userEmail("john@test.com")
                .userPhone("9999999999")
                .userRole(UserRole.RIDER)
                .status(UserStatus.ACTIVE)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();

        when(userService.updateUser(eq(1L), any(UserUpdateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/users/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("UpdatedName"));

        log.info("PATCH user test passed");
    }

}