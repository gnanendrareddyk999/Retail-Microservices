package com.example.retailer_service.service;

import com.example.retailer_service.client.UserClient;
import com.example.retailer_service.repository.InventoryRepository;
import com.example.retailer_service.repository.OrderRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.retailer_service.dto.OrderItemRequest;
import com.example.retailer_service.entity.OrderItem;
import com.example.retailer_service.dto.OrderRequest;
import com.example.retailer_service.dto.UserResponse;
import com.example.retailer_service.entity.Inventory;
import com.example.retailer_service.entity.Order;

import org.mockito.Mockito;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private UserClient userClient;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

   @Test
void testCreateOrderUserServiceFailure() {

    // User Service throws an exception
    Mockito.when(userClient.getCurrentUser())
            .thenThrow(new RuntimeException("User service unavailable"));


    // Request object
    OrderRequest request = new OrderRequest();


    // Verify exception
    ResponseStatusException exception =
            assertThrows(
                    ResponseStatusException.class,
                    () -> orderService.createOrder(
                            request,
                            "user1"
                    )
            );


    // Verify HTTP status
    assertEquals(
            HttpStatus.BAD_GATEWAY,
            exception.getStatusCode()
    );


    // Verify message
    assertEquals(
            "Could not verify user with user-service",
            exception.getReason()
    );


    // Inventory should never be called
    Mockito.verifyNoInteractions(
            inventoryRepository
    );


    // Order should never be saved
    Mockito.verifyNoInteractions(
            orderRepository
    );
}


@Test
void testCreateOrderUserNotVerified() {

    // User Service returns null
    Mockito.when(userClient.getCurrentUser())
            .thenReturn(null);


    // Request object
    OrderRequest request = new OrderRequest();


    // Verify exception
    ResponseStatusException exception =
            assertThrows(
                    ResponseStatusException.class,
                    () -> orderService.createOrder(
                            request,
                            "user1"
                    )
            );


    // Verify HTTP status
    assertEquals(
            HttpStatus.UNAUTHORIZED,
            exception.getStatusCode()
    );


    // Verify message
    assertEquals(
            "User could not be verified",
            exception.getReason()
    );


    // Inventory should never be called
    Mockito.verifyNoInteractions(
            inventoryRepository
    );


    // Order should never be saved
    Mockito.verifyNoInteractions(
            orderRepository
    );
}

@Test
void testCreateOrderNonUserRole() {

    // User Service returns RETAILER
    UserResponse user =
            new UserResponse(
                    2L,
                    "retailer1",
                    "RETAILER"
            );

    Mockito.when(userClient.getCurrentUser())
            .thenReturn(user);


    // Request object
    OrderRequest request = new OrderRequest();


    // Verify exception
    ResponseStatusException exception =
            assertThrows(
                    ResponseStatusException.class,
                    () -> orderService.createOrder(
                            request,
                            "retailer1"
                    )
            );


    // Verify HTTP status
    assertEquals(
            HttpStatus.FORBIDDEN,
            exception.getStatusCode()
    );


    // Verify message
    assertEquals(
            "Only USER can place orders",
            exception.getReason()
    );


    // No database operations should happen
    Mockito.verifyNoInteractions(
            inventoryRepository
    );

    Mockito.verifyNoInteractions(
            orderRepository
    );
}

@Test
void testCreateOrderUsernameMismatch() {

    UserResponse user =
            new UserResponse(3L, "user2", "USER");

    Mockito.when(userClient.getCurrentUser())
            .thenReturn(user);

    OrderRequest request = new OrderRequest();

    ResponseStatusException exception =
            assertThrows(
                    ResponseStatusException.class,
                    () -> orderService.createOrder(request, "user1")
            );

    assertEquals(
            HttpStatus.UNAUTHORIZED,
            exception.getStatusCode()
    );

    assertEquals(
            "Authenticated user does not match user-service",
            exception.getReason()
    );

    Mockito.verifyNoInteractions(inventoryRepository);
    Mockito.verifyNoInteractions(orderRepository);
}

