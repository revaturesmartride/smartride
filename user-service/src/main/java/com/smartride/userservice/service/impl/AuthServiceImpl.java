package com.smartride.userservice.service.impl;
import com.smartride.userservice.dto.request.DriverRegisterRequest;
import com.smartride.userservice.dto.request.LoginRequest;
import com.smartride.userservice.dto.request.RiderRegisterRequest;
import com.smartride.userservice.dto.response.AuthResponse;
import com.smartride.userservice.exception.ResourceAlreadyExistsException;
import com.smartride.userservice.exception.ResourceNotFoundException;
import com.smartride.userservice.exception.UnauthorizedException;
import com.smartride.userservice.mapper.DriverMapper;
import com.smartride.userservice.model.*;
import com.smartride.userservice.repository.DriverProfileRepository;
import com.smartride.userservice.repository.UserRepository;
import com.smartride.userservice.security.JwtTokenProvider;
import com.smartride.userservice.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    /*
    --------------this method is for rider registration
    */
    @Override
    @Transactional
    public AuthResponse registerRider(RiderRegisterRequest request) {
        log.info("Registering rider with email: {}", request.getUserEmail());

        checkEmailExists(request.getUserEmail());

        UserEntity user = createUser(
                request.getUserName(),
                request.getUserEmail(),
                request.getUserPassword(),
                request.getUserPhone(),
                UserRole.RIDER
        );

        log.info("Rider registered successfully with ID: {}", user.getUserId());

        return buildAuthResponse(user);
    }
    /*
    --------------this method is for driver registration
    */

    @Override
    @Transactional
    public AuthResponse registerDriver(DriverRegisterRequest request) {

        log.info("Registering driver with email: {}", request.getUserEmail());

        checkEmailExists(request.getUserEmail());
        // Create UserEntity using builder

        UserEntity user = createUser(
                request.getUserName(),
                request.getUserEmail(),
                request.getUserPassword(),
                request.getUserPhone(),
                UserRole.DRIVER
        );
        // We are using  DriverProfile using builder

        DriverProfile driverProfile = DriverProfile.builder()
                .user(user)
                .licenceNumber(request.getLicenceNumber())
                .vehicleId(request.getVehicleId())
                .rating(0.0)
                .totalRides(0)
                .availabilityStatus(DriverAvailabilityStatus.OFFLINE)
                .approvalStatus(DriverApprovalStatus.PENDING)
                .build();

        driverProfileRepository.save(driverProfile);

        log.info("Driver registered successfully with ID: {}", user.getUserId());

        return buildAuthResponse(user);
    }
    /*
    ------for login-----
     */

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getUserEmail());

        UserEntity user = userRepository.findByUserEmail(request.getUserEmail())
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", request.getUserEmail());
                    return new ResourceNotFoundException("User not found");
                });

        if (!passwordEncoder.matches(request.getUserPassword(), user.getUserPassword())) {
            log.error("Invalid password for email: {}", request.getUserEmail());
            throw new UnauthorizedException("Invalid credentials");
        }

        log.info("Login successful for user ID: {}", user.getUserId());

        return buildAuthResponse(user);
    }
    /*
    -----these are the helper methods that are helpful for above methods
     */
    private void checkEmailExists(String email) {
        if (userRepository.existsByUserEmail(email)) {
            log.error("Email already exists: {}", email);
            throw new ResourceAlreadyExistsException("Email already registered");
        }
    }
    private UserEntity createUser(
            String userName, String userEmail, String userPassword, String userPhone, UserRole userRole
    ) {
        log.debug("Creating user with email: {}", userEmail);

        UserEntity user = UserEntity.builder()
                .userName(userName)
                .userEmail(userEmail)
                .userPassword(passwordEncoder.encode(userPassword))
                .userPhone(userPhone)
                .userRole(userRole)
                .status(UserStatus.ACTIVE)
                .build();

        return userRepository.save(user);
    }
    private AuthResponse buildAuthResponse(UserEntity user) {

        log.debug("Generating token for user ID: {}", user.getUserId());

        String token = jwtTokenProvider.generateToken(
                user.getUserEmail(),
                user.getUserRole().name()
        );

        return AuthResponse.builder()
                .token(token)
                .role(user.getUserRole())
                .userId(user.getUserId())
                .build();  }
}
