package com.sportsstore.sports_api.services;

import com.sportsstore.sports_api.domain.dtos.AddToCartRequest;
import com.sportsstore.sports_api.domain.entities.*;
import com.sportsstore.sports_api.domain.enums.OrderStatus;
import com.sportsstore.sports_api.domain.event.OrderCompletedEvent;
import com.sportsstore.sports_api.exception.InsufficientStockException;
import com.sportsstore.sports_api.exception.ResourceNotFoundException;
import com.sportsstore.sports_api.repository.CartRepository;
import com.sportsstore.sports_api.repository.OrderRepository;
import com.sportsstore.sports_api.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
public class ShoppingService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    public ShoppingService(CartRepository cartRepository, ProductRepository productRepository,
                           OrderRepository orderRepository, AuditService auditService,
                           ApplicationEventPublisher eventPublisher) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.auditService = auditService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Cart addToCart(Long userId, AddToCartRequest request) {
        // ANTES: new RuntimeException("Producto no encontrado")
        // AHORA: ResourceNotFoundException → GlobalExceptionHandler lo captura como 404
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto no encontrado con id: " + request.productId()
                ));

        Cart cart = cartRepository.findByUserId(userId).orElse(new Cart(userId));

        cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .findFirst()
                .ifPresentOrElse(
                        item -> item.setQuantity(item.getQuantity() + request.quantity()),
                        () -> cart.addItem(new CartItem(product, request.quantity()))
                );

        cart.recalculateTotal();
        return cartRepository.save(cart);
    }

    @Transactional
    public Order processCheckout(Long userId) {
        // ANTES: new RuntimeException("Carrito no encontrado")
        // AHORA: ResourceNotFoundException → GlobalExceptionHandler lo captura como 404
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Carrito no encontrado para el usuario: " + userId
                ));

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("El carrito está vacío");  // ya lo captura el handler como 400
        }

        try {
            Order order = new Order();
            order.setUserId(userId);
            order.setStatus(OrderStatus.PAID);
            order.setTotalAmount(cart.getTotalPrice());

            for (CartItem item : cart.getItems()) {
                Product product = item.getProduct();

                // ANTES: throw new RuntimeException("Stock insuficiente para: " + product.getName())
                // AHORA: InsufficientStockException → GlobalExceptionHandler lo captura como 409
                if (product.getStock() < item.getQuantity()) {
                    throw new InsufficientStockException(
                            "Stock insuficiente para '" + product.getName() + "'. " +
                                    "Disponible: " + product.getStock() + ", " +
                                    "solicitado: " + item.getQuantity()
                    );
                }

                product.setStock(product.getStock() - item.getQuantity());
                productRepository.save(product);

                order.addItem(new OrderItem(product, item.getQuantity(), product.getPrice()));
            }

            Order savedOrder = orderRepository.save(order);

            cart.getItems().clear();
            cart.recalculateTotal();
            cartRepository.save(cart);

            eventPublisher.publishEvent(new OrderCompletedEvent(savedOrder.getId(), userId));

            return savedOrder;

        } catch (ObjectOptimisticLockingFailureException e) {
            throw e;
        } catch (Exception e) {
            auditService.logFailure("CHECKOUT_FAILED", "User: " + userId + " - Error: " + e.getMessage());
            throw e;
        }
    }

    @Transactional
    public Cart removeItem(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado"));

        boolean removed = cart.getItems()
                .removeIf(item -> item.getProduct().getId().equals(productId));

        if (!removed) {
            throw new ResourceNotFoundException("El producto no está en el carrito");
        }

        cart.recalculateTotal();
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart updateItemQuantity(Long userId, Long productId, Integer newQuantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado"));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("El producto no está en el carrito"));

        if (item.getProduct().getStock() < newQuantity) {
            throw new InsufficientStockException(
                    "Stock insuficiente. Disponible: " + item.getProduct().getStock()
            );
        }

        item.setQuantity(newQuantity);
        cart.recalculateTotal();
        return cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado"));

        cart.getItems().clear();
        cart.recalculateTotal();
        cartRepository.save(cart);
    }
}
