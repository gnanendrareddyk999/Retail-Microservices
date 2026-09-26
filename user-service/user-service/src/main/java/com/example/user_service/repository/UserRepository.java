package com.example.user_service.repository;

import com.example.user_service.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByUsernameAndRole(
            String username,
            String role
    );

    boolean existsByUsernameAndRole(
            String username,
            String role
    );

    List<User> findByUsername(String username);
}