package com.smartride.userservice.controller;

import com.smartride.userservice.dto.request.UserUpdateRequest;
import com.smartride.userservice.dto.response.UserResponse;
import com.smartride.userservice.dto.response.UserIdResponse;
import com.smartride.userservice.model.UserEntity;
import com.smartride.userservice.repository.UserRepository;
import com.smartride.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        log.info("Request received to fetch user with ID: {}", id);
        UserResponse response = userService.getUserById(id);
        log.info("User fetched successfully with ID: {}", id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateRequest request) {
        log.info("Request received to update user with ID: {}", id);
        UserResponse response = userService.updateUser(id, request);
        log.info("User updated successfully with ID: {}", id);
        return ResponseEntity.ok(response);
    }

    // ── Called by API Gateway to resolve email → userId ───────────────────────
    // Full path: GET /api/users/email/{email}
    @GetMapping("/email/{email}")
    public ResponseEntity<UserIdResponse> getUserIdByEmail(
            @PathVariable String email) {
        log.info("Gateway requesting userId for email: {}", email);
        UserEntity user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with email: " + email));
        return ResponseEntity.ok(new UserIdResponse(user.getUserId()));
    }

    // ── Internal endpoint for service-to-service calls ───────────────────────
    @GetMapping("/internal/{id}")
    public ResponseEntity<UserResponse> getUserInternal(@PathVariable Long id) {
        log.info("Internal request to fetch user with ID: {}", id);
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        UserResponse response = UserResponse.builder()
                .userId(user.getUserId())
                .userEmail(user.getUserEmail())
                .userRole(user.getUserRole())
                .status(user.getStatus())
                .build();
        
        return ResponseEntity.ok(response);
    }
}
