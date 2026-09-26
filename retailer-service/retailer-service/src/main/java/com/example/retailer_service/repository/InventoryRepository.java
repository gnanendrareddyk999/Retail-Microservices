package com.example.retailer_service.repository;

import com.example.retailer_service.entity.Inventory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface InventoryRepository
        extends JpaRepository<Inventory, Long> {

    List<Inventory> findByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );
}