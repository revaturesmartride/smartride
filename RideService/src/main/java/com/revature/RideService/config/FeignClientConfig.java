package com.revature.RideService.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.RequestContextListener;

@Configuration
public class FeignClientConfig {

    @Bean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }

    @Bean
    public RequestInterceptor forwardAuthorizationHeader() {
        return (RequestTemplate template) -> {
            // Don't forward Authorization headers for internal service-to-service calls
            // UserService endpoints are already configured to allow internal access
        };
    }
}