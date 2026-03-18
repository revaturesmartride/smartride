package com.revature.ApiGateway.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Reactive WebClient that calls UserService to resolve email → userId.
 *
 * Why needed:
 *   JWT subject is userEmail (not userId).
 *   Downstream services (RideService etc.) need userId via X-User-Id header.
 *   Gateway calls this once per request to resolve the id.
 *
 * Requires UserService to expose:
 *   GET /api/users/email/{email}  → { "userId": 42 }
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${userservice.url}")
    private String userServiceUrl;

    public Mono<Long> getUserIdByEmail(String email) {
        return webClientBuilder
                .baseUrl(userServiceUrl)
                .build()
                .get()
                .uri("/api/users/email/{email}", email)
                .retrieve()
                .bodyToMono(UserIdResponse.class)
                .map(UserIdResponse::getUserId)
                .doOnError(e ->
                        log.error("Failed to resolve userId for email [{}]: {}",
                                email, e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserIdResponse {
        private Long userId;
    }
}