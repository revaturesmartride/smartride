package com.revature.RideService.service.impl;


import com.revature.RideService.dto.request.RideRequestDTO;
import com.revature.RideService.dto.response.RideResponseDTO;
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

    // ─────────────────────────────────────────────────────────────────────────
    // 1. REQUEST RIDE
    //    - Validates incoming DTO
    //    - Persists a RideRequest (status = "PENDING")
    //    - Publishes RideCreatedEvent to Kafka
    //    - Returns RideResponseDTO built from the saved RideRequest
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public RideResponseDTO requestRide(RideRequestDTO dto) {
        log.info("Rider [{}] requesting ride from '{}' to '{}'",
                dto.getRiderId(), dto.getPickupLocation(), dto.getDropLocation());

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

        // --- Persist RideRequest ---
        // RideRequest.status is a plain String field (see entity)
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

    // ─────────────────────────────────────────────────────────────────────────
    // 2. ACCEPT RIDE
    //    - Driver picks a PENDING RideRequest
    //    - RideRequest.status → "ACCEPTED"
    //    - Creates the Ride row, linking back to RideRequest via @OneToOne
    //    - Publishes RideAssignedEvent
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public RideResponseDTO acceptRide(Long rideRequestId, Long driverId) {
        log.info("Driver [{}] accepting rideRequestId [{}]", driverId, rideRequestId);

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

    // ─────────────────────────────────────────────────────────────────────────
    // 3. START RIDE
    //    - Ride must be in ACCEPTED state
    //    - Sets status → IN_PROGRESS, stamps startTime
    //    - Publishes RideStartedEvent
    // ─────────────────────────────────────────────────────────────────────────
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

    // ─────────────────────────────────────────────────────────────────────────
    // 4. COMPLETE RIDE
    //    - Ride must be IN_PROGRESS
    //    - FareCalculator computes fare from pickup/drop strings
    //    - Sets status → COMPLETED, stamps endTime, persists fare
    //    - Publishes RideCompletedEvent
    // ─────────────────────────────────────────────────────────────────────────
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

    // ─────────────────────────────────────────────────────────────────────────
    // 5. GET RIDE BY ID
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public RideResponseDTO getRide(Long rideId) {
        log.info("Fetching rideId [{}]", rideId);
        return toResponseDTO(findRideOrThrow(rideId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. GET ALL RIDES BY RIDER
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<RideResponseDTO> getRidesByRider(Long riderId) {
        log.info("Fetching rides for riderId [{}]", riderId);
        return rideRepository.findByRiderId(riderId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. GET ALL RIDES BY DRIVER
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<RideResponseDTO> getRidesByDriver(Long driverId) {
        log.info("Fetching rides for driverId [{}]", driverId);
        return rideRepository.findByDriverId(driverId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

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

    /**
     * Manual mapper: Ride entity → RideResponseDTO.
     *
     * NOTE: RideResponseDTO.rideId maps from Ride.id  (entity PK is 'id',
     * DTO field is 'rideId' — see your RideResponseDTO.java).
     * RideRequest id is read via ride.getRideRequest().getId().
     */
    private RideResponseDTO toResponseDTO(Ride ride) {
        return RideResponseDTO.builder()
                .rideId(ride.getId())                           // DTO: rideId  ← entity: id
                .riderId(ride.getRiderId())
                .driverId(ride.getDriverId())
                .pickupLocation(ride.getPickupLocation())
                .dropLocation(ride.getDropLocation())
                .distance(ride.getDistance())
                .fare(ride.getFare())
                .status(ride.getStatus() != null              // RideStatus enum → String
                        ? ride.getStatus().name() : null)
                .requestedTime(ride.getRequestedTime())
                .startTime(ride.getStartTime())
                .endTime(ride.getEndTime())
                .build();
    }
}