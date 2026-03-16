package com.revature.RideService.service.impl;

import com.revature.RideService.entity.RideRating;
import com.revature.RideService.repository.RideRatingRepository;
import com.revature.RideService.service.RatingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RatingServiceImpl implements RatingService {

    private final RideRatingRepository ratingRepository;

    public RatingServiceImpl(RideRatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    @Override
    public RideRating submitRating(RideRating rating) {
        return ratingRepository.save(rating);
    }

    @Override
    public List<RideRating> getDriverRatings(Long driverId) {
        return ratingRepository.findByDriverId(driverId);
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