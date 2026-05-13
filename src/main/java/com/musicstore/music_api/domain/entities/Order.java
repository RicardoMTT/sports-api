package com.musicstore.music_api.domain.entities;


import com.musicstore.music_api.domain.enums.OrderStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();


    // Constructores, Getters y Setters omitidos por brevedad...
    public void setUserId(Long userId) { this.userId = userId; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public void addItem(OrderItem item) {
        this.items.add(item);
        item.setOrder(this);
    }

    public Long getId() { return id; }


    public void setId(Long id) {
        this.id = id;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Long getUserId() {
        return userId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}

