package com.sportsstore.sports_api.domain.entities;


import com.sportsstore.sports_api.domain.enums.Category;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product extends Auditable {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String brand;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    private Integer stock;
    @Enumerated(EnumType.STRING)
    private Category category;

    // NUEVO CAMPO: Control de concurrencia optimista
    @Version
    private Long version;

    // NUEVO CAMPO: Guardará la URL de la imagen (ej: https://tucuenta.blob.core.windows.net/imagenes/zapato.jpg)
    @Column(name = "image_url")
    private String imageUrl;

    public Product() {}

    public Product(String brand, String name, BigDecimal price, Integer stock, Category category,
                   String imageUrl) {
        this.brand = brand;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.imageUrl = imageUrl;
    }

    // Getters y Setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPrice() { return price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
