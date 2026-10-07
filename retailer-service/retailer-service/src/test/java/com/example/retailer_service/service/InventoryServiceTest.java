package com.example.retailer_service.service;

import com.example.retailer_service.entity.Inventory;
import com.example.retailer_service.repository.InventoryRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void testAddInventory() {

        // 1. Create Inventory object
        Inventory inventory = new Inventory();

        inventory.setId(100L);
        inventory.setProductName("Laptop");
        inventory.setDescription("Dell Laptop");
        inventory.setQuantity(10);
        inventory.setPrice(50000.0);

        // 2. Mock repository save()
        Mockito.when(inventoryRepository.save(Mockito.any(Inventory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // 3. Call service method
        Inventory result =
                inventoryService.add(inventory, "retailer1");

        // 4. Verify ID is reset
        org.junit.jupiter.api.Assertions.assertNull(result.getId());

        // 5. Verify retailer username
        org.junit.jupiter.api.Assertions.assertEquals(
                "retailer1",
                result.getRetailerUsername()
        );

        // 6. Verify product name
        org.junit.jupiter.api.Assertions.assertEquals(
                "Laptop",
                result.getProductName()
        );

        // 7. Verify repository save() was called
        Mockito.verify(inventoryRepository)
                .save(Mockito.any(Inventory.class));
    }


   @Test
void testGetAllWithoutDateFilter() {

    // 1. Create test inventory
    Inventory inventory = new Inventory();

    inventory.setId(1L);
    inventory.setProductName("Laptop");
    inventory.setDescription("Dell Laptop");
    inventory.setQuantity(10);
    inventory.setPrice(50000.0);
    inventory.setRetailerUsername("retailer1");

    // 2. Create expected list
    java.util.List<Inventory> inventoryList =
            java.util.List.of(inventory);

    // 3. Mock repository findAll()
    Mockito.when(inventoryRepository.findAll())
            .thenReturn(inventoryList);

    // 4. Call service method
    java.util.List<Inventory> result =
            inventoryService.getAll(null, null);

    // 5. Verify result
    org.junit.jupiter.api.Assertions.assertEquals(
            1,
            result.size()
    );

    org.junit.jupiter.api.Assertions.assertEquals(
            "Laptop",
            result.get(0).getProductName()
    );

    // 6. Verify repository method was called
    Mockito.verify(inventoryRepository)
            .findAll();
}

@Test
void testGetAllWithDateFilter() {

    // 1. Create test inventory
    Inventory inventory = new Inventory();

    inventory.setId(2L);
    inventory.setProductName("Phone");
    inventory.setDescription("Samsung Phone");
    inventory.setQuantity(20);
    inventory.setPrice(30000.0);
    inventory.setRetailerUsername("retailer1");

    // 2. Create expected list
    java.util.List<Inventory> inventoryList =
            java.util.List.of(inventory);

    // 3. Define date range
    java.time.LocalDate from =
            java.time.LocalDate.of(2026, 1, 1);

    java.time.LocalDate to =
            java.time.LocalDate.of(2026, 1, 31);

    // 4. Mock repository method
    Mockito.when(
            inventoryRepository.findByCreatedAtBetween(
                    Mockito.any(java.time.LocalDateTime.class),
                    Mockito.any(java.time.LocalDateTime.class)
            )
    ).thenReturn(inventoryList);

    // 5. Call service method
    java.util.List<Inventory> result =
            inventoryService.getAll(from, to);

    // 6. Verify result
    org.junit.jupiter.api.Assertions.assertEquals(
            1,
            result.size()
    );

    org.junit.jupiter.api.Assertions.assertEquals(
            "Phone",
            result.get(0).getProductName()
    );

    // 7. Verify repository method was called
    Mockito.verify(inventoryRepository)
            .findByCreatedAtBetween(
                    java.time.LocalDateTime.of(2026, 1, 1, 0, 0),
                    java.time.LocalDateTime.of(2026, 2, 1, 0, 0)
                            .minusNanos(1)
            );
}

@Test
void testGetAllWithInvalidDateRange() {

    // From date is after To date
    java.time.LocalDate from =
            java.time.LocalDate.of(2026, 2, 1);

    java.time.LocalDate to =
            java.time.LocalDate.of(2026, 1, 1);

    // Verify exception
    org.junit.jupiter.api.Assertions.assertThrows(
            RuntimeException.class,
            () -> inventoryService.getAll(from, to)
    );

    // Repository should NOT be called
    Mockito.verify(
            inventoryRepository,
            Mockito.never()
    ).findByCreatedAtBetween(
            Mockito.any(java.time.LocalDateTime.class),
            Mockito.any(java.time.LocalDateTime.class)
    );
}

@Test
void testGetByIdSuccess() {

    // 1. Create inventory
    Inventory inventory = new Inventory();

    inventory.setId(10L);
    inventory.setProductName("Keyboard");
    inventory.setDescription("Wireless Keyboard");
    inventory.setQuantity(15);
    inventory.setPrice(1500.0);
    inventory.setRetailerUsername("retailer1");

    // 2. Mock repository findById()
    Mockito.when(inventoryRepository.findById(10L))
            .thenReturn(java.util.Optional.of(inventory));

    // 3. Call service method
    Inventory result =
            inventoryService.getById(10L);

    // 4. Verify result
    org.junit.jupiter.api.Assertions.assertNotNull(result);

    org.junit.jupiter.api.Assertions.assertEquals(
            10L,
            result.getId()
    );

    org.junit.jupiter.api.Assertions.assertEquals(
            "Keyboard",
            result.getProductName()
    );

    // 5. Verify repository was called
    Mockito.verify(inventoryRepository)
            .findById(10L);
}

@Test
void testGetByIdNotFound() {

    // 1. Mock repository: ID does not exist
    Mockito.when(inventoryRepository.findById(999L))
            .thenReturn(java.util.Optional.empty());

    // 2. Verify exception
    org.junit.jupiter.api.Assertions.assertThrows(
            RuntimeException.class,
            () -> inventoryService.getById(999L)
    );

    // 3. Verify repository was called
    Mockito.verify(inventoryRepository)
            .findById(999L);
}

@Test
void testUpdateSuccess() {

    // 1. Existing inventory
    Inventory existing = new Inventory();

    existing.setId(10L);
    existing.setProductName("Old Laptop");
    existing.setDescription("Old Description");
    existing.setQuantity(5);
    existing.setPrice(40000.0);
    existing.setRetailerUsername("retailer1");

    // 2. Updated inventory data
    Inventory updated = new Inventory();

    updated.setProductName("New Laptop");
    updated.setDescription("New Description");
    updated.setQuantity(20);
    updated.setPrice(55000.0);

    // 3. Mock getById() -> find existing inventory
    Mockito.when(inventoryRepository.findById(10L))
            .thenReturn(java.util.Optional.of(existing));

    // 4. Mock save()
    Mockito.when(inventoryRepository.save(Mockito.any(Inventory.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    // 5. Call update()
    Inventory result =
            inventoryService.update(
                    10L,
                    updated,
                    "retailer1"
            );

    // 6. Verify updated values
    org.junit.jupiter.api.Assertions.assertEquals(
            "New Laptop",
            result.getProductName()
    );

    org.junit.jupiter.api.Assertions.assertEquals(
            "New Description",
            result.getDescription()
    );

    org.junit.jupiter.api.Assertions.assertEquals(
            20,
            result.getQuantity()
    );

    org.junit.jupiter.api.Assertions.assertEquals(
            55000.0,
            result.getPrice()
    );

    // 7. Retailer username should remain same
    org.junit.jupiter.api.Assertions.assertEquals(
            "retailer1",
            result.getRetailerUsername()
    );

    // 8. Verify save()
    Mockito.verify(inventoryRepository)
            .save(existing);
}

@Test
void testUpdateUnauthorizedRetailer() {

    // 1. Existing inventory belongs to retailer1
    Inventory existing = new Inventory();

    existing.setId(20L);
    existing.setProductName("Laptop");
    existing.setDescription("Laptop Description");
    existing.setQuantity(10);
    existing.setPrice(50000.0);
    existing.setRetailerUsername("retailer1");

    // 2. Updated inventory
    Inventory updated = new Inventory();

    updated.setProductName("Updated Laptop");
    updated.setDescription("Updated Description");
    updated.setQuantity(20);
    updated.setPrice(60000.0);

    // 3. Mock existing inventory
    Mockito.when(inventoryRepository.findById(20L))
            .thenReturn(java.util.Optional.of(existing));

    // 4. retailer2 tries to update retailer1's inventory
    org.junit.jupiter.api.Assertions.assertThrows(
            RuntimeException.class,
            () -> inventoryService.update(
                    20L,
                    updated,
                    "retailer2"
            )
    );

    // 5. Verify save() was NOT called
    Mockito.verify(
            inventoryRepository,
            Mockito.never()
    ).save(Mockito.any(Inventory.class));
}

@Test
void testDeleteSuccess() {

    // 1. Existing inventory
    Inventory existing = new Inventory();

    existing.setId(30L);
    existing.setProductName("Mouse");
    existing.setDescription("Wireless Mouse");
    existing.setQuantity(10);
    existing.setPrice(1000.0);
    existing.setRetailerUsername("retailer1");

    // 2. Mock findById()
    Mockito.when(inventoryRepository.findById(30L))
            .thenReturn(java.util.Optional.of(existing));

    // 3. retailer1 deletes own inventory
    inventoryService.delete(
            30L,
            "retailer1"
    );

    // 4. Verify delete() was called
    Mockito.verify(inventoryRepository)
            .delete(existing);
}

@Test
void testDeleteUnauthorizedRetailer() {

    // 1. Existing inventory belongs to retailer1
    Inventory existing = new Inventory();

    existing.setId(40L);
    existing.setProductName("Monitor");
    existing.setDescription("LED Monitor");
    existing.setQuantity(5);
    existing.setPrice(15000.0);
    existing.setRetailerUsername("retailer1");

    // 2. Mock findById()
    Mockito.when(inventoryRepository.findById(40L))
            .thenReturn(java.util.Optional.of(existing));

    // 3. retailer2 tries to delete retailer1's inventory
    org.junit.jupiter.api.Assertions.assertThrows(
            RuntimeException.class,
            () -> inventoryService.delete(
                    40L,
                    "retailer2"
            )
    );

    // 4. Verify delete() was NOT called
    Mockito.verify(
            inventoryRepository,
            Mockito.never()
    ).delete(Mockito.any(Inventory.class));
}
}