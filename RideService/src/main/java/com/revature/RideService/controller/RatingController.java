package com.revature.RideService.controller;

import com.revature.RideService.entity.RideRating;
import com.revature.RideService.service.RatingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    // Submit rating for a completed ride
    @PostMapping
    public ResponseEntity<RideRating> submitRating(@RequestBody RideRating rating) {

        RideRating savedRating = ratingService.submitRating(rating);

        return ResponseEntity.ok(savedRating);
    }

    // Get all ratings received by a driver
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<RideRating>> getDriverRatings(@PathVariable Long driverId) {

        List<RideRating> ratings = ratingService.getDriverRatings(driverId);

        return ResponseEntity.ok(ratings);
    }

    // Get rating for a specific ride
    @GetMapping("/ride/{rideId}")
    public ResponseEntity<List<RideRating>> getRatingsByRideId(@PathVariable Long rideId) {

        List<RideRating> ratings = ratingService.getRatingsByRideId(rideId);

        return ResponseEntity.ok(ratings);
    }

    // Get average rating of driver
    @GetMapping("/driver/{driverId}/average")
    public ResponseEntity<Double> getAverageDriverRating(@PathVariable Long driverId) {

        double avgRating = ratingService.getAverageDriverRating(driverId);

        return ResponseEntity.ok(avgRating);
    }
}