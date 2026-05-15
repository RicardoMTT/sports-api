package com.musicstore.music_api.controller;


import com.musicstore.music_api.domain.dtos.PageResponse;
import com.musicstore.music_api.domain.dtos.ProductRequest;
import com.musicstore.music_api.domain.dtos.ProductResponse;
import com.musicstore.music_api.domain.enums.Category;
import com.musicstore.music_api.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Productos", description = "Catálogo de productos — vinilos, CDs, ropa, instrumentos, accesorios, posters y libros")
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(
        summary = "Listar productos",
        description = "Devuelve el catálogo paginado. Filtrá por `name` (búsqueda parcial) o por `category` (VINYL, CD, CLOTHING, ACCESSORIES, INSTRUMENTS, POSTERS, BOOKS). Si no se envía ningún parámetro devuelve todos."
    )
    @ApiResponse(responseCode = "200", description = "Lista de productos")
    @SecurityRequirements  // GET de productos es público
    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> getAllProducts(
            @Parameter(description = "Búsqueda parcial por nombre") @RequestParam(required = false) String name,
            @Parameter(description = "Categoría exacta: VINYL | CD | CLOTHING | ACCESSORIES | INSTRUMENTS | POSTERS | BOOKS") @RequestParam(required = false) String category,
            @PageableDefault(page = 0, size = 6, sort = "name") Pageable pageable) {

        if (name != null && !name.isBlank()) {
            return ResponseEntity.ok(productService.searchProducts(name, pageable));
        } else if (category != null && !category.isBlank()) {
            Category parsed = parseCategory(category);
            if (parsed == null) {
                // Categoría desconocida → lista vacía
                return ResponseEntity.ok(new PageResponse<>(List.of(), pageable.getPageNumber(), pageable.getPageSize(), 0, 0));
            }
            return ResponseEntity.ok(productService.getProductsByCategory(parsed, pageable));
        } else {
            return ResponseEntity.ok(productService.getAllProducts(pageable));
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<PageResponse<ProductResponse>> getProductsByCategory(
            @PathVariable String category,
            @PageableDefault(page = 0, size = 10, sort = "price") Pageable pageable) {
        Category parsed = parseCategory(category);
        if (parsed == null) {
            return ResponseEntity.ok(new PageResponse<>(List.of(), pageable.getPageNumber(), pageable.getPageSize(), 0, 0));
        }
        return ResponseEntity.ok(productService.getProductsByCategory(parsed, pageable));
    }

    /** Convierte un String a Category. Retorna null si el valor no existe en el enum. */
    private Category parseCategory(String value) {
        try {
            return Category.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Operation(summary = "Obtener producto por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto encontrado"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @SecurityRequirements  // público
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "ID del producto") @PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @Operation(summary = "Crear producto", description = "Solo accesible para usuarios con rol ADMIN")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Producto creado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "Se requiere rol ADMIN")
    })
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    @Operation(hidden = true)
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Test");
    }
}

