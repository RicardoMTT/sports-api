package com.musicstore.music_api.controller;


import com.musicstore.music_api.domain.dtos.OrderDetailResponse;
import com.musicstore.music_api.domain.dtos.OrderResponse;
import com.musicstore.music_api.domain.dtos.PageResponse;
import com.musicstore.music_api.domain.entities.User;
import com.musicstore.music_api.exception.ResourceNotFoundException;
import com.musicstore.music_api.repository.UserRepository;
import com.musicstore.music_api.services.JwtService;
import com.musicstore.music_api.services.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public OrderController(OrderService orderService,
                           UserRepository userRepository,
                           JwtService jwtService) {
        this.orderService = orderService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    // Lista paginada — más reciente primero
    @GetMapping("/my")
    public ResponseEntity<PageResponse<OrderResponse>> getMyOrders(
            HttpServletRequest request,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        User user = getUserFromToken(request);
        return ResponseEntity.ok(orderService.getMyOrders(user.getId(), pageable));
    }

    // Detalle completo con items
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(
            HttpServletRequest request,
            @PathVariable Long orderId) {

        User user = getUserFromToken(request);
        return ResponseEntity.ok(orderService.getOrderDetail(user.getId(), orderId));
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Test");
    }

    /**
     * Extrae el email del JWT en el header Authorization y busca al usuario.
     * El token ya fue validado por JwtAuthenticationFilter antes de llegar aquí,
     * por lo que no es necesario volver a validarlo — solo leer el subject (email).
     */
    private User getUserFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = authHeader.substring(7); // quitar "Bearer "
        String email = jwtService.extractUsername(token);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }
}

