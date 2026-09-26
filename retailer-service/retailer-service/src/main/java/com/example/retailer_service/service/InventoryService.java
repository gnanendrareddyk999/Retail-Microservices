package com.example.retailer_service.service;

import com.example.retailer_service.entity.Inventory;
import com.example.retailer_service.repository.InventoryRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(
            InventoryRepository inventoryRepository) {

        this.inventoryRepository =
                inventoryRepository;
    }

    // ==========================================
    // ADD INVENTORY
    // ==========================================

    public Inventory add(
            Inventory inventory,
            String retailerUsername) {

        inventory.setId(null);

        inventory.setRetailerUsername(
                retailerUsername
        );

        return inventoryRepository.save(
                inventory
        );
    }

    // ==========================================
    // GET ALL INVENTORY
    // WITH DATE FILTER
    // ==========================================

    public List<Inventory> getAll(
            LocalDate from,
            LocalDate to) {

        // No date filter
        if (from == null && to == null) {

            return inventoryRepository.findAll();
        }

        // If from is null
        LocalDate startDate =
                from != null
                        ? from
                        : LocalDate.of(2000, 1, 1);

        // If to is null
        LocalDate endDate =
                to != null
                        ? to
                        : LocalDate.now();

        // Validate dates
        if (startDate.isAfter(endDate)) {

            throw new RuntimeException(
                    "From date cannot be after To date"
            );
        }

        // Start of from date
        LocalDateTime start =
                startDate.atStartOfDay();

        // End of to date
        LocalDateTime end =
                endDate
                        .plusDays(1)
                        .atStartOfDay()
                        .minusNanos(1);

        return inventoryRepository
                .findByCreatedAtBetween(
                        start,
                        end
                );
    }

    // ==========================================
    // GET BY ID
    // ==========================================

    public Inventory getById(Long id) {

        return inventoryRepository
                .findById(id)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Inventory not found"
                        )
                );
    }

    // ==========================================
    // UPDATE
    // ==========================================

    public Inventory update(
            Long id,
            Inventory updatedInventory,
            String retailerUsername) {

        Inventory existing =
                getById(id);

        if (!existing.getRetailerUsername()
                .equals(retailerUsername)) {

            throw new RuntimeException(
                    "You can update only your own inventory"
            );
        }

        existing.setProductName(
                updatedInventory.getProductName()
        );

        existing.setDescription(
                updatedInventory.getDescription()
        );

        existing.setPrice(
                updatedInventory.getPrice()
        );

        existing.setQuantity(
                updatedInventory.getQuantity()
        );

        return inventoryRepository.save(
                existing
        );
    }

    // ==========================================
    // DELETE
    // ==========================================

    public void delete(
            Long id,
            String retailerUsername) {

        Inventory existing =
                getById(id);

        if (!existing.getRetailerUsername()
                .equals(retailerUsername)) {

            throw new RuntimeException(
                    "You can delete only your own inventory"
            );
        }

        inventoryRepository.delete(
                existing
        );
    }
}