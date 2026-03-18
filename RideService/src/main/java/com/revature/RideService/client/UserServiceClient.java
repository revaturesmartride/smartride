package com.revature.RideService.client;


import com.revature.RideService.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "USER-SERVICE",
        url  = "${userservice.url}"
)
public interface UserServiceClient {

    @GetMapping("/api/users/{userId}")
    UserResponse getUserById(@PathVariable("userId") Long userId);
}