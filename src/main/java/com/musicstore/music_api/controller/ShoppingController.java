package com.musicstore.music_api.controller;


import com.musicstore.music_api.domain.dtos.*;
import com.musicstore.music_api.domain.entities.Cart;
import com.musicstore.music_api.domain.entities.IdempotencyKey;
import com.musicstore.music_api.domain.entities.Order;
import com.musicstore.music_api.domain.entities.User;
import com.musicstore.music_api.services.IdempotencyService;
import com.musicstore.music_api.services.ShoppingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Carrito & Checkout", description = "Gestión del carrito de compras y procesamiento de órdenes. Todos los endpoints requieren autenticación.")
@RestController
@RequestMapping("/api/v1/shopping")
public class ShoppingController {

    private final ShoppingService shoppingService;
    private final IdempotencyService idempotencyService;
    public ShoppingController(ShoppingService shoppingService,
                              IdempotencyService idempotencyService) {
        this.shoppingService = shoppingService;
        this.idempotencyService = idempotencyService;
    }

    @Operation(summary = "Agregar item al carrito", description = "Si el producto ya existe en el carrito, incrementa la cantidad")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item agregado — devuelve el carrito actualizado"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PostMapping("/cart/items")
    public ResponseEntity<CartResponse> addItem(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody AddToCartRequest request) {

        Cart cart = shoppingService.addToCart(currentUser.getId(), request);
        return ResponseEntity.ok(mapToResponse(cart));
    }

    @Operation(
        summary = "Confirmar checkout",
        description = "Procesa el carrito activo como una orden. Requiere el header `Idempotency-Key` para evitar dobles cobros. Generá un UUID único por intento de pago."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Orden creada exitosamente"),
        @ApiResponse(responseCode = "409", description = "Stock insuficiente para algún producto"),
        @ApiResponse(responseCode = "400", description = "El carrito está vacío"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(
                @AuthenticationPrincipal User currentUser,
                @Parameter(description = "Clave única por intento de pago — evita doble procesamiento", required = true)
                @RequestHeader(value = "Idempotency-Key",required = true) String idempotencyKey) {

        // 1. Verificamos la llave
        IdempotencyKey keyRecord = idempotencyService.createOrReturnLock(idempotencyKey);

        // 2. Si la llave ya había terminado antes, devolvemos el resultado en caché (Idempotencia en acción)
        if ("COMPLETED".equals(keyRecord.getStatus())) {
            return ResponseEntity.ok(new OrderResponse(keyRecord.getOrderId(), "PAID (Cached)", null));
        }
        // 3. Procesamos el checkout real (solo llega aquí si la llave estaba nueva)
        Order order = shoppingService.processCheckout(currentUser.getId());

        // 4. Marcamos la llave como completada y guardamos el resultado
        idempotencyService.markAsCompleted(idempotencyKey, order.getId());

        return ResponseEntity.ok(new OrderResponse(order.getId(), order.getStatus().name(), order.getTotalAmount()));
    }

    private CartResponse mapToResponse(Cart cart) {
        var itemsDto = cart.getItems().stream()
                .map(item -> new CartItemDto(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getSubtotal()
                )).toList();

        return new CartResponse(cart.getId(), itemsDto, cart.getTotalPrice());
    }


    @Operation(summary = "Eliminar item del carrito", description = "Quita completamente un producto del carrito, sin importar la cantidad")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item eliminado — devuelve el carrito actualizado"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado en el carrito"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @DeleteMapping("/cart/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "ID del producto a eliminar") @PathVariable Long productId) {

        Cart cart = shoppingService.removeItem(currentUser.getId(), productId);
        return ResponseEntity.ok(mapToResponse(cart));
    }

    @Operation(summary = "Actualizar cantidad de un item", description = "Reemplaza la cantidad actual del producto en el carrito con el nuevo valor")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cantidad actualizada — devuelve el carrito actualizado"),
        @ApiResponse(responseCode = "400", description = "Cantidad inválida (debe ser mayor a 0)"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado en el carrito"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PutMapping("/cart/items/{productId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "ID del producto a actualizar") @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request) {

        Cart cart = shoppingService.updateItemQuantity(
                currentUser.getId(), productId, request.quantity()
        );
        return ResponseEntity.ok(mapToResponse(cart));
    }

    @Operation(summary = "Vaciar el carrito", description = "Elimina todos los items del carrito activo del usuario")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Carrito vaciado exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @DeleteMapping("/cart")
    public ResponseEntity<Void> clearCart(
            @AuthenticationPrincipal User currentUser) {

        shoppingService.clearCart(currentUser.getId());
        return ResponseEntity.noContent().build();
    }

}

