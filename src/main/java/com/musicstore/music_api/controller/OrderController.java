package com.musicstore.music_api.controller;


import com.musicstore.music_api.domain.dtos.OrderDetailResponse;
import com.musicstore.music_api.domain.dtos.OrderResponse;
import com.musicstore.music_api.domain.dtos.PageResponse;
import com.musicstore.music_api.domain.entities.User;
import com.musicstore.music_api.exception.ResourceNotFoundException;
import com.musicstore.music_api.repository.UserRepository;
import com.musicstore.music_api.services.OrderService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {


    private final OrderService orderService;
    private final UserRepository userRepository;

    public OrderController(OrderService orderService, UserRepository userRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    // Lista paginada — más reciente primero
    @GetMapping("/my")
    public ResponseEntity<PageResponse<OrderResponse>> getMyOrders(
            @RequestParam String email,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
        
        return ResponseEntity.ok(orderService.getMyOrders(user.getId(), pageable));
    }

    // Detalle completo con items
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(
            @RequestParam String email,
            @PathVariable Long orderId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));

        return ResponseEntity.ok(orderService.getOrderDetail(user.getId(), orderId));
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Test");
    }
}

