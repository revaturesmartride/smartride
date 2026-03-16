package com.revature.RideService.service.impl;

import com.revature.RideService.entity.RideRating;
import com.revature.RideService.exception.RideNotFoundException;
import com.revature.RideService.repository.RideRatingRepository;
import com.revature.RideService.service.RatingService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RatingServiceImpl implements RatingService {

    private final RideRatingRepository ratingRepository;

    public RatingServiceImpl(RideRatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    @Override
    @Transactional
    public RideRating submitRating(RideRating rating) {

        // validate rating range
        if (rating.getRating() < 1 || rating.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        // prevent duplicate rating for same ride
        if (ratingRepository.findByRide_Id(rating.getRide().getId()).size() > 0) {
            throw new RuntimeException("Ride already rated");
        }

        return ratingRepository.save(rating);
    }

    @Override
    public List<RideRating> getDriverRatings(Long driverId) {

        List<RideRating> ratings = ratingRepository.findByDriverId(driverId);

        if (ratings.isEmpty()) {
            throw new RideNotFoundException("No ratings found for driver: " + driverId);
        }

        return ratings;
    }

    @Override
    public List<RideRating> getRatingsByRideId(Long rideId) {

        return ratingRepository.findByRide_Id(rideId);
    }

    @Override
    public double getAverageDriverRating(Long driverId) {

        List<RideRating> ratings = ratingRepository.findByDriverId(driverId);

        if (ratings.isEmpty()) {
            return 0.0;
        }

        double sum = ratings.stream()
                .mapToInt(RideRating::getRating)
                .sum();

        return sum / ratings.size();
    }
}