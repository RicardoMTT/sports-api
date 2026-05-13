package com.musicstore.music_api.controller;


import com.musicstore.music_api.domain.dtos.PageResponse;
import com.musicstore.music_api.domain.dtos.ProductRequest;
import com.musicstore.music_api.domain.dtos.ProductResponse;
import com.musicstore.music_api.domain.enums.Category;
import com.musicstore.music_api.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> getAllProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Category category,
            // PageableDefault establece valores por defecto si el cliente no los envía
            @PageableDefault(page = 0, size = 6, sort = "name") Pageable pageable) {

        if (name != null && !name.isBlank()) {
            return ResponseEntity.ok(productService.searchProducts(name, pageable));
        } else if (category != null) {
            return ResponseEntity.ok(productService.getProductsByCategory(category, pageable));
        } else {
            return ResponseEntity.ok(productService.getAllProducts(pageable));
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<PageResponse<ProductResponse>> getProductsByCategory(
            @PathVariable Category category,
            @PageableDefault(page = 0, size = 10, sort = "price") Pageable pageable) {
        return ResponseEntity.ok(productService.getProductsByCategory(category, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // Endpoint para administradores
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }


    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Test");
    }
}

