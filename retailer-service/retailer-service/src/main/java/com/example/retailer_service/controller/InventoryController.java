package com.example.retailer_service.controller;

import com.example.retailer_service.entity.Inventory;
import com.example.retailer_service.service.InventoryService;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(
            InventoryService inventoryService) {

        this.inventoryService = inventoryService;
    }

    // ==========================================
    // ADD INVENTORY
    // ==========================================

    @PostMapping
    @PreAuthorize("hasRole('RETAILER')")
    public ResponseEntity<Inventory> add(
            @Valid @RequestBody Inventory inventory,
            Authentication authentication) {

        String username = authentication.getName();

        Inventory saved =
                inventoryService.add(
                        inventory,
                        username
                );

        return ResponseEntity.ok(saved);
    }

    // ==========================================
    // GET ALL INVENTORY
    // ==========================================

    @GetMapping
    public ResponseEntity<List<Inventory>> getAll(

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
                inventoryService.getAll(
                        from,
                        to
                )
        );
    }

    // ==========================================
    // GET INVENTORY BY ID
    // ==========================================

    @GetMapping("/{id}")
    public ResponseEntity<Inventory> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                inventoryService.getById(id)
        );
    }

    // ==========================================
    // UPDATE INVENTORY
    // ==========================================

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RETAILER')")
    public ResponseEntity<Inventory> update(
            @PathVariable Long id,
            @Valid @RequestBody Inventory inventory,
            Authentication authentication) {

        String username =
                authentication.getName();

        return ResponseEntity.ok(
                inventoryService.update(
                        id,
                        inventory,
                        username
                )
        );
    }

    // ==========================================
    // DELETE INVENTORY
    // ==========================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RETAILER')")
    public ResponseEntity<String> delete(
            @PathVariable Long id,
            Authentication authentication) {

        String username =
                authentication.getName();

        inventoryService.delete(
                id,
                username
        );

        return ResponseEntity.ok(
                "Inventory deleted successfully"
        );
    }
}