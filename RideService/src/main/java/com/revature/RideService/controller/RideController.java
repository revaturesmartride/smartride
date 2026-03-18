package com.revature.RideService.controller;

import com.revature.RideService.dto.request.RideRequestDTO;
import com.revature.RideService.dto.response.RideResponseDTO;
import com.revature.RideService.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for ride lifecycle management.
 *
 * User identity is NEVER taken from the request body or query params.
 * The API Gateway decodes the JWT and forwards two headers on every request:
 *
 *   X-User-Id   → authenticated user's database id  (Long)
 *   X-User-Role → authenticated user's role         (RIDER / DRIVER / ADMIN)
 *
 * Base path: /api/rides
 */
@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
@Slf4j
public class RideController {

    private final RideService rideService;

    // ─────────────────────────────────────────────────────────────────────────
    // 1. REQUEST RIDE  (RIDER)
    //    riderId injected from X-User-Id header into DTO before service call.
    //    Body carries only pickupLocation + dropLocation.
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/request")
    public ResponseEntity<RideResponseDTO> requestRide(
            @RequestHeader("X-User-Id")   Long userId,
            @RequestHeader("X-User-Role") String userRole,
            @Valid @RequestBody RideRequestDTO dto) {

        log.info("POST /api/rides/request | riderId={}", userId);
        dto.setRiderId(userId);   // set verified id from gateway header
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(rideService.requestRide(dto));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. ACCEPT RIDE  (DRIVER)
    //    driverId from X-User-Id header.
    //    rideRequestId from path — which pending request to accept.
    // ─────────────────────────────────────────────────────────────────────────
    @PutMapping("/{rideRequestId}/accept")
    public ResponseEntity<RideResponseDTO> acceptRide(
            @RequestHeader("X-User-Id")   Long driverId,
            @RequestHeader("X-User-Role") String userRole,
            @PathVariable                 Long rideRequestId) {

        log.info("PUT /api/rides/{}/accept | driverId={}", rideRequestId, driverId);
        return ResponseEntity.ok(rideService.acceptRide(rideRequestId, driverId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. START RIDE  (DRIVER)
    // ─────────────────────────────────────────────────────────────────────────
    @PutMapping("/{rideId}/start")
    public ResponseEntity<RideResponseDTO> startRide(
            @RequestHeader("X-User-Id")   Long driverId,
            @RequestHeader("X-User-Role") String userRole,
            @PathVariable                 Long rideId) {

        log.info("PUT /api/rides/{}/start | driverId={}", rideId, driverId);
        return ResponseEntity.ok(rideService.startRide(rideId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. COMPLETE RIDE  (DRIVER)
    //    Triggers fare calculation via Nominatim + OSRM.
    // ─────────────────────────────────────────────────────────────────────────
    @PutMapping("/{rideId}/complete")
    public ResponseEntity<RideResponseDTO> completeRide(
            @RequestHeader("X-User-Id")   Long driverId,
            @RequestHeader("X-User-Role") String userRole,
            @PathVariable                 Long rideId) {

        log.info("PUT /api/rides/{}/complete | driverId={}", rideId, driverId);
        return ResponseEntity.ok(rideService.completeRide(rideId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. CANCEL RIDE  (RIDER or DRIVER)
    // ─────────────────────────────────────────────────────────────────────────
    @PutMapping("/{rideId}/cancel")
    public ResponseEntity<RideResponseDTO> cancelRide(
            @RequestHeader("X-User-Id")   Long userId,
            @RequestHeader("X-User-Role") String userRole,
            @PathVariable                 Long rideId) {

        log.info("PUT /api/rides/{}/cancel | userId={} role={}", rideId, userId, userRole);
        return ResponseEntity.ok(rideService.cancelRide(rideId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. GET RIDE BY ID
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/{rideId}")
    public ResponseEntity<RideResponseDTO> getRide(
            @RequestHeader("X-User-Id")   Long userId,
            @RequestHeader("X-User-Role") String userRole,
            @PathVariable                 Long rideId) {

        log.info("GET /api/rides/{} | userId={}", rideId, userId);
        return ResponseEntity.ok(rideService.getRide(rideId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. GET MY RIDES  (RIDER)
    //    riderId taken from header — rider only sees their own rides.
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/rider/my-rides")
    public ResponseEntity<List<RideResponseDTO>> getRidesByRider(
            @RequestHeader("X-User-Id")   Long riderId,
            @RequestHeader("X-User-Role") String userRole) {

        log.info("GET /api/rides/rider/my-rides | riderId={}", riderId);
        return ResponseEntity.ok(rideService.getRidesByRider(riderId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 8. GET MY RIDES  (DRIVER)
    //    driverId taken from header — driver only sees their own rides.
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/driver/my-rides")
    public ResponseEntity<List<RideResponseDTO>> getRidesByDriver(
            @RequestHeader("X-User-Id")   Long driverId,
            @RequestHeader("X-User-Role") String userRole) {

        log.info("GET /api/rides/driver/my-rides | driverId={}", driverId);
        return ResponseEntity.ok(rideService.getRidesByDriver(driverId));
    }
}