@Test
void testCreateOrderInventoryNotFound() {

    UserResponse user =
            new UserResponse(1L, "user1", "USER");

    Mockito.when(userClient.getCurrentUser())
            .thenReturn(user);

    OrderItemRequest itemRequest =
            new OrderItemRequest();

    itemRequest.setInventoryId(999L);
    itemRequest.setQuantity(2);

    OrderRequest request =
            new OrderRequest();

    request.setItems(List.of(itemRequest));

    Mockito.when(inventoryRepository.findById(999L))
            .thenReturn(Optional.empty());

    ResponseStatusException exception =
            assertThrows(
                    ResponseStatusException.class,
                    () -> orderService.createOrder(request, "user1")
            );

    assertEquals(
            HttpStatus.NOT_FOUND,
            exception.getStatusCode()
    );

    assertEquals(
            "Inventory item not found: 999",
            exception.getReason()
    );

    Mockito.verify(inventoryRepository)
            .findById(999L);

    Mockito.verifyNoInteractions(orderRepository);
}

@Test
void testCreateOrderInvalidQuantity() {

    UserResponse user =
            new UserResponse(1L, "user1", "USER");

    Mockito.when(userClient.getCurrentUser())
            .thenReturn(user);

    Inventory inventory = new Inventory();

    inventory.setId(1L);
    inventory.setProductName("Laptop");
    inventory.setPrice(50000.0);
    inventory.setQuantity(10);
    inventory.setRetailerUsername("retailer1");

    Mockito.when(inventoryRepository.findById(1L))
            .thenReturn(Optional.of(inventory));

    OrderItemRequest itemRequest =
            new OrderItemRequest();

    itemRequest.setInventoryId(1L);
    itemRequest.setQuantity(0);

    OrderRequest request =
            new OrderRequest();

    request.setItems(List.of(itemRequest));

    ResponseStatusException exception =
            assertThrows(
                    ResponseStatusException.class,
                    () -> orderService.createOrder(request, "user1")
            );

    assertEquals(
            HttpStatus.BAD_REQUEST,
            exception.getStatusCode()
    );

    assertEquals(
            "Quantity must be greater than zero",
            exception.getReason()
    );

    Mockito.verify(inventoryRepository)
            .findById(1L);

    Mockito.verifyNoInteractions(orderRepository);
}

@Test
void testCreateOrderInsufficientStock() {

    UserResponse user =
            new UserResponse(1L, "user1", "USER");

    Mockito.when(userClient.getCurrentUser())
            .thenReturn(user);

    Inventory inventory = new Inventory();

    inventory.setId(1L);
    inventory.setProductName("Laptop");
    inventory.setPrice(50000.0);
    inventory.setQuantity(5);
    inventory.setRetailerUsername("retailer1");

    Mockito.when(inventoryRepository.findById(1L))
            .thenReturn(Optional.of(inventory));

    OrderItemRequest itemRequest =
            new OrderItemRequest();

    itemRequest.setInventoryId(1L);
    itemRequest.setQuantity(10);

    OrderRequest request =
            new OrderRequest();

    request.setItems(List.of(itemRequest));

    ResponseStatusException exception =
            assertThrows(
                    ResponseStatusException.class,
                    () -> orderService.createOrder(request, "user1")
            );

    assertEquals(
            HttpStatus.BAD_REQUEST,
            exception.getStatusCode()
    );

    assertEquals(
            "Insufficient stock for: Laptop",
            exception.getReason()
    );

    Mockito.verify(inventoryRepository)
            .findById(1L);

    Mockito.verifyNoMoreInteractions(inventoryRepository);

    Mockito.verifyNoInteractions(orderRepository);
}

@Test
void testCreateOrderInventoryRetailerMissing() {

    UserResponse user =
            new UserResponse(1L, "user1", "USER");

    Mockito.when(userClient.getCurrentUser())
            .thenReturn(user);

    Inventory inventory = new Inventory();

    inventory.setId(1L);
    inventory.setProductName("Laptop");
    inventory.setPrice(50000.0);
    inventory.setQuantity(10);

    // retailerUsername intentionally NOT set

    Mockito.when(inventoryRepository.findById(1L))
            .thenReturn(Optional.of(inventory));

    OrderItemRequest itemRequest =
            new OrderItemRequest();

    itemRequest.setInventoryId(1L);
    itemRequest.setQuantity(2);

    OrderRequest request =
            new OrderRequest();

    request.setItems(List.of(itemRequest));

    ResponseStatusException exception =
            assertThrows(
                    ResponseStatusException.class,
                    () -> orderService.createOrder(request, "user1")
            );

    assertEquals(
            HttpStatus.INTERNAL_SERVER_ERROR,
            exception.getStatusCode()
    );

    assertEquals(
            "Inventory retailer is missing",
            exception.getReason()
    );

    Mockito.verify(inventoryRepository)
            .findById(1L);

    Mockito.verifyNoMoreInteractions(inventoryRepository);

    Mockito.verifyNoInteractions(orderRepository);
}

