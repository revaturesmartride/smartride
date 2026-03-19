package com.smartride.userservice.service.impl;

import com.smartride.userservice.dto.request.DriverRegisterRequest;
import com.smartride.userservice.dto.request.LoginRequest;
import com.smartride.userservice.dto.request.RiderRegisterRequest;
import com.smartride.userservice.dto.response.AuthResponse;
import com.smartride.userservice.exception.ResourceAlreadyExistsException;
import com.smartride.userservice.exception.ResourceNotFoundException;
import com.smartride.userservice.exception.UnauthorizedException;
import com.smartride.userservice.model.DriverProfile;
import com.smartride.userservice.model.UserEntity;
import com.smartride.userservice.model.UserRole;
import com.smartride.userservice.model.UserStatus;
import com.smartride.userservice.repository.DriverProfileRepository;
import com.smartride.userservice.repository.UserRepository;
import com.smartride.userservice.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
@Slf4j
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private DriverProfileRepository driverProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private RiderRegisterRequest riderRequest;
    private DriverRegisterRequest driverRequest;
    private LoginRequest loginRequest;
    private UserEntity user;

    @BeforeEach
    void setup() {

        log.info("Setting up test data");

        riderRequest = RiderRegisterRequest.builder()
                .userName("John")
                .userEmail("john@test.com")
                .userPassword("password")
                .userPhone("9999999999")
                .build();

        driverRequest = DriverRegisterRequest.builder()
                .userName("Driver")
                .userEmail("driver@test.com")
                .userPassword("password")
                .userPhone("8888888888")
                .licenceNumber("LIC123")
                .vehicleId("VEH123")
                .build();

        loginRequest = LoginRequest.builder()
                .userEmail("john@test.com")
                .userPassword("password")
                .build();

        user = UserEntity.builder()
                .userId(1L)
                .userName("John")
                .userEmail("john@test.com")
                .userPassword("encodedPassword")
                .userRole(UserRole.RIDER)
                .status(UserStatus.ACTIVE)
                .build();

    }

    // ------------------ Rider Tests ------------------

    @Test
    @DisplayName("Register Rider Successfully")
    void registerRider_success() {

        log.info("Testing registerRider_success");

        when(userRepository.existsByUserEmail(riderRequest.getUserEmail())).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);
        when(jwtTokenProvider.generateToken(anyString(), anyString())).thenReturn("jwtToken");

        AuthResponse response = authService.registerRider(riderRequest);

        log.info("Response received: {}", response);

        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());
        assertEquals(UserRole.RIDER, response.getRole());
        assertEquals(1L, response.getUserId());

        verify(userRepository).save(any(UserEntity.class));
        verify(jwtTokenProvider).generateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Throw exception when rider email already exists")
    void registerRider_emailAlreadyExists() {

        log.warn("Testing registerRider_emailAlreadyExists");

        when(userRepository.existsByUserEmail(riderRequest.getUserEmail())).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class,
                () -> authService.registerRider(riderRequest));

        verify(userRepository, never()).save(any());
    }

    // ------------------ Driver Tests ------------------

    @Test
    @DisplayName("Register Driver Successfully")
    void registerDriver_success() {

        log.info("Testing registerDriver_success");

        UserEntity driverUser = UserEntity.builder()
                .userId(1L)
                .userName(driverRequest.getUserName())
                .userEmail(driverRequest.getUserEmail())
                .userPassword("encodedPassword")
                .userRole(UserRole.DRIVER)
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.existsByUserEmail(driverRequest.getUserEmail())).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(driverUser);
        when(jwtTokenProvider.generateToken(anyString(), anyString())).thenReturn("jwtToken");

        AuthResponse response = authService.registerDriver(driverRequest);

        log.info("Driver registration response: {}", response);

        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());
        assertEquals(UserRole.DRIVER, response.getRole());
        assertEquals(1L, response.getUserId());

        verify(userRepository).save(any(UserEntity.class));
        verify(driverProfileRepository).save(any(DriverProfile.class));
        verify(jwtTokenProvider).generateToken(anyString(), anyString());
    }
    @Test
    @DisplayName("Throw exception when driver email already exists")
    void registerDriver_emailAlreadyExists() {

        log.warn("Testing registerDriver_emailAlreadyExists");

        when(userRepository.existsByUserEmail(driverRequest.getUserEmail())).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class,
                () -> authService.registerDriver(driverRequest));

        verify(driverProfileRepository, never()).save(any());
    }

    // ------------------ Login Tests ------------------

    @Test
    @DisplayName("Login successfully with valid credentials")
    void login_success() {

        log.info("Testing login_success");

        when(userRepository.findByUserEmail(loginRequest.getUserEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password", "encodedPassword"))
                .thenReturn(true);

        when(jwtTokenProvider.generateToken(anyString(), anyString()))
                .thenReturn("jwtToken");

        AuthResponse response = authService.login(loginRequest);

        log.info("Login response: {}", response);

        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());
        assertEquals(user.getUserId(), response.getUserId());

        verify(jwtTokenProvider).generateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Throw exception when user not found during login")
    void login_userNotFound() {

        log.error("Testing login_userNotFound");

        when(userRepository.findByUserEmail(loginRequest.getUserEmail()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("Throw exception when password is invalid")
    void login_invalidPassword() {

        log.warn("Testing login_invalidPassword");

        when(userRepository.findByUserEmail(loginRequest.getUserEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password", "encodedPassword"))
                .thenReturn(false);

        assertThrows(UnauthorizedException.class,
                () -> authService.login(loginRequest));
    }

}