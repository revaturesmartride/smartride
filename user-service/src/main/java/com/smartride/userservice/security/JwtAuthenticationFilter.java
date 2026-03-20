package com.smartride.userservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/",
            "/api/users/email/",   // only the internal gateway lookup
            "/api/users/internal/"  // internal service-to-service calls
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // ── No token → reject immediately ────────────────────────────────────
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing Authorization header for: {}", request.getRequestURI());
            sendUnauthorized(response, "Missing JWT token");
            return;
        }

        String token = authHeader.substring(7);

        // ── Invalid or expired token → reject ────────────────────────────────
        if (!jwtTokenProvider.validateToken(token)) {
            log.warn("Invalid or expired JWT for: {}", request.getRequestURI());
            sendUnauthorized(response, "Invalid or expired JWT token");
            return;
        }

        try {
            String email = jwtTokenProvider.getEmailFromToken(token);
            String role  = jwtTokenProvider.getRoleFromToken(token);

            // ROLE_ prefix is required by Spring Security for hasRole() to work
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("JWT authenticated | email={} role={}", email, role);

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("JWT processing error: {}", e.getMessage());
            sendUnauthorized(response, "JWT processing error");
        }
    }

    private void sendUnauthorized(HttpServletResponse response, String message)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}");
    }
}