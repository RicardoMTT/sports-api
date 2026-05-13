package com.musicstore.music_api.domain.entities;


import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer quantity;

    public CartItem() {}
    public CartItem(Product product, Integer quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public BigDecimal getSubtotal() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    // Getters y Setters...
    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setCart(Cart cart) { this.cart = cart; }

    public void setId(Long id) {
        this.id = id;
    }

    public Cart getCart() {
        return cart;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}

