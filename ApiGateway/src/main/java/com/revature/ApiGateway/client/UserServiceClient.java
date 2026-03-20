package com.revature.ApiGateway.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class UserServiceClient {

    private final WebClient webClient;

    /**
     * Build the WebClient ONCE using the @LoadBalanced builder.
     * baseUrl is set here at construction time — not inside the method.
     * This ensures the load-balanced builder is actually used.
     */
    public UserServiceClient(WebClient.Builder webClientBuilder,
                             @Value("${userservice.url}") String userServiceUrl) {
        this.webClient = webClientBuilder
                .baseUrl(userServiceUrl)
                .build();
    }

    public Mono<Long> getUserIdByEmail(String email) {
        return webClient
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