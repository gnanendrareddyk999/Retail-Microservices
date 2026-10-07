package com.example.retailer_service.service;

import com.example.retailer_service.client.UserClient;
import com.example.retailer_service.dto.OrderItemRequest;
import com.example.retailer_service.dto.OrderRequest;
import com.example.retailer_service.dto.UserResponse;
import com.example.retailer_service.entity.Inventory;
import com.example.retailer_service.entity.Order;
import com.example.retailer_service.entity.OrderItem;
import com.example.retailer_service.repository.InventoryRepository;
import com.example.retailer_service.repository.OrderRepository;

import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.List;

@Service
public class OrderService {

    private final UserClient userClient;

    private final InventoryRepository inventoryRepository;

    private final OrderRepository orderRepository;

    public OrderService(

            UserClient userClient,

            InventoryRepository inventoryRepository,

            OrderRepository orderRepository) {

        this.userClient =
                userClient;

        this.inventoryRepository =
                inventoryRepository;

        this.orderRepository =
                orderRepository;
    }


    // =====================================================
    // CREATE ORDER
    // =====================================================

    @Transactional
    public Order createOrder(

            OrderRequest request,

            String username) {


        // =================================================
        // 1. VERIFY USER THROUGH USER SERVICE
        // =================================================

        UserResponse user;

        try {

            user =
                    userClient.getCurrentUser();

        } catch (Exception e) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_GATEWAY,

                    "Could not verify user with user-service",

                    e
            );
        }


        // =================================================
        // 2. USER EXISTENCE
        // =================================================

        if (user == null) {

            throw new ResponseStatusException(

                    HttpStatus.UNAUTHORIZED,

                    "User could not be verified"
            );
        }


        // =================================================
        // 3. ONLY USER CAN PLACE ORDER
        // =================================================

        if (!"USER".equalsIgnoreCase(
                user.getRole())) {

            throw new ResponseStatusException(

                    HttpStatus.FORBIDDEN,

                    "Only USER can place orders"
            );
        }


        // =================================================
        // 4. JWT USER == USER SERVICE USER
        // =================================================

        if (!username.equals(
                user.getUsername())) {

            throw new ResponseStatusException(

                    HttpStatus.UNAUTHORIZED,

                    "Authenticated user does not match user-service"
            );
        }


        // =================================================
        // 5. CREATE ORDER
        // =================================================

        Order order =
                new Order();

        order.setUsername(
                username
        );

        order.setStatus(
                "PLACED"
        );


        double grandTotal = 0.0;

        String orderRetailer = null;


        // =================================================
        // 6. PROCESS ITEMS
        // =================================================

        for (OrderItemRequest itemRequest
                : request.getItems()) {


            // =============================================
            // FIND INVENTORY
            // =============================================

            Inventory inventory =
                    inventoryRepository
                            .findById(
                                    itemRequest
                                            .getInventoryId()
                            )
                            .orElseThrow(

                                    () ->
                                            new ResponseStatusException(

                                                    HttpStatus.NOT_FOUND,

                                                    "Inventory item not found: "
                                                            + itemRequest
                                                            .getInventoryId()
                                            )

                            );


            // =============================================
            // QUANTITY VALIDATION
            // =============================================

            if (itemRequest.getQuantity() <= 0) {

                throw new ResponseStatusException(

                        HttpStatus.BAD_REQUEST,

                        "Quantity must be greater than zero"
                );
            }


            // =============================================
            // STOCK VALIDATION
            // =============================================

            if (inventory.getQuantity()
                    < itemRequest.getQuantity()) {

                throw new ResponseStatusException(

                        HttpStatus.BAD_REQUEST,

                        "Insufficient stock for: "
                                + inventory.getProductName()
                );
            }


            // =============================================
            // RETAILER VALIDATION
            // =============================================

            if (inventory.getRetailerUsername() == null
                    || inventory
                    .getRetailerUsername()
                    .isBlank()) {

                throw new ResponseStatusException(

                        HttpStatus.INTERNAL_SERVER_ERROR,

                        "Inventory retailer is missing"
                );
            }


            // =============================================
            // ONE ORDER = ONE RETAILER
            // =============================================

            if (orderRetailer == null) {

                orderRetailer =
                        inventory
                                .getRetailerUsername();

            } else if (!orderRetailer.equals(
                    inventory.getRetailerUsername())) {

                throw new ResponseStatusException(

                        HttpStatus.BAD_REQUEST,

                        "One order can contain items from only one retailer"
                );
            }


            // =============================================
            // TOTAL PRICE
            // =============================================

            double totalPrice =

                    inventory.getPrice()
                            *
                            itemRequest.getQuantity();


            // =============================================
            // CREATE ORDER ITEM
            // =============================================

            OrderItem orderItem =
                    new OrderItem();

            orderItem.setInventoryId(
                    inventory.getId()
            );

            orderItem.setItemName(
                    inventory.getProductName()
            );

            orderItem.setQuantity(
                    itemRequest.getQuantity()
            );

            orderItem.setPrice(
                    inventory.getPrice()
            );

            orderItem.setTotalPrice(
                    totalPrice
            );

            orderItem.setRetailerUsername(
                    inventory.getRetailerUsername()
            );

            orderItem.setOrder(
                    order
            );


            order.getItems()
                    .add(orderItem);


            grandTotal +=
                    totalPrice;


            // =============================================
            // REDUCE STOCK
            // =============================================

            inventory.setQuantity(

                    inventory.getQuantity()
                            -
                            itemRequest.getQuantity()

            );

            inventoryRepository.save(
                    inventory
            );
        }


        // =================================================
        // 7. SET GRAND TOTAL
        // =================================================

        order.setGrandTotal(
                grandTotal
        );


        // =================================================
        // 8. SAVE ORDER
        // =================================================

        return orderRepository.save(
                order
        );
    }

