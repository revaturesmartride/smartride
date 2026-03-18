package com.revature.RideService.service.impl;


import com.revature.RideService.client.UserServiceClient;
import com.revature.RideService.dto.request.RideRequestDTO;
import com.revature.RideService.dto.response.RideResponseDTO;
import com.revature.RideService.dto.response.UserResponse;
import com.revature.RideService.entity.Ride;
import com.revature.RideService.entity.RideRequest;
import com.revature.RideService.entity.RideStatus;
import com.revature.RideService.exception.RideNotFoundException;
import com.revature.RideService.kafka.event.RideAssignedEvent;
import com.revature.RideService.kafka.event.RideCompletedEvent;
import com.revature.RideService.kafka.event.RideCreatedEvent;
import com.revature.RideService.kafka.event.RideStartedEvent;
import com.revature.RideService.kafka.producer.RideEventProducer;
import com.revature.RideService.repository.RideRepository;
import com.revature.RideService.repository.RideRequestRepository;
import com.revature.RideService.service.RideService;
import com.revature.RideService.util.FareCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideServiceImpl implements RideService {

    private final RideRepository rideRepository;
    private final RideRequestRepository rideRequestRepository;
    private final RideEventProducer rideEventProducer;
    private final FareCalculator fareCalculator;
    private final UserServiceClient userServiceClient;


    @Override
    @Transactional
    public RideResponseDTO requestRide(RideRequestDTO dto) {
        log.info("Rider [{}] requesting ride from '{}' to '{}'",
                dto.getRiderId(), dto.getPickupLocation(), dto.getDropLocation());

        // Validate rider exists in UserService
        UserResponse rider = userServiceClient.getUserById(dto.getRiderId());
        if (rider == null) {
            throw new RideNotFoundException(
                    "Rider not found with id: " + dto.getRiderId());
        }

        // Validate role is RIDER
        if (!"RIDER".equalsIgnoreCase(rider.getUserRole())) {
            throw new IllegalStateException(
                    "User [" + dto.getRiderId() + "] is not a RIDER");
        }

        // --- Validation ---
        if (dto.getRiderId() == null) {
            log.error("Ride request failed: riderId is null");
            throw new IllegalArgumentException("Rider ID must not be null");
        }
        if (dto.getPickupLocation() == null || dto.getPickupLocation().isBlank()) {
            throw new IllegalArgumentException("Pickup location must not be blank");
        }
        if (dto.getDropLocation() == null || dto.getDropLocation().isBlank()) {
            throw new IllegalArgumentException("Drop location must not be blank");
        }


        RideRequest rideRequest = RideRequest.builder()
                .riderId(dto.getRiderId())
                .pickupLocation(dto.getPickupLocation())
                .dropLocation(dto.getDropLocation())
                .status("PENDING")
                .requestedTime(LocalDateTime.now())
                .build();

        RideRequest savedRequest = rideRequestRepository.save(rideRequest);
        log.info("RideRequest [{}] persisted for riderId [{}]",
                savedRequest.getId(), savedRequest.getRiderId());

        // --- Kafka Event ---
        rideEventProducer.publishRideCreated(
                RideCreatedEvent.builder()
                        .rideRequestId(savedRequest.getId())
                        .riderId(savedRequest.getRiderId())
                        .pickupLocation(savedRequest.getPickupLocation())
                        .dropLocation(savedRequest.getDropLocation())
                        .requestedTime(savedRequest.getRequestedTime())
                        .status(savedRequest.getStatus())
                        .build()
        );

        // --- Response (no Ride row yet — only RideRequest exists at this stage) ---
        return RideResponseDTO.builder()
                .riderId(savedRequest.getRiderId())
                .pickupLocation(savedRequest.getPickupLocation())
                .dropLocation(savedRequest.getDropLocation())
                .status(savedRequest.getStatus())
                .requestedTime(savedRequest.getRequestedTime())
                .build();
    }

    @Override
    @Transactional
    public RideResponseDTO acceptRide(Long rideRequestId, Long driverId) {
        log.info("Driver [{}] accepting rideRequestId [{}]", driverId, rideRequestId);
        // Validate driver exists in UserService
        UserResponse driver = userServiceClient.getUserById(driverId);
        if (driver == null) {
            throw new RideNotFoundException(
                    "Driver not found with id: " + driverId);
        }

        if (!"DRIVER".equalsIgnoreCase(driver.getUserRole())) {
            throw new IllegalStateException(
                    "User [" + driverId + "] is not a DRIVER");
        }

        // --- Load RideRequest ---
        RideRequest rideRequest = rideRequestRepository.findById(rideRequestId)
                .orElseThrow(() -> new RideNotFoundException(
                        "RideRequest not found with id: " + rideRequestId));

        // --- Guard: must be PENDING ---
        if (!"PENDING".equalsIgnoreCase(rideRequest.getStatus())) {
            throw new IllegalStateException(
                    "Cannot accept ride — current status: " + rideRequest.getStatus());
        }

        // --- Update RideRequest status ---
        rideRequest.setStatus("ACCEPTED");
        rideRequestRepository.save(rideRequest);

        // --- Create Ride row ---
        // Ride.rideRequest is @OneToOne — set the whole object, not an id field
        Ride ride = Ride.builder()
                .riderId(rideRequest.getRiderId())
                .driverId(driverId)
                .pickupLocation(rideRequest.getPickupLocation())
                .dropLocation(rideRequest.getDropLocation())
                .status(RideStatus.ACCEPTED)
                .requestedTime(rideRequest.getRequestedTime())
                .rideRequest(rideRequest)   // @OneToOne FK
                .build();

        Ride savedRide = rideRepository.save(ride);
        log.info("Ride [{}] created and assigned to driver [{}]",
                savedRide.getId(), driverId);

        // --- Kafka Event ---
        rideEventProducer.publishRideAssigned(
                RideAssignedEvent.builder()
                        .rideId(savedRide.getId())
                        .rideRequestId(rideRequest.getId())
                        .riderId(savedRide.getRiderId())
                        .driverId(driverId)
                        .pickupLocation(savedRide.getPickupLocation())
                        .dropLocation(savedRide.getDropLocation())
                        .assignedAt(LocalDateTime.now())
                        .build()
        );

        return toResponseDTO(savedRide);
    }


    @Override
    @Transactional
    public RideResponseDTO startRide(Long rideId) {
        log.info("Starting rideId [{}]", rideId);

        Ride ride = findRideOrThrow(rideId);
        assertStatus(ride, RideStatus.ACCEPTED, "start");

        ride.setStatus(RideStatus.STARTED);
        ride.setStartTime(LocalDateTime.now());
        Ride savedRide = rideRepository.save(ride);

        rideEventProducer.publishRideStarted(
                RideStartedEvent.builder()
                        .rideId(savedRide.getId())
                        .riderId(savedRide.getRiderId())
                        .driverId(savedRide.getDriverId())
                        .startTime(savedRide.getStartTime())
                        .build()
        );

        log.info("Ride [{}] is now IN_PROGRESS since {}", rideId, savedRide.getStartTime());
        return toResponseDTO(savedRide);
    }


    @Override
    @Transactional
    public RideResponseDTO completeRide(Long rideId) {
        log.info("Completing rideId [{}]", rideId);

        Ride ride = findRideOrThrow(rideId);
        assertStatus(ride, RideStatus.STARTED, "complete");

        // FareCalculator stub — replace with real geo logic later
        double fare = fareCalculator.calculate(
                ride.getPickupLocation(), ride.getDropLocation());

        ride.setStatus(RideStatus.COMPLETED);
        ride.setEndTime(LocalDateTime.now());
        ride.setFare(fare);
        Ride savedRide = rideRepository.save(ride);

        rideEventProducer.publishRideCompleted(
                RideCompletedEvent.builder()
                        .rideId(savedRide.getId())
                        .riderId(savedRide.getRiderId())
                        .driverId(savedRide.getDriverId())
                        .fare(savedRide.getFare())
                        .distance(savedRide.getDistance())
                        .endTime(savedRide.getEndTime())
                        .build()
        );

        log.info("Ride [{}] COMPLETED | fare={}", rideId, fare);
        return toResponseDTO(savedRide);
    }


    @Override
    @Transactional(readOnly = true)
    public RideResponseDTO getRide(Long rideId) {
        log.info("Fetching rideId [{}]", rideId);
        return toResponseDTO(findRideOrThrow(rideId));
    }


    @Override
    @Transactional(readOnly = true)
    public List<RideResponseDTO> getRidesByRider(Long riderId) {
        log.info("Fetching rides for riderId [{}]", riderId);
        return rideRepository.findByRiderId(riderId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public List<RideResponseDTO> getRidesByDriver(Long driverId) {
        log.info("Fetching rides for driverId [{}]", driverId);
        return rideRepository.findByDriverId(driverId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }


    /**
     * Loads a Ride by id or throws RideNotFoundException.
     */
    private Ride findRideOrThrow(Long rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException(
                        "Ride not found with id: " + rideId));
    }

    /**
     * Guards illegal state transitions.
     * Throws IllegalStateException if ride.status != required.
     */
    private void assertStatus(Ride ride, RideStatus required, String action) {
        if (ride.getStatus() != required) {
            throw new IllegalStateException(String.format(
                    "Cannot %s ride [%d] — required: %s, current: %s",
                    action, ride.getId(), required, ride.getStatus()));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
// CANCEL RIDE
// Can be called by rider or driver before ride is COMPLETED.
// Guards against cancelling an already COMPLETED or CANCELLED ride.
// Publishes a RideCancelledEvent to Kafka.
// ─────────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public RideResponseDTO cancelRide(Long rideId) {
        log.info("Cancelling rideId [{}]", rideId);

        Ride ride = findRideOrThrow(rideId);

        // Guard: cannot cancel a ride that is already finished
        if (ride.getStatus() == RideStatus.COMPLETED ||
                ride.getStatus() == RideStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Cannot cancel ride [" + rideId + "] — current status: " + ride.getStatus());
        }

        ride.setStatus(RideStatus.CANCELLED);
        Ride savedRide = rideRepository.save(ride);
        log.info("Ride [{}] successfully CANCELLED", rideId);

        // Also mark the linked RideRequest as CANCELLED
        RideRequest rideRequest = savedRide.getRideRequest();
        if (rideRequest != null) {
            rideRequest.setStatus("CANCELLED");
            rideRequestRepository.save(rideRequest);
        }

        return toResponseDTO(savedRide);
    }
    private RideResponseDTO toResponseDTO(Ride ride) {
        return RideResponseDTO.builder()
                .rideId(ride.getId())
                .riderId(ride.getRiderId())
                .driverId(ride.getDriverId())
                .pickupLocation(ride.getPickupLocation())
                .dropLocation(ride.getDropLocation())
                .distance(ride.getDistance())
                .fare(ride.getFare())
                .status(ride.getStatus() != null
                        ? ride.getStatus().name() : null)
                .requestedTime(ride.getRequestedTime())
                .startTime(ride.getStartTime())
                .endTime(ride.getEndTime())
                .build();
    }
}