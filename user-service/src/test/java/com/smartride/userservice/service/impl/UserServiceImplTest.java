package com.smartride.userservice.service.impl;

import com.smartride.userservice.dto.request.UserUpdateRequest;
import com.smartride.userservice.dto.response.UserResponse;
import com.smartride.userservice.exception.ResourceNotFoundException;
import com.smartride.userservice.exception.UnauthorizedException;
import com.smartride.userservice.model.UserEntity;
import com.smartride.userservice.model.UserRole;
import com.smartride.userservice.model.UserStatus;
import com.smartride.userservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        log.info("Setting up UserService test data");

        user = UserEntity.builder()
                .userId(1L)
                .userName("John")
                .userEmail("john@test.com")
                .userPhone("9999999999")
                .userRole(UserRole.RIDER)
                .status(UserStatus.ACTIVE)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext(); // Prevent auth leakage between tests
    }

    // ------------------ getUserById ------------------

    @Test
    @DisplayName("Get user by ID successfully")
    void getUserById_success() {
        log.info("Testing getUserById_success");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(1L);

        log.info("Response: {}", response);

        assertNotNull(response);
        assertEquals("John", response.getUserName());
        assertEquals("john@test.com", response.getUserEmail());

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Throw exception when user not found by ID")
    void getUserById_notFound() {
        log.warn("Testing getUserById_notFound");

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserById(1L));
    }

    // ------------------ getAllUsers ------------------

    @Test
    @DisplayName("Get all users successfully")
    void getAllUsers_success() {
        log.info("Testing getAllUsers_success");

        List<UserEntity> users = Arrays.asList(user);

        when(userRepository.findAll()).thenReturn(users);

        List<UserResponse> response = userService.getAllUsers();

        log.info("Users fetched: {}", response);

        assertEquals(1, response.size());
        assertEquals("John", response.get(0).getUserName());

        verify(userRepository, times(1)).findAll();
    }

    // ------------------ deleteUser ------------------

    @Test
    @DisplayName("Delete user successfully")
    void deleteUser_success() {
        log.info("Testing deleteUser_success");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository, times(1)).delete(user);
    }

    @Test
    @DisplayName("Throw exception when deleting non-existing user")
    void deleteUser_notFound() {
        log.warn("Testing deleteUser_notFound");

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.deleteUser(1L));
    }

    // ------------------ updateUser ------------------

    @Test
    @DisplayName("Update user successfully")
    void updateUser_success() {
        log.info("Testing updateUser_success");

        UserUpdateRequest request = UserUpdateRequest.builder()
                .userName("UpdatedName")
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("john@test.com", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByUserEmail("john@test.com")).thenReturn(Optional.of(user));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);

        UserResponse response = userService.updateUser(1L, request);

        log.info("Updated response: {}", response);

        assertEquals("UpdatedName", response.getUserName());

        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Throw exception when user tries to update another user (Unauthorized)")
    void updateUser_unauthorized() {
        log.error("Testing updateUser_unauthorized");

        UserUpdateRequest request = UserUpdateRequest.builder()
                .userName("Hack")
                .build();

        UserEntity anotherUser = UserEntity.builder()
                .userId(2L)
                .userEmail("other@test.com")
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("other@test.com", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByUserEmail("other@test.com")).thenReturn(Optional.of(anotherUser));

        assertThrows(UnauthorizedException.class,
                () -> userService.updateUser(1L, request));
    }

    @Test
    @DisplayName("Throw exception when updating non-existing user")
    void updateUser_userNotFound() {
        log.warn("Testing updateUser_userNotFound");

        UserUpdateRequest request = UserUpdateRequest.builder()
                .userName("Update")
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("john@test.com", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByUserEmail("john@test.com")).thenReturn(Optional.of(user));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUser(1L, request));
    }

    @Test
    @DisplayName("Update all user fields successfully")
    void updateUser_fullUpdate() {
        log.info("Testing updateUser_fullUpdate");

        UserUpdateRequest request = UserUpdateRequest.builder()
                .userName("NewName")
                .userEmail("new@test.com")
                .userPhone("8888888888")
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("john@test.com", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByUserEmail("john@test.com")).thenReturn(Optional.of(user));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);

        UserResponse response = userService.updateUser(1L, request);

        log.info("Updated response (full): {}", response);

        assertNotNull(response);

        verify(userRepository, times(1)).save(any(UserEntity.class));
    }
}