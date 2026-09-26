package com.example.retailer_service.controller;

import com.example.retailer_service.entity.OrderItem;
import com.example.retailer_service.service.OrderItemService;

import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import java.util.List;

@RestController
@RequestMapping("/order-items")
public class OrderItemController {

    private final OrderItemService orderItemService;

    public OrderItemController(
            OrderItemService orderItemService) {

        this.orderItemService =
                orderItemService;
    }
    // =====================================================
// GET ORDER ITEM BY ID
// RETAILER ONLY
// =====================================================

@GetMapping("/{id}")
@PreAuthorize("hasRole('RETAILER')")
public ResponseEntity<OrderItem> getOrderItemById(

        @PathVariable Long id,

        Authentication authentication) {

    return ResponseEntity.ok(

            orderItemService.getOrderItemById(

                    id,

                    authentication.getName()

            )
    );
}

    // =====================================================
    // RETAILER ONLY
    // =====================================================

    @GetMapping
    @PreAuthorize("hasRole('RETAILER')")
    public ResponseEntity<List<OrderItem>>
    getMyOrderItems(

            Authentication authentication,

            @RequestParam(
                    required = false
            )
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate from,

            @RequestParam(
                    required = false
            )
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate to) {

        return ResponseEntity.ok(

                orderItemService.getRetailerItems(

                        authentication.getName(),

                        from,

                        to
                )
        );
    }
}