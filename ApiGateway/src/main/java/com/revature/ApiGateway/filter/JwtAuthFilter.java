package com.revature.ApiGateway.filter;

import com.revature.ApiGateway.client.UserServiceClient;
import com.revature.ApiGateway.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * JWT Authentication Filter.
 *
 * Applied to protected routes via application.properties:
 *   spring.cloud.gateway.routes[n].filters[0]=JwtAuthFilter
 *
 * Flow per request:
 *   1. Read Authorization: Bearer <token>
 *   2. Validate JWT          → invalid? 401
 *   3. Extract email + role
 *   4. Call UserService      → resolve email → userId
 *   5. Mutate request        → add X-User-Id, X-User-Role, X-User-Email
 *   6. Forward to downstream service
 *
 * Downstream services read:
 *   @RequestHeader("X-User-Id")    Long userId
 *   @RequestHeader("X-User-Role")  String role
 *
 * Root cause of original bug:
 *   switchIfEmpty() was chained AFTER flatMap(chain.filter()).
 *   chain.filter() returns Mono<Void> which is ALWAYS empty by definition.
 *   So switchIfEmpty always fired → always returned 401 even on success.
 *
 * Fix:
 *   switchIfEmpty() is now chained BEFORE flatMap, scoping it only to
 *   the userServiceClient lookup. If the user is not found, it returns 401.
 *   If found, flatMap runs, headers are mutated, request is forwarded.
 *   chain.filter()'s Mono<Void> is no longer watched by switchIfEmpty.
 */
@Component
@Slf4j
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserServiceClient userServiceClient;

    public JwtAuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            String path = exchange.getRequest().getURI().getPath();
            log.info("JwtAuthFilter intercepted: {}", path);

            // ── Step 1: Read Authorization header ────────────────────────────
            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing Authorization header → {}", path);
                return unauthorized(exchange);
            }

            String token = authHeader.substring(7);

            // ── Step 2: Validate JWT ──────────────────────────────────────────
            if (!jwtUtil.validateToken(token)) {
                log.warn("Invalid or expired JWT → {}", path);
                return unauthorized(exchange);
            }

            // ── Step 3: Extract claims ────────────────────────────────────────
            String email = jwtUtil.getEmailFromToken(token);
            String role  = jwtUtil.getRoleFromToken(token);
            log.info("JWT valid | email={} role={}", email, role);

            // ── Step 4 + 5: Resolve userId → mutate headers → forward ─────────
            //
            // IMPORTANT: switchIfEmpty must be placed BEFORE flatMap.
            // chain.filter() returns Mono<Void> which is always empty —
            // placing switchIfEmpty after flatMap caused it to trigger on
            // every successful request, returning 401 unconditionally.
            //
            return userServiceClient.getUserIdByEmail(email)
                    .switchIfEmpty(Mono.defer(() -> {
                        log.error("Could not resolve userId for email: {}", email);
                        return unauthorized(exchange).then(Mono.empty());
                    }))
                    .flatMap(userId -> {
                        log.info("Resolved userId={} for email={}", userId, email);

                        ServerWebExchange mutatedExchange = exchange.mutate()
                                .request(r -> r
                                        .header("X-User-Id",    String.valueOf(userId))
                                        .header("X-User-Role",  role)
                                        .header("X-User-Email", email)
                                )
                                .build();

                        return chain.filter(mutatedExchange);
                    })
                    .onErrorResume(e -> {
                        log.error("JwtAuthFilter unexpected error for path={} : {}",
                                path, e.getMessage());
                        return unauthorized(exchange);
                    });
        };
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        // no per-route config needed
    }
}