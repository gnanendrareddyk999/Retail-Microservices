package com.example.user_service.controller;

import com.example.user_service.dto.UserResponse;
import com.example.user_service.service.UserService;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;


    public UserController(
            UserService userService) {

        this.userService = userService;
    }


    // =====================================================
    // GET CURRENT LOGGED-IN USER
    // =====================================================

    @GetMapping("/me")
    public UserResponse getCurrentUser(
            Authentication authentication) {


        // JWT నుంచి username

        String username =
                authentication.getName();


        // JWT నుంచి role

        String role =
                authentication
                        .getAuthorities()
                        .stream()
                        .findFirst()
                        .map(authority ->
                                authority
                                        .getAuthority()
                                        .replace(
                                                "ROLE_",
                                                ""
                                        )
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Role not found in JWT"
                                )
                        );


        return userService.getCurrentUser(
                username,
                role
        );
    }
}