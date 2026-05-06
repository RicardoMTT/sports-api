package com.sportsstore.sports_api.controller;


import com.sportsstore.sports_api.domain.dtos.*;
import com.sportsstore.sports_api.domain.entities.Cart;
import com.sportsstore.sports_api.domain.entities.IdempotencyKey;
import com.sportsstore.sports_api.domain.entities.Order;
import com.sportsstore.sports_api.domain.entities.User;
import com.sportsstore.sports_api.services.IdempotencyService;
import com.sportsstore.sports_api.services.ShoppingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/cart/items")
    public ResponseEntity<CartResponse> addItem(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody AddToCartRequest request) {

        Cart cart = shoppingService.addToCart(currentUser.getId(), request);
        return ResponseEntity.ok(mapToResponse(cart));
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(
                @AuthenticationPrincipal User currentUser,
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


    @DeleteMapping("/cart/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long productId) {

        Cart cart = shoppingService.removeItem(currentUser.getId(), productId);
        return ResponseEntity.ok(mapToResponse(cart));
    }

    // Actualizar la cantidad de un producto en el carrito
    @PutMapping("/cart/items/{productId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request) {

        Cart cart = shoppingService.updateItemQuantity(
                currentUser.getId(), productId, request.quantity()
        );
        return ResponseEntity.ok(mapToResponse(cart));
    }

    // Vaciar el carrito completo
    @DeleteMapping("/cart")
    public ResponseEntity<Void> clearCart(
            @AuthenticationPrincipal User currentUser) {

        shoppingService.clearCart(currentUser.getId());
        return ResponseEntity.noContent().build();
    }

}
