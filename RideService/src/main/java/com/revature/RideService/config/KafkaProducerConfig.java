package com.revature.RideService.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
//import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // ── Topic names (same @Value keys used in RideEventProducer) ─────────────
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
    // PRODUCER FACTORY
    // Builds the underlying Kafka producer with all connection +
    // reliability + serialization settings.
    // =========================================================================
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();

        // ── Connection ────────────────────────────────────────────────────────
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // ── Serializers ───────────────────────────────────────────────────────
        // Key   → plain String  (we use entity ids as keys)
        // Value → JSON via Jackson (handles all 6 event POJOs automatically)
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class);

        // Embed the fully-qualified class name as a Kafka header on every message.
        // Consumers can use this header to deserialize back to the correct class.
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);

        // ── Reliability ───────────────────────────────────────────────────────
        // acks=all  → producer waits for leader + all in-sync replicas to ack.
        // Strongest durability guarantee — no data loss on broker failure.
        config.put(ProducerConfig.ACKS_CONFIG, "all");

        // Retry up to 3 times on transient errors (network blip, leader election)
        config.put(ProducerConfig.RETRIES_CONFIG, 3);

        // Retry backoff — wait 1 second between retry attempts
        config.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);

        // Keep in-flight requests to 1 per connection while retrying.
        // Prevents out-of-order messages when retries are in play.
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);

        // ── Performance ───────────────────────────────────────────────────────
        // Accumulate up to 16 KB in a batch before sending to the broker.
        // Reduces network round trips without noticeable latency increase.
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);

        // Wait up to 1 ms to fill a batch before flushing.
        // linger.ms=0 sends immediately; 1 ms gives a small batching window.
        config.put(ProducerConfig.LINGER_MS_CONFIG, 1);

        // 32 MB producer send buffer — handles burst traffic without blocking
        config.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);

        // Max time the producer will block on send() if the buffer is full.
        // After 30 s it throws a TimeoutException instead of blocking forever.
        config.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 30000);

        // Request timeout — how long to wait for a broker response per attempt
        config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);

        return new DefaultKafkaProducerFactory<>(config);
    }

    // =========================================================================
    // KAFKA TEMPLATE
    // The single bean injected into RideEventProducer.
    // Wraps ProducerFactory and exposes the send() API.
    // =========================================================================
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // =========================================================================
    // TOPIC AUTO-CREATION
    // Spring will create these topics on startup if they don't already exist.
    // Partition count = 3  → allows 3 parallel consumers per topic
    // Replication factor = 1 → fine for local/dev; set to 3 in production
    // =========================================================================

    @Bean
    public NewTopic rideCreatedTopic() {
        return new NewTopic(rideCreatedTopic, 3, (short) 1);
    }

    @Bean
    public NewTopic rideAssignedTopic() {
        return new NewTopic(rideAssignedTopic, 3, (short) 1);
    }

    @Bean
    public NewTopic rideStartedTopic() {
        return new NewTopic(rideStartedTopic, 3, (short) 1);
    }

    @Bean
    public NewTopic rideCompletedTopic() {
        return new NewTopic(rideCompletedTopic, 3, (short) 1);
    }

    @Bean
    public NewTopic paymentCompletedTopic() {
        return new NewTopic(paymentCompletedTopic, 3, (short) 1);
    }

    @Bean
    public NewTopic rideRatedTopic() {
        return new NewTopic(rideRatedTopic, 3, (short) 1);
    }
}