@Test
void testCreateOrderMultipleRetailers() {

    UserResponse user =
            new UserResponse(1L, "user1", "USER");

    Mockito.when(userClient.getCurrentUser())
            .thenReturn(user);

    Inventory inventory1 = new Inventory();

    inventory1.setId(1L);
    inventory1.setProductName("Laptop");
    inventory1.setPrice(50000.0);
    inventory1.setQuantity(10);
    inventory1.setRetailerUsername("retailer1");


    Inventory inventory2 = new Inventory();

    inventory2.setId(2L);
    inventory2.setProductName("Mouse");
    inventory2.setPrice(1000.0);
    inventory2.setQuantity(10);
    inventory2.setRetailerUsername("retailer2");


    Mockito.when(inventoryRepository.findById(1L))
            .thenReturn(Optional.of(inventory1));

    Mockito.when(inventoryRepository.findById(2L))
            .thenReturn(Optional.of(inventory2));


    OrderItemRequest item1 =
            new OrderItemRequest();

    item1.setInventoryId(1L);
    item1.setQuantity(1);


    OrderItemRequest item2 =
            new OrderItemRequest();

    item2.setInventoryId(2L);
    item2.setQuantity(1);


    OrderRequest request =
            new OrderRequest();

    request.setItems(List.of(item1, item2));


    ResponseStatusException exception =
            assertThrows(
                    ResponseStatusException.class,
                    () -> orderService.createOrder(request, "user1")
            );


    assertEquals(
            HttpStatus.BAD_REQUEST,
            exception.getStatusCode()
    );

    assertEquals(
            "One order can contain items from only one retailer",
            exception.getReason()
    );


    Mockito.verify(inventoryRepository)
            .findById(1L);

    Mockito.verify(inventoryRepository)
            .findById(2L);

    Mockito.verify(inventoryRepository)
            .save(inventory1);

    Mockito.verifyNoInteractions(orderRepository);
}

