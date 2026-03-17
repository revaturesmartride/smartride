package com.revature.RideService.kafka.event;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Published when a rider submits a rating for a completed ride.
 * Consumers: UserService (update driver's average rating),
 *            NotificationService (optional — thank rider for rating)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideRatedEvent {


    private Long ratingId;          // RideRating.id (PK of ride_ratings table)
    private Long rideId;            // the ride being rated
    private Long riderId;           // who submitted the rating
    private Long driverId;          // who is being rated


    private Integer rating;         // RideRating.rating  — value between 1 and 5
    private String comment;         // RideRating.comment — optional free text


    private LocalDateTime ratedAt;  // RideRating.createdAt
}