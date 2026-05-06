package com.sportsstore.sports_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportsstore.sports_api.domain.dtos.PageResponse;
import com.sportsstore.sports_api.domain.dtos.ProductRequest;
import com.sportsstore.sports_api.domain.dtos.ProductResponse;
import com.sportsstore.sports_api.domain.enums.Category;
import com.sportsstore.sports_api.services.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController Tests")
class ProductControllerTest {

    // Usamos el @Mock para simular el ProductService
    @Mock
    private ProductService productService;

    // Usamos el @InjectMocks para inyectar el ProductService en el ProductController
    @InjectMocks
    private ProductController productController;

    // Usamos el MockMvc para simular las peticiones HTTP
    private MockMvc mockMvc;

    // Usamos el ObjectMapper para convertir los objetos a JSON
    private ObjectMapper objectMapper;

    private ProductResponse sampleProductResponse;
    private ProductRequest sampleProductRequest;
    private PageResponse<ProductResponse> samplePageResponse;

    @BeforeEach
    void setUp() {
        // Usamos el MockMvc para simular las peticiones HTTP
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();

        sampleProductResponse = new ProductResponse(
                1L,
                "Running Shoes",
                "Nike",
                new BigDecimal("99.99"),
                50,
                Category.FOOTWEAR,
                "http://example.com/shoes.jpg"
        );

        sampleProductRequest = new ProductRequest(
                "Running Shoes",
                "Nike",
                new BigDecimal("99.99"),
                50,
                Category.FOOTWEAR,
                "http://example.com/shoes.jpg"
        );

        samplePageResponse = new PageResponse<>(
                List.of(sampleProductResponse),
                0,
                6,
                1,
                1
        );
    }

    // Arrange
    // Act
    // Assert

