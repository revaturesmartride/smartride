package com.revature.RideService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.RideService.entity.RideRating;
import com.revature.RideService.service.RatingService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@WebMvcTest(RatingController.class)
@AutoConfigureMockMvc
class RatingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RatingService ratingService;

    @Autowired
    private ObjectMapper objectMapper;

    // Submit Rating
    @Test
    void testSubmitRating_success() throws Exception {

        RideRating rating = RideRating.builder()
                .id(1L)
                .riderId(10L)
                .driverId(20L)
                .rating(5)
                .comment("Great ride")
                .build();

        when(ratingService.submitRating(any(RideRating.class))).thenReturn(rating);

        mockMvc.perform(post("/api/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rating)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(5));
    }

    //  Get Ratings by Driver
    @Test
    void testGetDriverRatings() throws Exception {

        RideRating rating = RideRating.builder()
                .driverId(20L)
                .rating(4)
                .build();

        when(ratingService.getDriverRatings(20L)).thenReturn(List.of(rating));

        mockMvc.perform(get("/api/ratings/driver/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].driverId").value(20L));
    }

    // Get Ratings by Ride ID
    @Test
    void testGetRatingsByRideId() throws Exception {

        RideRating rating = RideRating.builder()
                .ride(null) // not required for test
                .rating(5)
                .build();

        when(ratingService.getRatingsByRideId(1L)).thenReturn(List.of(rating));

        mockMvc.perform(get("/api/ratings/ride/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rating").value(5));
    }

    // Get Average Driver Rating
    @Test
    void testGetAverageDriverRating() throws Exception {

        when(ratingService.getAverageDriverRating(20L)).thenReturn(4.5);

        mockMvc.perform(get("/api/ratings/driver/20/average"))
                .andExpect(status().isOk())
                .andExpect(content().string("4.5"));
    }
}