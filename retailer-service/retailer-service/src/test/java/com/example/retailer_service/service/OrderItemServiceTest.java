package com.example.retailer_service.service;

import com.example.retailer_service.entity.OrderItem;
import com.example.retailer_service.repository.OrderItemRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderItemService orderItemService;

    @Test
    void testGetRetailerItemsSuccess() {

        // 1. Create test OrderItem
        OrderItem item = new OrderItem();

        // 2. Create test dates
        LocalDate from =
                LocalDate.of(2026, 1, 1);

        LocalDate to =
                LocalDate.of(2026, 1, 31);

        // 3. Mock repository
        Mockito.when(
                orderItemRepository.findByRetailerUsernameAndCreatedAtBetween(
                        Mockito.eq("retailer1"),
                        Mockito.any(java.time.LocalDateTime.class),
                        Mockito.any(java.time.LocalDateTime.class)
                )
        ).thenReturn(List.of(item));

        // 4. Call service
        List<OrderItem> result =
                orderItemService.getRetailerItems(
                        "retailer1",
                        from,
                        to
                );

        // 5. Verify result
        org.junit.jupiter.api.Assertions.assertEquals(
                1,
                result.size()
        );

        // 6. Verify repository was called
        Mockito.verify(orderItemRepository)
                .findByRetailerUsernameAndCreatedAtBetween(
                        Mockito.eq("retailer1"),
                        Mockito.eq(
                                java.time.LocalDateTime.of(
                                        2026, 1, 1, 0, 0
                                )
                        ),
                        Mockito.eq(
                                java.time.LocalDateTime.of(
                                        2026, 2, 1, 0, 0
                                ).minusNanos(1)
                        )
                );
    }

@Test
void testGetRetailerItemsInvalidDateRange() {

    // 1. From date is after To date
    LocalDate from =
            LocalDate.of(2026, 2, 1);

    LocalDate to =
            LocalDate.of(2026, 1, 1);

    // 2. Exception should be thrown
    org.junit.jupiter.api.Assertions.assertThrows(
            org.springframework.web.server.ResponseStatusException.class,
            () -> orderItemService.getRetailerItems(
                    "retailer1",
                    from,
                    to
            )
    );

    // 3. Repository should NOT be called
    Mockito.verify(
            orderItemRepository,
            Mockito.never()
    ).findByRetailerUsernameAndCreatedAtBetween(
            Mockito.any(String.class),
            Mockito.any(java.time.LocalDateTime.class),
            Mockito.any(java.time.LocalDateTime.class)
    );
}

@Test
void testGetRetailerItemsWithoutDates() {

    // 1. Create test OrderItem
    OrderItem item = new OrderItem();

    // 2. Mock repository
    Mockito.when(
            orderItemRepository.findByRetailerUsernameAndCreatedAtBetween(
                    Mockito.eq("retailer1"),
                    Mockito.any(java.time.LocalDateTime.class),
                    Mockito.any(java.time.LocalDateTime.class)
            )
    ).thenReturn(List.of(item));

    // 3. Call service without dates
    List<OrderItem> result =
            orderItemService.getRetailerItems(
                    "retailer1",
                    null,
                    null
            );

    // 4. Verify result
    org.junit.jupiter.api.Assertions.assertEquals(
            1,
            result.size()
    );

    // 5. Verify repository call
    Mockito.verify(orderItemRepository)
            .findByRetailerUsernameAndCreatedAtBetween(
                    Mockito.eq("retailer1"),
                    Mockito.eq(
                            java.time.LocalDateTime.of(
                                    2000, 1, 1, 0, 0
                            )
                    ),
                    Mockito.eq(
                            LocalDate.now()
                                    .plusDays(1)
                                    .atStartOfDay()
                                    .minusNanos(1)
                    )
            );
}

@Test
void testGetOrderItemByIdSuccess() {

    // 1. Create OrderItem
    OrderItem item = new OrderItem();

    // 2. Set owner retailer
    item.setRetailerUsername("retailer1");

    // 3. Mock repository
    Mockito.when(orderItemRepository.findById(10L))
            .thenReturn(java.util.Optional.of(item));

    // 4. Call service
    OrderItem result =
            orderItemService.getOrderItemById(
                    10L,
                    "retailer1"
            );

    // 5. Verify result
    org.junit.jupiter.api.Assertions.assertNotNull(result);

    // 6. Verify repository call
    Mockito.verify(orderItemRepository)
            .findById(10L);
}

@Test
void testGetOrderItemByIdNotFound() {

    // 1. Order item does not exist
    Mockito.when(orderItemRepository.findById(999L))
            .thenReturn(java.util.Optional.empty());

    // 2. NOT_FOUND exception should be thrown
    org.junit.jupiter.api.Assertions.assertThrows(
            org.springframework.web.server.ResponseStatusException.class,
            () -> orderItemService.getOrderItemById(
                    999L,
                    "retailer1"
            )
    );

    // 3. Verify repository was called
    Mockito.verify(orderItemRepository)
            .findById(999L);
}

@Test
void testGetOrderItemByIdUnauthorizedRetailer() {

    // 1. Order item belongs to retailer1
    OrderItem item = new OrderItem();
    item.setRetailerUsername("retailer1");

    // 2. Mock repository
    Mockito.when(orderItemRepository.findById(20L))
            .thenReturn(java.util.Optional.of(item));

    // 3. retailer2 tries to access retailer1's item
    org.junit.jupiter.api.Assertions.assertThrows(
            org.springframework.web.server.ResponseStatusException.class,
            () -> orderItemService.getOrderItemById(
                    20L,
                    "retailer2"
            )
    );

    // 4. Verify repository was called
    Mockito.verify(orderItemRepository)
            .findById(20L);
}
}