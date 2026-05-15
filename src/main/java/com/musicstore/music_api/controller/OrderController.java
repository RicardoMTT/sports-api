package com.musicstore.music_api.controller;


import com.musicstore.music_api.domain.dtos.OrderDetailResponse;
import com.musicstore.music_api.domain.dtos.OrderResponse;
import com.musicstore.music_api.domain.dtos.PageResponse;
import com.musicstore.music_api.domain.entities.User;
import com.musicstore.music_api.exception.ResourceNotFoundException;
import com.musicstore.music_api.repository.UserRepository;
import com.musicstore.music_api.services.JwtService;
import com.musicstore.music_api.services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Órdenes", description = "Consulta de historial de compras del usuario autenticado. El usuario se identifica mediante el JWT — no es posible consultar órdenes de otros usuarios.")
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

    @Operation(
        summary = "Mis órdenes",
        description = "Devuelve el historial paginado de órdenes del usuario autenticado, ordenadas de la más reciente a la más antigua"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista paginada de órdenes"),
        @ApiResponse(responseCode = "401", description = "No autenticado — JWT inválido o expirado")
    })
    @GetMapping("/my")
    public ResponseEntity<PageResponse<OrderResponse>> getMyOrders(
            HttpServletRequest request,
            @Parameter(description = "Paginación: page (0-based), size, sort") @PageableDefault(page = 0, size = 10) Pageable pageable) {

        User user = getUserFromToken(request);
        return ResponseEntity.ok(orderService.getMyOrders(user.getId(), pageable));
    }

    @Operation(
        summary = "Detalle de una orden",
        description = "Devuelve la información completa de una orden incluyendo todos sus items. Solo el dueño de la orden puede consultarla."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Detalle completo de la orden"),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada o no pertenece al usuario"),
        @ApiResponse(responseCode = "401", description = "No autenticado — JWT inválido o expirado")
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(
            HttpServletRequest request,
            @Parameter(description = "ID de la orden") @PathVariable Long orderId) {

        User user = getUserFromToken(request);
        return ResponseEntity.ok(orderService.getOrderDetail(user.getId(), orderId));
    }

    @Operation(hidden = true)
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

