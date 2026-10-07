package com.example.retailer_service.controller;

import com.example.retailer_service.dto.OrderRequest;
import com.example.retailer_service.dto.StatusRequest;
import com.example.retailer_service.entity.Order;
import com.example.retailer_service.service.OrderService;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(
            OrderService orderService) {

        this.orderService =
                orderService;
    }

    // =====================================================
    // USER - CREATE ORDER
    // =====================================================

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Order> createOrder(

            @Valid
            @RequestBody
            OrderRequest request,

            Authentication authentication) {

        return ResponseEntity.ok(

                orderService.createOrder(
                        request,
                        authentication.getName()
                )

        );
    }

    // =====================================================
    // USER - MY ORDERS
    // =====================================================

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMyOrders(

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

                orderService.getMyOrders(
                        authentication.getName(),
                        from,
                        to
                )

        );
    }
// =====================================================
// GET ORDER BY ID
// USER / RETAILER
// =====================================================

@GetMapping("/{id}")
@PreAuthorize("hasAnyRole('USER','RETAILER')")
public ResponseEntity<Order> getOrderById(

        @PathVariable Long id,

        Authentication authentication) {

    return ResponseEntity.ok(

            orderService.getOrderById(
                    id,
                    authentication.getName(),
                    authentication.getAuthorities()
                            .stream()
                            .anyMatch(
                                    a -> a.getAuthority()
                                            .equals("ROLE_RETAILER")
                            )
            )

    );
}
    // =====================================================
    // RETAILER - VIEW ORDERS
    // =====================================================

    @GetMapping("/retailer")
    @PreAuthorize("hasRole('RETAILER')")
    public ResponseEntity<?> getRetailerOrders(

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

                orderService.getRetailerOrders(
                        authentication.getName(),
                        from,
                        to
                )

        );
    }

    // =====================================================
    // RETAILER - UPDATE STATUS
    // =====================================================

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('RETAILER')")
    public ResponseEntity<Order> updateStatus(

            @PathVariable Long id,

            @Valid
            @RequestBody
            StatusRequest request,

            Authentication authentication) {

        return ResponseEntity.ok(

                orderService.updateStatus(

                        id,

                        request.getStatus(),

                        authentication.getName()

                )

        );
    }

    // =====================================================
    // USER - MARK RECEIVED
    // =====================================================

    @PutMapping("/{id}/received")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Order> markReceived(

            @PathVariable Long id,

            Authentication authentication) {

        return ResponseEntity.ok(

                orderService.markReceived(

                        id,

                        authentication.getName()

                )

        );
    }
}