    @Test
    @DisplayName("GET /api/v1/products - Should return all products when no filters provided")
    void getAllProducts_NoFilters_ShouldReturnAllProducts() throws Exception {
        // Usamos el when para simular el comportamiento del ProductService
        when(productService.getAllProducts(any(Pageable.class))).thenReturn(samplePageResponse);

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].name", is("Running Shoes")))
                .andExpect(jsonPath("$.content[0].brand", is("Nike")))
                .andExpect(jsonPath("$.content[0].price", is(99.99)))
                .andExpect(jsonPath("$.content[0].stock", is(50)))
                .andExpect(jsonPath("$.content[0].category", is("FOOTWEAR")))
                .andExpect(jsonPath("$.pageNumber", is(0)))
                .andExpect(jsonPath("$.pageSize", is(6)))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)));

        verify(productService, times(1)).getAllProducts(any(Pageable.class));
        verify(productService, never()).searchProducts(anyString(), any(Pageable.class));
        verify(productService, never()).getProductsByCategory(any(Category.class), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/v1/products?name=Running - Should return filtered products by name")
    void getAllProducts_WithNameFilter_ShouldReturnFilteredProducts() throws Exception {
        when(productService.searchProducts(eq("Running"), any(Pageable.class))).thenReturn(samplePageResponse);

        mockMvc.perform(get("/api/v1/products")
                        .param("name", "Running"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Running Shoes")));

        verify(productService, times(1)).searchProducts(eq("Running"), any(Pageable.class));
        verify(productService, never()).getAllProducts(any(Pageable.class));
        verify(productService, never()).getProductsByCategory(any(Category.class), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/v1/products?category=FOOTWEAR - Should return filtered products by category")
    void getAllProducts_WithCategoryFilter_ShouldReturnFilteredProducts() throws Exception {
        when(productService.getProductsByCategory(eq(Category.FOOTWEAR), any(Pageable.class)))
                .thenReturn(samplePageResponse);

        mockMvc.perform(get("/api/v1/products")
                        .param("category", "FOOTWEAR"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].category", is("FOOTWEAR")));

        verify(productService, times(1)).getProductsByCategory(eq(Category.FOOTWEAR), any(Pageable.class));
        verify(productService, never()).getAllProducts(any(Pageable.class));
        verify(productService, never()).searchProducts(anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/v1/products?name=&category=FOOTWEAR - Should prioritize category filter when name is blank")
    void getAllProducts_WithBlankNameAndCategory_ShouldUseCategoryFilter() throws Exception {
        when(productService.getProductsByCategory(eq(Category.FOOTWEAR), any(Pageable.class)))
                .thenReturn(samplePageResponse);

        mockMvc.perform(get("/api/v1/products")
                        .param("name", "")
                        .param("category", "FOOTWEAR"))
                .andExpect(status().isOk());

        verify(productService, times(1)).getProductsByCategory(eq(Category.FOOTWEAR), any(Pageable.class));
        verify(productService, never()).searchProducts(anyString(), any(Pageable.class));
        verify(productService, never()).getAllProducts(any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/v1/products/category/FOOTWEAR - Should return products by category")
    void getProductsByCategory_ShouldReturnProductsByCategory() throws Exception {
        when(productService.getProductsByCategory(eq(Category.FOOTWEAR), any(Pageable.class)))
                .thenReturn(samplePageResponse);

        mockMvc.perform(get("/api/v1/products/category/FOOTWEAR"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].category", is("FOOTWEAR")));

        verify(productService, times(1)).getProductsByCategory(eq(Category.FOOTWEAR), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/v1/products/category/CLOTHING - Should return clothing products")
    void getProductsByCategory_Clothing_ShouldReturnClothingProducts() throws Exception {
        PageResponse<ProductResponse> clothingPage = new PageResponse<>(
                List.of(new ProductResponse(2L, "T-Shirt", "Adidas", new BigDecimal("29.99"), 100, Category.CLOTHING, null)),
                0,
                10,
                1,
                1
        );

        when(productService.getProductsByCategory(eq(Category.CLOTHING), any(Pageable.class)))
                .thenReturn(clothingPage);

        mockMvc.perform(get("/api/v1/products/category/CLOTHING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].category", is("CLOTHING")));

        verify(productService, times(1)).getProductsByCategory(eq(Category.CLOTHING), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/v1/products/1 - Should return product by ID")
    void getProductById_ShouldReturnProduct() throws Exception {
        when(productService.getProductById(1L)).thenReturn(sampleProductResponse);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Running Shoes")))
                .andExpect(jsonPath("$.brand", is("Nike")))
                .andExpect(jsonPath("$.price", is(99.99)))
                .andExpect(jsonPath("$.stock", is(50)))
                .andExpect(jsonPath("$.category", is("FOOTWEAR")));

        verify(productService, times(1)).getProductById(1L);
    }

    @Test
    @DisplayName("POST /api/v1/products - Should create new product")
    void createProduct_ValidRequest_ShouldCreateProduct() throws Exception {
        when(productService.createProduct(any(ProductRequest.class))).thenReturn(sampleProductResponse);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleProductRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Running Shoes")))
                .andExpect(jsonPath("$.brand", is("Nike")))
                .andExpect(jsonPath("$.price", is(99.99)))
                .andExpect(jsonPath("$.stock", is(50)))
                .andExpect(jsonPath("$.category", is("FOOTWEAR")));

        verify(productService, times(1)).createProduct(any(ProductRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/products - Should return 400 when request is invalid")
    void createProduct_InvalidRequest_ShouldReturnBadRequest() throws Exception {
        ProductRequest invalidRequest = new ProductRequest(
                "",  // Invalid: blank name
                "Nike",
                new BigDecimal("99.99"),
                50,
                Category.FOOTWEAR,
                "http://example.com/shoes.jpg"
        );

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(any(ProductRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/products - Should return 400 when price is negative")
    void createProduct_NegativePrice_ShouldReturnBadRequest() throws Exception {
        ProductRequest invalidRequest = new ProductRequest(
                "Running Shoes",
                "Nike",
                new BigDecimal("-10.00"),  // Invalid: negative price
                50,
                Category.FOOTWEAR,
                "http://example.com/shoes.jpg"
        );

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(any(ProductRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/products - Should return 400 when stock is negative")
    void createProduct_NegativeStock_ShouldReturnBadRequest() throws Exception {
        ProductRequest invalidRequest = new ProductRequest(
                "Running Shoes",
                "Nike",
                new BigDecimal("99.99"),
                -5,  // Invalid: negative stock
                Category.FOOTWEAR,
                "http://example.com/shoes.jpg"
        );

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(any(ProductRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/products - Should return 400 when category is null")
    void createProduct_NullCategory_ShouldReturnBadRequest() throws Exception {
        ProductRequest invalidRequest = new ProductRequest(
                "Running Shoes",
                "Nike",
                new BigDecimal("99.99"),
                50,
                null,  // Invalid: null category
                "http://example.com/shoes.jpg"
        );

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(any(ProductRequest.class));
    }

    @Test
    @DisplayName("GET /api/v1/products - Should handle pagination parameters")
    void getAllProducts_WithPagination_ShouldUsePagination() throws Exception {
        when(productService.getAllProducts(any(Pageable.class))).thenReturn(samplePageResponse);

        mockMvc.perform(get("/api/v1/products")
                        .param("page", "1")
                        .param("size", "10")
                        .param("sort", "name,desc"))
                .andExpect(status().isOk());

        verify(productService, times(1)).getAllProducts(any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/v1/products/category/FOOTWEAR - Should handle pagination parameters")
    void getProductsByCategory_WithPagination_ShouldUsePagination() throws Exception {
        when(productService.getProductsByCategory(eq(Category.FOOTWEAR), any(Pageable.class)))
                .thenReturn(samplePageResponse);

        mockMvc.perform(get("/api/v1/products/category/FOOTWEAR")
                        .param("page", "2")
                        .param("size", "5")
                        .param("sort", "price,asc"))
                .andExpect(status().isOk());

        verify(productService, times(1)).getProductsByCategory(eq(Category.FOOTWEAR), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/v1/products/test - Should return different test response (INTENTIONALLY FAILING)")
    void testEndpoint_ShouldReturnDifferentResponse() throws Exception {
        mockMvc.perform(get("/api/v1/products/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("Different Test Response")); // This will fail - actual response is "Test"
    }
}
