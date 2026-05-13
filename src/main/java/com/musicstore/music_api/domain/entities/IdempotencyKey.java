package com.musicstore.music_api.domain.entities;


import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "key_value", unique = true, nullable = false, length = 36)
    private String keyValue;

    @Column(nullable = false)
    private String status; // Puede ser "PROCESSING" o "COMPLETED"

    @Column(name = "order_id")
    private Long orderId; // Guardamos el resultado exitoso aquí

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public IdempotencyKey() {}

    public IdempotencyKey(String keyValue, String status) {
        this.keyValue = keyValue;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public String getKeyValue() { return keyValue; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }


    public void setId(Long id) {
        this.id = id;
    }

    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

