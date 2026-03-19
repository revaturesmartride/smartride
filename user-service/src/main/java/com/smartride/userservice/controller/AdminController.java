package com.smartride.userservice.controller;

import com.smartride.userservice.dto.response.UserResponse;
import com.smartride.userservice.model.DriverApprovalStatus;
import com.smartride.userservice.model.UserStatus;
import com.smartride.userservice.repository.DriverProfileRepository;
import com.smartride.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.smartride.userservice.mapper.UserMapper;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserRepository userRepository;
    private final DriverProfileRepository driverProfileRepository;
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Admin requested all users");

        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());

        log.info("Total users returned: {}", users.size());

        return ResponseEntity.ok(users);
    }
    @PutMapping("/drivers/{driverId}/approve")
    public ResponseEntity<Void> approveDriver(@PathVariable Long driverId) {
        log.info("Admin approving driver with ID: {}", driverId);

        var driver = driverProfileRepository.findById(driverId)
                .orElseThrow(() -> {
                    log.error("Driver not found with ID: {}", driverId);
                    return new RuntimeException("Driver not found");
                });

        driver.setApprovalStatus(DriverApprovalStatus.APPROVED);
        driverProfileRepository.save(driver);

        log.info("Driver approved successfully with ID: {}", driverId);

        return ResponseEntity.ok().build();
    }
    @PutMapping("/drivers/{driverId}/reject")
    public ResponseEntity<Void> rejectDriver(@PathVariable Long driverId) {
        log.info("Admin rejecting driver with ID: {}", driverId);

        var driver = driverProfileRepository.findById(driverId)
                .orElseThrow(() -> {
                    log.error("Driver not found with ID: {}", driverId);
                    return new RuntimeException("Driver not found");
                });

        driver.setApprovalStatus(DriverApprovalStatus.REJECTED);
        driverProfileRepository.save(driver);

        log.info("Driver rejected successfully with ID: {}", driverId);

        return ResponseEntity.ok().build();
    }
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<Void> changeUserStatus(@PathVariable Long userId, @RequestParam UserStatus status) {
        log.info("Admin changing status for user ID: {} to {}", userId, status);

        var user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new RuntimeException("User not found");
                });

        user.setStatus(status);
        userRepository.save(user);

        log.info("User status updated successfully for ID: {}", userId);

        return ResponseEntity.ok().build();
    }

}
