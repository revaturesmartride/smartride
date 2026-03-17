package com.revature.RideService.service;

import com.revature.RideService.entity.RideRating;

import java.util.List;

public interface RatingService {

    // Submit rating after ride completion
    RideRating submitRating(RideRating rating);

    // Get all ratings of a driver
    List<RideRating> getDriverRatings(Long driverId);

    // Get rating for a ride
    List<RideRating> getRatingsByRideId(Long rideId);

    // Get average rating of driver
    double getAverageDriverRating(Long driverId);
}