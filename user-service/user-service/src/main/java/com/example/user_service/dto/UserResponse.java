package com.example.user_service.dto;

public class UserResponse {

    private Long id;
    private String username;
    private String role;

    public UserResponse() {
    }

    public UserResponse(Long id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}