@Test
void testCreateOrderSuccess() {

    UserResponse user =
            new UserResponse(1L, "user1", "USER");

    Mockito.when(userClient.getCurrentUser())
            .thenReturn(user);

    Inventory inventory = new Inventory();

    inventory.setId(1L);
    inventory.setProductName("Laptop");
    inventory.setPrice(50000.0);
    inventory.setQuantity(10);
    inventory.setRetailerUsername("retailer1");

    Mockito.when(inventoryRepository.findById(1L))
            .thenReturn(Optional.of(inventory));

    OrderItemRequest itemRequest =
            new OrderItemRequest();

    itemRequest.setInventoryId(1L);
    itemRequest.setQuantity(2);

    OrderRequest request =
            new OrderRequest();

    request.setItems(List.of(itemRequest));

    Mockito.when(orderRepository.save(Mockito.any(Order.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    Order result =
            orderService.createOrder(request, "user1");

    assertNotNull(result);

    assertEquals("user1", result.getUsername());

    assertEquals("PLACED", result.getStatus());

    assertEquals(100000.0, result.getGrandTotal());

    assertEquals(1, result.getItems().size());

    assertEquals(2, result.getItems().get(0).getQuantity());

    assertEquals(
            50000.0,
            result.getItems().get(0).getPrice()
    );

    assertEquals(
            100000.0,
            result.getItems().get(0).getTotalPrice()
    );

    assertEquals(
            "retailer1",
            result.getItems().get(0).getRetailerUsername()
    );

    assertEquals(
            8,
            inventory.getQuantity()
    );

    Mockito.verify(inventoryRepository)
            .save(inventory);

    Mockito.verify(orderRepository)
            .save(Mockito.any(Order.class));
}

@Test
void testGetOrderByIdSuccessForUser() {

    Order order = new Order();

    order.setId(1L);
    order.setUsername("user1");
    order.setStatus("PLACED");
    order.setGrandTotal(100000.0);

    Mockito.when(orderRepository.findById(1L))
            .thenReturn(Optional.of(order));

    Order result =
            orderService.getOrderById(
                    1L,
                    "user1",
                    false
            );

    assertNotNull(result);

    assertEquals(1L, result.getId());

    assertEquals(
            "user1",
            result.getUsername()
    );

    assertEquals(
            "PLACED",
            result.getStatus()
    );

    assertEquals(
            100000.0,
            result.getGrandTotal()
    );

    Mockito.verify(orderRepository)
            .findById(1L);
}

@Test
void testGetOrderByIdNotFound() {

    Mockito.when(orderRepository.findById(999L))
            .thenReturn(Optional.empty());

    ResponseStatusException exception =
            assertThrows(
                    ResponseStatusException.class,
                    () -> orderService.getOrderById(
                            999L,
                            "user1",
                            false
                    )
            );

    assertEquals(
            HttpStatus.NOT_FOUND,
            exception.getStatusCode()
    );

    assertEquals(
            "Order not found",
            exception.getReason()
    );

    Mockito.verify(orderRepository)
            .findById(999L);
}

@Test
void testGetOrderByIdUnauthorizedUser() {

    Order order = new Order();

    order.setId(1L);
    order.setUsername("user2");
    order.setStatus("PLACED");
    order.setGrandTotal(50000.0);

    Mockito.when(orderRepository.findById(1L))
            .thenReturn(Optional.of(order));

    ResponseStatusException exception =
            assertThrows(
                    ResponseStatusException.class,
                    () -> orderService.getOrderById(
                            1L,
                            "user1",
                            false
                    )
            );

    assertEquals(
            HttpStatus.FORBIDDEN,
            exception.getStatusCode()
    );

    assertEquals(
            "You can view only your own order",
            exception.getReason()
    );

    Mockito.verify(orderRepository)
            .findById(1L);
}

@Test
void testGetOrderByIdSuccessForRetailer() {

    Order order = new Order();

    order.setId(1L);
    order.setUsername("user1");
    order.setStatus("PLACED");
    order.setGrandTotal(50000.0);

    OrderItem item = new OrderItem();

    item.setRetailerUsername("retailer1");

    order.getItems().add(item);

    Mockito.when(orderRepository.findById(1L))
            .thenReturn(Optional.of(order));

    Order result =
            orderService.getOrderById(
                    1L,
                    "retailer1",
                    true
            );

    assertNotNull(result);

    assertEquals(
            1L,
            result.getId()
    );

    assertEquals(
            "user1",
            result.getUsername()
    );

    assertEquals(
            "PLACED",
            result.getStatus()
    );

    Mockito.verify(orderRepository)
            .findById(1L);
}
@Test
void testGetOrderByIdUnauthorizedRetailer() {

    Order order = new Order();

    order.setId(1L);
    order.setUsername("user1");
    order.setStatus("PLACED");
    order.setGrandTotal(50000.0);

    OrderItem item = new OrderItem();

    item.setRetailerUsername("retailer1");

    order.getItems().add(item);

    Mockito.when(orderRepository.findById(1L))
            .thenReturn(Optional.of(order));

    ResponseStatusException exception =
            assertThrows(
                    ResponseStatusException.class,
                    () -> orderService.getOrderById(
                            1L,
                            "retailer2",
                            true
                    )
            );

    assertEquals(
            HttpStatus.FORBIDDEN,
            exception.getStatusCode()
    );

    assertEquals(
            "You are not allowed to view this order",
            exception.getReason()
    );

    Mockito.verify(orderRepository)
            .findById(1L);
}

@Test
void testGetMyOrdersWithoutDateFilter() {

    Order order1 = new Order();
    order1.setId(1L);
    order1.setUsername("user1");
    order1.setStatus("PLACED");

    Order order2 = new Order();
    order2.setId(2L);
    order2.setUsername("user1");
    order2.setStatus("DELIVERED");

    List<Order> orders = List.of(order1, order2);

    Mockito.when(orderRepository.findByUsername("user1"))
            .thenReturn(orders);

    List<Order> result =
            orderService.getMyOrders(
                    "user1",
                    null,
                    null
            );

    assertNotNull(result);

    assertEquals(
            2,
            result.size()
    );

    assertEquals(
            1L,
            result.get(0).getId()
    );

    assertEquals(
            2L,
            result.get(1).getId()
    );

    Mockito.verify(orderRepository)
            .findByUsername("user1");

    Mockito.verify(
            orderRepository,
            Mockito.never()
    ).findByUsernameAndCreatedAtBetween(
            Mockito.anyString(),
            Mockito.any(),
            Mockito.any()
    );
}

@Test
void testGetMyOrdersWithDateFilter() {

    LocalDate from = LocalDate.of(2026, 9, 1);
    LocalDate to = LocalDate.of(2026, 9, 30);

    LocalDateTime expectedStart =
            from.atStartOfDay();

    LocalDateTime expectedEnd =
            to.plusDays(1)
                    .atStartOfDay()
                    .minusNanos(1);

    Order order1 = new Order();

    order1.setId(1L);
    order1.setUsername("user1");
    order1.setStatus("PLACED");

    Order order2 = new Order();

    order2.setId(2L);
    order2.setUsername("user1");
    order2.setStatus("DELIVERED");

    List<Order> orders =
            List.of(order1, order2);

    Mockito.when(
            orderRepository.findByUsernameAndCreatedAtBetween(
                    "user1",
                    expectedStart,
                    expectedEnd
            )
    ).thenReturn(orders);

    List<Order> result =
            orderService.getMyOrders(
                    "user1",
                    from,
                    to
            );

    assertNotNull(result);

    assertEquals(
            2,
            result.size()
    );

    assertEquals(
            1L,
            result.get(0).getId()
    );

    assertEquals(
            2L,
            result.get(1).getId()
    );

    Mockito.verify(
            orderRepository
    ).findByUsernameAndCreatedAtBetween(
            "user1",
            expectedStart,
            expectedEnd
    );

    Mockito.verify(
            orderRepository,
            Mockito.never()
    ).findByUsername("user1");
}

@Test
void testGetMyOrdersInvalidDateRange() {

    LocalDate from =
            LocalDate.of(2026, 9, 30);

    LocalDate to =
            LocalDate.of(2026, 9, 1);

    ResponseStatusException exception =
            assertThrows(
                    ResponseStatusException.class,
                    () -> orderService.getMyOrders(
                            "user1",
                            from,
                            to
                    )
            );

    assertEquals(
            HttpStatus.BAD_REQUEST,
            exception.getStatusCode()
    );

    assertEquals(
            "From date cannot be after To date",
            exception.getReason()
    );

    Mockito.verifyNoInteractions(
            orderRepository
    );
}

@Test
void testGetRetailerOrders() {

    Order order1 = new Order();

    order1.setId(1L);
    order1.setUsername("user1");
    order1.setStatus("PLACED");

    Order order2 = new Order();

    order2.setId(2L);
    order2.setUsername("user2");
    order2.setStatus("DELIVERED");

    List<Order> orders =
            List.of(order1, order2);

    Mockito.when(
            orderRepository.findOrdersByRetailerUsername(
                    "retailer1"
            )
    ).thenReturn(orders);

    List<Order> result =
            orderService.getRetailerOrders(
                    "retailer1"
            );

    assertNotNull(result);

    assertEquals(
            2,
            result.size()
    );

    assertEquals(
            1L,
            result.get(0).getId()
    );

    assertEquals(
            2L,
            result.get(1).getId()
    );

    Mockito.verify(
            orderRepository
    ).findOrdersByRetailerUsername(
            "retailer1"
    );
}

@Test
void testUpdateStatusOrderNotFound() {

    Mockito.when(orderRepository.findById(999L))
            .thenReturn(Optional.empty());

    ResponseStatusException exception =
            assertThrows(
                    ResponseStatusException.class,
                    () -> orderService.updateStatus(
                            999L,
                            "CONFIRMED",
                            "retailer1"
                    )
            );

    assertEquals(
            HttpStatus.NOT_FOUND,
            exception.getStatusCode()
    );

    assertEquals(
            "Order not found",
            exception.getReason()
    );

    Mockito.verify(orderRepository)
            .findById(999L);
}

@Test
void testUpdateStatusUnauthorizedRetailer() {

    Order order = new Order();

    order.setId(1L);
    order.setUsername("user1");
    order.setStatus("PLACED");

    OrderItem item = new OrderItem();

    item.setRetailerUsername("retailer1");

    order.getItems().add(item);

    Mockito.when(orderRepository.findById(1L))
            .thenReturn(Optional.of(order));

    ResponseStatusException exception =
            assertThrows(
                    ResponseStatusException.class,
                    () -> orderService.updateStatus(
                            1L,
                            "CONFIRMED",
                            "retailer2"
                    )
            );

    assertEquals(
            HttpStatus.FORBIDDEN,
            exception.getStatusCode()
    );

    assertEquals(
            "You are not allowed to update this order",
            exception.getReason()
    );

    Mockito.verify(orderRepository)
            .findById(1L);

    Mockito.verify(
            orderRepository,
            Mockito.never()
    ).save(Mockito.any(Order.class));
}

@Test
void testUpdateStatusInvalidStatus() {

    Order order = new Order();

    order.setId(1L);
    order.setUsername("user1");
    order.setStatus("PLACED");

    OrderItem item = new OrderItem();

    item.setRetailerUsername("retailer1");

    order.getItems().add(item);

    Mockito.when(orderRepository.findById(1L))
            .thenReturn(Optional.of(order));

    ResponseStatusException exception =
            assertThrows(
                    ResponseStatusException.class,
                    () -> orderService.updateStatus(
                            1L,
                            "INVALID_STATUS",
                            "retailer1"
                    )
            );

    assertEquals(
            HttpStatus.BAD_REQUEST,
            exception.getStatusCode()
    );

    assertEquals(
            "Invalid order status. Allowed: CONFIRMED, SHIPPED, OUT_FOR_DELIVERY, DELIVERED",
            exception.getReason()
    );

    Mockito.verify(orderRepository)
            .findById(1L);

    Mockito.verify(
            orderRepository,
            Mockito.never()
    ).save(Mockito.any(Order.class));
}

@Test
void testUpdateStatusPlacedToConfirmed() {

    Order order = new Order();

    order.setId(1L);
    order.setUsername("user1");
    order.setStatus("PLACED");

    OrderItem item = new OrderItem();

    item.setRetailerUsername("retailer1");

    order.getItems().add(item);

    Mockito.when(orderRepository.findById(1L))
            .thenReturn(Optional.of(order));

    Mockito.when(orderRepository.save(order))
            .thenReturn(order);

    Order result =
            orderService.updateStatus(
                    1L,
                    "CONFIRMED",
                    "retailer1"
            );

    assertEquals(
            "CONFIRMED",
            result.getStatus()
    );

    assertEquals(
            "CONFIRMED",
            order.getStatus()
    );

    Mockito.verify(orderRepository)
            .findById(1L);

    Mockito.verify(orderRepository)
            .save(order);
}

@Test
void testUpdateStatusConfirmedToShipped() {

    Order order = new Order();

    order.setId(1L);
    order.setUsername("user1");
    order.setStatus("CONFIRMED");

    OrderItem item = new OrderItem();

    item.setRetailerUsername("retailer1");

    order.getItems().add(item);

    Mockito.when(orderRepository.findById(1L))
            .thenReturn(Optional.of(order));

    Mockito.when(orderRepository.save(order))
            .thenReturn(order);

    Order result =
            orderService.updateStatus(
                    1L,
                    "SHIPPED",
                    "retailer1"
            );

    assertEquals(
            "SHIPPED",
            result.getStatus()
    );

    assertEquals(
            "SHIPPED",
            order.getStatus()
    );

    Mockito.verify(orderRepository)
            .findById(1L);

    Mockito.verify(orderRepository)
            .save(order);
}

@Test
void testUpdateStatusShippedToOutForDelivery() {

    Order order = new Order();

    order.setId(1L);
    order.setUsername("user1");
    order.setStatus("SHIPPED");

    OrderItem item = new OrderItem();

    item.setRetailerUsername("retailer1");

    order.getItems().add(item);

    Mockito.when(orderRepository.findById(1L))
            .thenReturn(Optional.of(order));

    Mockito.when(orderRepository.save(order))
            .thenReturn(order);

    Order result =
            orderService.updateStatus(
                    1L,
                    "OUT_FOR_DELIVERY",
                    "retailer1"
            );

    assertEquals(
            "OUT_FOR_DELIVERY",
            result.getStatus()
    );

    assertEquals(
            "OUT_FOR_DELIVERY",
            order.getStatus()
    );

    Mockito.verify(orderRepository)
            .findById(1L);

    Mockito.verify(orderRepository)
            .save(order);
}

@Test
void testUpdateStatusOutForDeliveryToDelivered() {

    Order order = new Order();

    order.setId(1L);
    order.setUsername("user1");
    order.setStatus("OUT_FOR_DELIVERY");

    OrderItem item = new OrderItem();

    item.setRetailerUsername("retailer1");

    order.getItems().add(item);

    Mockito.when(orderRepository.findById(1L))
            .thenReturn(Optional.of(order));

    Mockito.when(orderRepository.save(order))
            .thenReturn(order);

    Order result =
            orderService.updateStatus(
                    1L,
                    "DELIVERED",
                    "retailer1"
            );

    assertEquals(
            "DELIVERED",
            result.getStatus()
    );

    assertEquals(
            "DELIVERED",
            order.getStatus()
    );

    Mockito.verify(orderRepository)
            .findById(1L);

    Mockito.verify(orderRepository)
            .save(order);
}

@Test
void testUpdateStatusInvalidTransition() {

    Order order = new Order();

    order.setId(1L);
    order.setUsername("user1");
    order.setStatus("PLACED");

    OrderItem item = new OrderItem();

    item.setRetailerUsername("retailer1");

    order.getItems().add(item);

    Mockito.when(orderRepository.findById(1L))
            .thenReturn(Optional.of(order));

    ResponseStatusException exception =
            assertThrows(
                    ResponseStatusException.class,
                    () -> orderService.updateStatus(
                            1L,
                            "SHIPPED",
                            "retailer1"
                    )
            );

    assertEquals(
            HttpStatus.BAD_REQUEST,
            exception.getStatusCode()
    );

    assertEquals(
            "Invalid status transition: PLACED -> SHIPPED",
            exception.getReason()
    );

    Mockito.verify(orderRepository)
            .findById(1L);

    Mockito.verify(
            orderRepository,
            Mockito.never()
    ).save(Mockito.any(Order.class));
}

@Test
void testMarkReceivedOrderNotFound() {

    Mockito.when(orderRepository.findById(1L))
            .thenReturn(Optional.empty());

    ResponseStatusException exception =
            assertThrows(
                    ResponseStatusException.class,
                    () -> orderService.markReceived(
                            1L,
                            "user1"
                    )
            );

    assertEquals(
            HttpStatus.NOT_FOUND,
            exception.getStatusCode()
    );

    assertEquals(
            "Order not found",
            exception.getReason()
    );

    Mockito.verify(orderRepository)
            .findById(1L);

    Mockito.verify(
            orderRepository,
            Mockito.never()
    ).save(Mockito.any(Order.class));
}

@Test
void testMarkReceivedUnauthorizedUser() {

    Order order = new Order();

    order.setId(1L);
    order.setUsername("user1");
    order.setStatus("DELIVERED");

    Mockito.when(orderRepository.findById(1L))
            .thenReturn(Optional.of(order));

    ResponseStatusException exception =
            assertThrows(
                    ResponseStatusException.class,
                    () -> orderService.markReceived(
                            1L,
                            "user2"
                    )
            );

    assertEquals(
            HttpStatus.FORBIDDEN,
            exception.getStatusCode()
    );

    assertEquals(
            "You can receive only your own order",
            exception.getReason()
    );

    Mockito.verify(orderRepository)
            .findById(1L);

    Mockito.verify(
            orderRepository,
            Mockito.never()
    ).save(Mockito.any(Order.class));
}

@Test
void testMarkReceivedOrderNotDelivered() {

    Order order = new Order();

    order.setId(1L);
    order.setUsername("user1");
    order.setStatus("SHIPPED");

    Mockito.when(orderRepository.findById(1L))
            .thenReturn(Optional.of(order));

    ResponseStatusException exception =
            assertThrows(
                    ResponseStatusException.class,
                    () -> orderService.markReceived(
                            1L,
                            "user1"
                    )
            );

    assertEquals(
            HttpStatus.BAD_REQUEST,
            exception.getStatusCode()
    );

    assertEquals(
            "Order can be marked RECEIVED only after DELIVERED",
            exception.getReason()
    );

    Mockito.verify(orderRepository)
            .findById(1L);

    Mockito.verify(
            orderRepository,
            Mockito.never()
    ).save(Mockito.any(Order.class));
}

@Test
void testMarkReceivedSuccess() {

    Order order = new Order();

    order.setId(1L);
    order.setUsername("user1");
    order.setStatus("DELIVERED");

    Mockito.when(orderRepository.findById(1L))
            .thenReturn(Optional.of(order));

    Mockito.when(orderRepository.save(order))
            .thenReturn(order);

    Order result =
            orderService.markReceived(
                    1L,
                    "user1"
            );

    assertEquals(
            "RECEIVED",
            result.getStatus()
    );

    assertEquals(
            "RECEIVED",
            order.getStatus()
    );

    Mockito.verify(orderRepository)
            .findById(1L);

    Mockito.verify(orderRepository)
            .save(order);
}
}