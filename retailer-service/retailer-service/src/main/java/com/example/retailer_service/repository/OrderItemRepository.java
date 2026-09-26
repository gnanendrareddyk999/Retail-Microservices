package com.example.retailer_service.repository;

import com.example.retailer_service.entity.OrderItem;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

import java.util.List;

@Repository
public interface OrderItemRepository
        extends JpaRepository<OrderItem, Long> {

    List<OrderItem>
    findByRetailerUsernameAndCreatedAtBetween(

            String retailerUsername,

            LocalDateTime start,

            LocalDateTime end
    );
}