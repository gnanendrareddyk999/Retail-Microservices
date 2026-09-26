package com.example.retailer_service.service;

import com.example.retailer_service.entity.OrderItem;
import com.example.retailer_service.repository.OrderItemRepository;

import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;

import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import java.util.List;

@Service
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;

    public OrderItemService(
            OrderItemRepository orderItemRepository) {

        this.orderItemRepository =
                orderItemRepository;
    }

    public List<OrderItem> getRetailerItems(

            String retailerUsername,

            LocalDate from,

            LocalDate to) {


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


        if (startDate.isAfter(
                endDate)) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "From date cannot be after To date"
            );
        }


        return orderItemRepository
                .findByRetailerUsernameAndCreatedAtBetween(

                        retailerUsername,

                        startDate.atStartOfDay(),

                        endDate
                                .plusDays(1)
                                .atStartOfDay()
                                .minusNanos(1)
                );
    }
    // =====================================================
// GET ORDER ITEM BY ID
// =====================================================

public OrderItem getOrderItemById(

        Long id,

        String retailerUsername) {

    OrderItem item = orderItemRepository
            .findById(id)
            .orElseThrow(

                    () -> new ResponseStatusException(

                            HttpStatus.NOT_FOUND,

                            "Order item not found"
                    )
            );

    // =================================================
    // RETAILER OWNERSHIP
    // =================================================

    if (!retailerUsername.equals(
            item.getRetailerUsername())) {

        throw new ResponseStatusException(

                HttpStatus.FORBIDDEN,

                "You are not allowed to view this order item"
        );
    }

    return item;
}
}