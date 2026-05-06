package com.sportsstore.sports_api.services;

import com.sportsstore.sports_api.domain.dtos.PageResponse;
import com.sportsstore.sports_api.domain.dtos.ProductRequest;
import com.sportsstore.sports_api.domain.dtos.ProductResponse;
import com.sportsstore.sports_api.domain.entities.Product;
import com.sportsstore.sports_api.domain.enums.Category;
import com.sportsstore.sports_api.exception.ResourceNotFoundException;
import com.sportsstore.sports_api.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {


    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getAllProducts(Pageable pageable) {
        Page<Product> page = productRepository.findAll(pageable);
        return mapToPageResponse(page);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProductsByCategory(Category category, Pageable pageable) {
        Page<Product> page = productRepository.findByCategory(category, pageable);
        return mapToPageResponse(page);
    }

    // Agrega este método a tu servicio
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> searchProducts(String name, Pageable pageable) {
        Page<Product> page = productRepository.searchProductsWithLike(name, pageable);
        return mapToPageResponse(page); // Usa tu helper existente
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        return mapToResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product(request.name(), request.brand(), request.price(), request.stock(), request.category(), request.imageUrl());
        Product saved = productRepository.save(product);
        return mapToResponse(saved);
    }

    private ProductResponse mapToResponse(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getBrand(),
                product.getPrice(), product.getStock(), product.getCategory(),product.getImageUrl());
    }

    // Helper para transformar el Page de Hibernate a nuestro DTO personalizado
    private PageResponse<ProductResponse> mapToPageResponse(Page<Product> page) {
        var content = page.getContent().stream().map(this::mapToResponse).toList();

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
