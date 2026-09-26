package com.example.retailer_service.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false)
    private String username;


    @Column(nullable = false)
    private double grandTotal;


    @Column(nullable = false)
    private String status;


    @Column(nullable = false)
    private LocalDateTime createdAt;


    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> items =
            new ArrayList<>();


    public Order() {
    }


    // =====================================================
    // Automatically set date before INSERT
    // =====================================================

    @PrePersist
    public void prePersist() {

        if (createdAt == null) {

            createdAt =
                    LocalDateTime.now();
        }
    }


    // =====================================================
    // GETTERS & SETTERS
    // =====================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public double getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(double grandTotal) {
        this.grandTotal = grandTotal;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {

        this.createdAt = createdAt;
    }


    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(
            List<OrderItem> items) {

        this.items = items;
    }
}