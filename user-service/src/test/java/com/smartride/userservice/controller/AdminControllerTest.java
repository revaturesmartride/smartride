package com.smartride.userservice.controller;

import static org.junit.jupiter.api.Assertions.*;
import com.smartride.userservice.dto.response.UserResponse;
import com.smartride.userservice.model.DriverApprovalStatus;
import com.smartride.userservice.model.DriverProfile;
import com.smartride.userservice.model.UserEntity;
import com.smartride.userservice.model.UserStatus;
import com.smartride.userservice.repository.DriverProfileRepository;
import com.smartride.userservice.repository.UserRepository;
import com.smartride.userservice.security.CustomUserDetailsService;
import com.smartride.userservice.security.JwtAuthenticationFilter;
import com.smartride.userservice.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private DriverProfileRepository driverProfileRepository;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;


    @InjectMocks
    private AdminController adminController;


    private UserEntity user;
    private DriverProfile driver;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .userId(1L)
                .userName("John")
                .userEmail("john@test.com")
                .status(UserStatus.ACTIVE)
                .build();

        driver = DriverProfile.builder()
                .driverId(1L)
                .user(user)
                .approvalStatus(DriverApprovalStatus.PENDING)
                .build();
    }

    // ------------------ getAllUsers ------------------
    @Test
    @DisplayName("Get all users successfully")
    void getAllUsers_success() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(user));

        ResponseEntity<List<UserResponse>> response = adminController.getAllUsers();

        assertNotNull(response);
        assertEquals(1, response.getBody().size());
        assertEquals("John", response.getBody().get(0).getUserName());

        verify(userRepository, times(1)).findAll();
    }

    // ------------------ approveDriver ------------------
    @Test
    @DisplayName("Approve driver successfully")
    void approveDriver_success() {
        when(driverProfileRepository.findById(1L)).thenReturn(Optional.of(driver));

        ResponseEntity<Void> response = adminController.approveDriver(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(DriverApprovalStatus.APPROVED, driver.getApprovalStatus());

        verify(driverProfileRepository, times(1)).save(driver);
    }

    @Test
    @DisplayName("Throw exception when driver not found for approve")
    void approveDriver_notFound() {
        when(driverProfileRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> adminController.approveDriver(1L));
    }

    // ------------------ rejectDriver ------------------
    @Test
    @DisplayName("Reject driver successfully")
    void rejectDriver_success() {
        when(driverProfileRepository.findById(1L)).thenReturn(Optional.of(driver));

        ResponseEntity<Void> response = adminController.rejectDriver(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(DriverApprovalStatus.REJECTED, driver.getApprovalStatus());

        verify(driverProfileRepository, times(1)).save(driver);
    }

    @Test
    @DisplayName("Throw exception when driver not found for reject")
    void rejectDriver_notFound() {
        when(driverProfileRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> adminController.rejectDriver(1L));
    }

    // ------------------ changeUserStatus ------------------
    @Test
    @DisplayName("Change user status successfully")
    void changeUserStatus_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ResponseEntity<Void> response = adminController.changeUserStatus(1L, UserStatus.BLOCKED);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(UserStatus.BLOCKED, user.getStatus());

        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Throw exception when user not found for status change")
    void changeUserStatus_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> adminController.changeUserStatus(1L, UserStatus.BLOCKED));
    }


}