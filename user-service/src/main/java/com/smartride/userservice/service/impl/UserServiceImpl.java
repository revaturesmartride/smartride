package com.smartride.userservice.service.impl;

import com.smartride.userservice.dto.request.UserUpdateRequest;
import com.smartride.userservice.dto.response.UserResponse;
import com.smartride.userservice.exception.ResourceNotFoundException;
import com.smartride.userservice.exception.UnauthorizedException;
import com.smartride.userservice.model.UserEntity;
import com.smartride.userservice.repository.UserRepository;
import com.smartride.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserResponse getUserById(Long userId) {
        log.info("Fetching user with ID: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found with id: " + userId);
                });

        log.info("User fetched successfully with ID: {}", userId);

        return mapToResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        log.info("Fetching all users");

        List<UserEntity> users = userRepository.findAll();

        log.info("Total users fetched: {}", users.size());

        return users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Deleting user with ID: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found for deletion with ID: {}", userId);
                    return new ResourceNotFoundException("User not found with id: " + userId);
                });

        userRepository.delete(user);

        log.info("User deleted successfully with ID: {}", userId);
    }

    @Override
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        log.info("Update request for user ID: {}", userId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loggedInEmail = authentication.getName();

        log.debug("Logged in user email: {}", loggedInEmail);

        UserEntity loggedInUser = userRepository.findByUserEmail(loggedInEmail)
                .orElseThrow(() -> {
                    log.error("Logged-in user not found with email: {}", loggedInEmail);
                    return new RuntimeException("User not found");
                });

        if (!loggedInUser.getUserId().equals(userId)) {
            log.warn("Unauthorized update attempt by user ID: {} on user ID: {}",
                    loggedInUser.getUserId(), userId);
            throw new UnauthorizedException("You can update only your profile");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found for update with ID: {}", userId);
                    return new ResourceNotFoundException("User not found");
                });

        if (request.getUserName() != null) {
            user.setUserName(request.getUserName());
        }
        if (request.getUserEmail() != null) {
            user.setUserEmail(request.getUserEmail());
        }
        if (request.getUserPhone() != null) {
            user.setUserPhone(request.getUserPhone());
        }

        UserEntity updatedUser = userRepository.save(user);

        log.info("User updated successfully with ID: {}", userId);

        return mapToResponse(updatedUser);
    }
    /*
    this method helpful to convert userentity to userresponse present in dto
     */
    private UserResponse mapToResponse(UserEntity user) {
        log.debug("Mapping UserEntity to UserResponse for ID: {}", user.getUserId());

        return UserResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .userEmail(user.getUserEmail())
                .userPhone(user.getUserPhone())
                .userRole(user.getUserRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }

}
