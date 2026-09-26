package com.example.retailer_service.repository;

import com.example.retailer_service.entity.Order;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

import java.util.List;

public interface OrderRepository
        extends JpaRepository<Order, Long> {


    // =====================================================
    // ALL ORDERS OF USER
    // =====================================================

    List<Order> findByUsername(
            String username
    );


    // =====================================================
    // USER ORDERS BETWEEN TWO DATES
    // =====================================================

    List<Order> findByUsernameAndCreatedAtBetween(
            String username,
            LocalDateTime start,
            LocalDateTime end
    );


    // =====================================================
    // RETAILER ORDERS
    // =====================================================

    @Query("""
            SELECT DISTINCT o
            FROM Order o
            JOIN o.items i
            WHERE i.retailerUsername = :retailerUsername
            """)
    List<Order> findOrdersByRetailerUsername(
            @Param("retailerUsername")
            String retailerUsername
    );
}