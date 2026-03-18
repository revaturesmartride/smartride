package com.smartride.userservice.controller;

import com.smartride.userservice.dto.response.UserResponse;
import com.smartride.userservice.model.DriverApprovalStatus;
import com.smartride.userservice.model.UserStatus;
import com.smartride.userservice.repository.DriverProfileRepository;
import com.smartride.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.smartride.userservice.mapper.UserMapper;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserRepository userRepository;
    private final DriverProfileRepository driverProfileRepository;
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }
    @PutMapping("/drivers/{driverId}/approve")
    public ResponseEntity<Void> approveDriver(@PathVariable Long driverId) {
        var driver = driverProfileRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        driver.setApprovalStatus(DriverApprovalStatus.APPROVED);
        driverProfileRepository.save(driver);
        return ResponseEntity.ok().build();
    }
    @PutMapping("/drivers/{driverId}/reject")
    public ResponseEntity<Void> rejectDriver(@PathVariable Long driverId) {
        var driver = driverProfileRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        driver.setApprovalStatus(DriverApprovalStatus.REJECTED);
        driverProfileRepository.save(driver);
        return ResponseEntity.ok().build();
    }
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<Void> changeUserStatus(@PathVariable Long userId, @RequestParam UserStatus status) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(status);
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

}
