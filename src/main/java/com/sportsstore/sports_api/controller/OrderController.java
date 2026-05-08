package com.sportsstore.sports_api.controller;


import com.sportsstore.sports_api.domain.dtos.OrderDetailResponse;
import com.sportsstore.sports_api.domain.dtos.OrderResponse;
import com.sportsstore.sports_api.domain.dtos.PageResponse;
import com.sportsstore.sports_api.domain.entities.User;
import com.sportsstore.sports_api.services.OrderService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {


    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Lista paginada — más reciente primero
    @GetMapping("/my")
    public ResponseEntity<PageResponse<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal User currentUser,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        return ResponseEntity.ok(orderService.getMyOrders(currentUser.getId(), pageable));
    }

    // Detalle completo con items
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long orderId) {

        return ResponseEntity.ok(orderService.getOrderDetail(currentUser.getId(), orderId));
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Test");
    }
}