// =====================================================
// GET ORDER BY ID
// =====================================================

public Order getOrderById(

        Long orderId,

        String username,

        boolean retailer) {

    Order order = orderRepository
            .findById(orderId)
            .orElseThrow(

                    () -> new ResponseStatusException(

                            HttpStatus.NOT_FOUND,

                            "Order not found"
                    )
            );

    // =================================================
    // RETAILER
    // =================================================

    if (retailer) {

        boolean retailerOwnsOrder =

                order.getItems()
                        .stream()
                        .anyMatch(

                                item -> username.equals(
                                        item.getRetailerUsername()
                                )
                        );

        if (!retailerOwnsOrder) {

            throw new ResponseStatusException(

                    HttpStatus.FORBIDDEN,

                    "You are not allowed to view this order"
            );
        }

        return order;
    }

    // =================================================
    // USER
    // =================================================

    if (!username.equals(
            order.getUsername())) {

        throw new ResponseStatusException(

                HttpStatus.FORBIDDEN,

                "You can view only your own order"
        );
    }

    return order;
}
    // =====================================================
    // GET MY ORDERS
    // =====================================================

    public List<Order> getMyOrders(

            String username,

            LocalDate from,

            LocalDate to) {


        if (from == null &&
                to == null) {

            return orderRepository
                    .findByUsername(
                            username
                    );
        }


        LocalDate startDate =

                from != null
                        ? from
                        : LocalDate.of(
                                2000,
                                1,
                                1
                        );


        LocalDate endDate =

                to != null
                        ? to
                        : LocalDate.now();


        validateDateRange(
                startDate,
                endDate
        );


        LocalDateTime start =
                startDate.atStartOfDay();


        LocalDateTime end =

                endDate
                        .plusDays(1)
                        .atStartOfDay()
                        .minusNanos(1);


        return orderRepository
                .findByUsernameAndCreatedAtBetween(

                        username,

                        start,

                        end
                );
    }


    // =====================================================
    // RETAILER ORDERS
    // =====================================================

    public List<Order> getRetailerOrders(

            String retailerUsername) {

        return orderRepository
                .findOrdersByRetailerUsername(
                        retailerUsername
                );
    }

    public List<Order> getRetailerOrders(

            String retailerUsername,

            LocalDate from,

            LocalDate to) {

        if (from == null && to == null) {

            return getRetailerOrders(
                    retailerUsername
            );
        }

        LocalDate startDate =
                from != null
                        ? from
                        : LocalDate.of(2000, 1, 1);

        LocalDate endDate =
                to != null
                        ? to
                        : LocalDate.now();

        validateDateRange(
                startDate,
                endDate
        );

        return orderRepository
                .findOrdersByRetailerUsernameAndCreatedAtBetween(
                        retailerUsername,
                        startDate.atStartOfDay(),
                        endDate.plusDays(1)
                                .atStartOfDay()
                                .minusNanos(1)
                );
    }


    // =====================================================
    // UPDATE STATUS
    // =====================================================

    @Transactional
    public Order updateStatus(

            Long orderId,

            String requestedStatus,

            String retailerUsername) {


        // =================================================
        // 1. FIND ORDER
        // =================================================

        Order order =
                orderRepository
                        .findById(orderId)

                        .orElseThrow(

                                () ->
                                        new ResponseStatusException(

                                                HttpStatus.NOT_FOUND,

                                                "Order not found"
                                        )
                        );


        // =================================================
        // 2. CHECK RETAILER OWNERSHIP
        // =================================================

        boolean retailerOwnsOrder =

                order.getItems()
                        .stream()
                        .anyMatch(

                                item ->

                                        retailerUsername.equals(

                                                item.getRetailerUsername()
                                        )
                        );


        if (!retailerOwnsOrder) {

            throw new ResponseStatusException(

                    HttpStatus.FORBIDDEN,

                    "You are not allowed to update this order"
            );
        }


        // =================================================
        // 3. NORMALIZE STATUS
        // =================================================

        String newStatus =

                requestedStatus == null

                        ? ""

                        : requestedStatus
                                .trim()
                                .toUpperCase();


        // =================================================
        // 4. VALID STATUS
        // =================================================

        if (!List.of(

                "CONFIRMED",

                "SHIPPED",

                "OUT_FOR_DELIVERY",

                "DELIVERED"

        ).contains(newStatus)) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Invalid order status. Allowed: CONFIRMED, SHIPPED, OUT_FOR_DELIVERY, DELIVERED"
            );
        }


        // =================================================
        // 5. CURRENT STATUS
        // =================================================

        String currentStatus =
                order.getStatus();


        // =================================================
        // 6. STATUS TRANSITION
        // =================================================

        boolean validTransition =

                switch (currentStatus) {

                    case "PLACED" ->

                            "CONFIRMED"
                                    .equals(newStatus);

                    case "CONFIRMED" ->

                            "SHIPPED"
                                    .equals(newStatus);

                    case "SHIPPED" ->

                            "OUT_FOR_DELIVERY"
                                    .equals(newStatus);

                    case "OUT_FOR_DELIVERY" ->

                            "DELIVERED"
                                    .equals(newStatus);

                    default -> false;
                };


        // =================================================
        // 7. INVALID TRANSITION
        // =================================================

        if (!validTransition) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Invalid status transition: "
                            + currentStatus
                            + " -> "
                            + newStatus
            );
        }


        // =================================================
        // 8. UPDATE
        // =================================================

        order.setStatus(
                newStatus
        );


        return orderRepository.save(
                order
        );
    }


    // =====================================================
    // MARK ORDER RECEIVED
    // =====================================================

    @Transactional
    public Order markReceived(

            Long orderId,

            String username) {


        Order order =
                orderRepository
                        .findById(orderId)

                        .orElseThrow(

                                () ->
                                        new ResponseStatusException(

                                                HttpStatus.NOT_FOUND,

                                                "Order not found"
                                        )
                        );


        // =================================================
        // USER OWNERSHIP
        // =================================================

        if (!username.equals(
                order.getUsername())) {

            throw new ResponseStatusException(

                    HttpStatus.FORBIDDEN,

                    "You can receive only your own order"
            );
        }


        // =================================================
        // MUST BE DELIVERED
        // =================================================

        if (!"DELIVERED".equals(
                order.getStatus())) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Order can be marked RECEIVED only after DELIVERED"
            );
        }


        // =================================================
        // RECEIVED
        // =================================================

        order.setStatus(
                "RECEIVED"
        );


        return orderRepository.save(
                order
        );
    }


    // =====================================================
    // DATE VALIDATION
    // =====================================================

    private void validateDateRange(

            LocalDate startDate,

            LocalDate endDate) {

        if (startDate.isAfter(
                endDate)) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "From date cannot be after To date"
            );
        }
    }
}