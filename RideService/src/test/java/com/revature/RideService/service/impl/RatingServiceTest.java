package com.revature.RideService.service.impl;

import com.revature.RideService.entity.RideRating;
import com.revature.RideService.entity.Ride;
import com.revature.RideService.repository.RideRatingRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private RideRatingRepository ratingRepository;

    @InjectMocks
    private RatingServiceImpl ratingService;

    private RideRating rating1;
    private RideRating rating2;

    @BeforeEach
    void setup() {
        rating1 = RideRating.builder()
                .id(1L)
                .driverId(20L)
                .rating(4)
                .comment("Good")
                .ride(Ride.builder().id(1L).build())
                .build();

        rating2 = RideRating.builder()
                .id(2L)
                .driverId(20L)
                .rating(5)
                .comment("Excellent")
                .ride(Ride.builder().id(2L).build())
                .build();
    }

    // Submit Rating
    @Test
    void testSubmitRating_success() {

        when(ratingRepository.findByRide_Id(1L)).thenReturn(List.of());
        when(ratingRepository.save(any(RideRating.class))).thenReturn(rating1);

        RideRating result = ratingService.submitRating(rating1);

        assertNotNull(result);
        assertEquals(4, result.getRating());
        verify(ratingRepository, times(1)).save(rating1);
    }

    // Get Driver Ratings
    @Test
    void testGetDriverRatings() {

        when(ratingRepository.findByDriverId(20L))
                .thenReturn(List.of(rating1, rating2));

        List<RideRating> ratings = ratingService.getDriverRatings(20L);

        assertEquals(2, ratings.size());
        verify(ratingRepository, times(1)).findByDriverId(20L);
    }

    // Get Ratings by Ride ID
    @Test
    void testGetRatingsByRideId() {

        when(ratingRepository.findByRide_Id(1L))
                .thenReturn(List.of(rating1));

        List<RideRating> ratings = ratingService.getRatingsByRideId(1L);

        assertEquals(1, ratings.size());
        verify(ratingRepository, times(1)).findByRide_Id(1L);
    }

    // Get Average Rating
    @Test
    void testGetAverageDriverRating() {

        when(ratingRepository.findByDriverId(20L))
                .thenReturn(List.of(rating1, rating2));

        double avg = ratingService.getAverageDriverRating(20L);

        assertEquals(4.5, avg);
    }

    // Average Rating When No Ratings Exist
    @Test
    void testGetAverageDriverRating_noRatings() {

        when(ratingRepository.findByDriverId(20L))
                .thenReturn(List.of());

        double avg = ratingService.getAverageDriverRating(20L);

        assertEquals(0.0, avg);
    }
}