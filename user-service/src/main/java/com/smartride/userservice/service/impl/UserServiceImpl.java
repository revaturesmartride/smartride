package com.smartride.userservice.service.impl;

import com.smartride.userservice.dto.request.UserUpdateRequest;
import com.smartride.userservice.dto.response.UserResponse;
import com.smartride.userservice.exception.ResourceNotFoundException;
import com.smartride.userservice.exception.UnauthorizedException;
import com.smartride.userservice.model.UserEntity;
import com.smartride.userservice.repository.UserRepository;
import com.smartride.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserResponse getUserById(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return mapToResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        List<UserEntity> users = userRepository.findAll();

        return users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        userRepository.delete(user);

    }

    @Override
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loggedInEmail = authentication.getName();

        UserEntity loggedInUser = userRepository.findByUserEmail(loggedInEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!loggedInUser.getUserId().equals(userId)) {
            throw new UnauthorizedException("You can update only your profile");
        }
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (request.getUserName() != null) {
            user.setUserName(request.getUserName());
        }
        if (request.getUserEmail() != null) {
            user.setUserEmail(request.getUserEmail());
        }
        if (request.getUserPhone() != null) {
            user.setUserPhone(request.getUserPhone());
        }
        return mapToResponse(userRepository.save(user));
    }
    /*
    this method helpful to convert userentity to userresponse present in dto
     */
    private UserResponse mapToResponse(UserEntity user) {
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
