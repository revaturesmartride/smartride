package com.revature.RideService.kafka.producer;

import com.revature.RideService.kafka.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class RideEventProducer {

    /*
     * KafkaTemplate<String, Object>
     *   - Key   : String  → we use entity id as the message key
     *                        so all events for the same ride go to the same partition
     *                        (preserves ordering per ride)
     *   - Value : Object  → serialized to JSON by JsonSerializer (configured in
     *                        application.properties)
     */
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // ── Topic names injected from application.properties ─────────────────────
    @Value("${kafka.topics.ride-created}")
    private String rideCreatedTopic;

    @Value("${kafka.topics.ride-assigned}")
    private String rideAssignedTopic;

    @Value("${kafka.topics.ride-started}")
    private String rideStartedTopic;

    @Value("${kafka.topics.ride-completed}")
    private String rideCompletedTopic;

    @Value("${kafka.topics.payment-completed}")
    private String paymentCompletedTopic;

    @Value("${kafka.topics.ride-rated}")
    private String rideRatedTopic;

    // =========================================================================
    // PUBLIC PUBLISH METHODS
    // one method per event — called directly from service layer
    // =========================================================================

    /**
     * Fired in RideServiceImpl.requestRide()
     * Key = rideRequestId (Ride row doesn't exist yet at this point)
     */
    public void publishRideCreated(RideCreatedEvent event) {
        send(rideCreatedTopic,
                String.valueOf(event.getRideRequestId()),
                event,
                "RIDE_CREATED");
    }

    /**
     * Fired in RideServiceImpl.acceptRide()
     * Key = rideId (Ride row has just been created)
     */
    public void publishRideAssigned(RideAssignedEvent event) {
        send(rideAssignedTopic,
                String.valueOf(event.getRideId()),
                event,
                "RIDE_ASSIGNED");
    }

    /**
     * Fired in RideServiceImpl.startRide()
     * Key = rideId
     */
    public void publishRideStarted(RideStartedEvent event) {
        send(rideStartedTopic,
                String.valueOf(event.getRideId()),
                event,
                "RIDE_STARTED");
    }

    /**
     * Fired in RideServiceImpl.completeRide()
     * Key = rideId — carries fare + distance for PaymentService
     */
    public void publishRideCompleted(RideCompletedEvent event) {
        send(rideCompletedTopic,
                String.valueOf(event.getRideId()),
                event,
                "RIDE_COMPLETED");
    }

    /**
     * Fired in PaymentServiceImpl.processPayment()
     * Key = paymentId
     */
    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        send(paymentCompletedTopic,
                String.valueOf(event.getPaymentId()),
                event,
                "PAYMENT_COMPLETED");
    }

    /**
     * Fired in RatingServiceImpl.submitRating()
     * Key = ratingId
     */
    public void publishRideRated(RideRatedEvent event) {
        send(rideRatedTopic,
                String.valueOf(event.getRatingId()),
                event,
                "RIDE_RATED");
    }

    // =========================================================================
    // PRIVATE GENERIC SEND
    // =========================================================================

    /**
     * Generic send method used by all publish methods above.
     *
     * Uses CompletableFuture (Spring Kafka 3.x API) instead of the deprecated
     * ListenableFuture to handle success / failure asynchronously without
     * blocking the calling thread.
     *
     * On SUCCESS  → logs topic, partition, offset
     * On FAILURE  → logs full error (retry is handled by producer config:
     *               retries=3, max.in.flight.requests.per.connection=1)
     *
     * @param topic     destination Kafka topic
     * @param key       message key  (entity id as String)
     * @param event     the event object — serialized to JSON by KafkaTemplate
     * @param eventType human-readable label used only for logging
     */
    private void send(String topic, String key, Object event, String eventType) {
        log.info("Publishing [{}] → topic='{}' key='{}'", eventType, topic, key);

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                // Production tip: push failed events to a dead-letter topic
                // or persist to an outbox table for retry
                log.error("FAILED to publish [{}] → topic='{}' key='{}' | error: {}",
                        eventType, topic, key, ex.getMessage(), ex);
            } else {
                log.info("SUCCESS [{}] → topic='{}' partition={} offset={}",
                        eventType,
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}