package com.example.retailer_service.client;

import com.example.retailer_service.config.FeignConfig;
import com.example.retailer_service.dto.UserResponse;

import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "user-service",
        url = "http://user-service:8081",
        configuration = FeignConfig.class
)
public interface UserClient {

    @GetMapping("/users/me")
    UserResponse getCurrentUser();
}