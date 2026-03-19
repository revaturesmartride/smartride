package com.smartride.userservice.controller;

import com.smartride.userservice.dto.request.UserUpdateRequest;
import com.smartride.userservice.dto.response.UserResponse;
import com.smartride.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@Slf4j
@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
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

}
