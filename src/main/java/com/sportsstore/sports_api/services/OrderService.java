package com.sportsstore.sports_api.services;

import com.sportsstore.sports_api.domain.dtos.OrderDetailResponse;
import com.sportsstore.sports_api.domain.dtos.OrderItemResponse;
import com.sportsstore.sports_api.domain.dtos.OrderResponse;
import com.sportsstore.sports_api.domain.dtos.PageResponse;
import com.sportsstore.sports_api.domain.entities.Order;
import com.sportsstore.sports_api.domain.entities.OrderItem;
import com.sportsstore.sports_api.exception.ResourceNotFoundException;
import com.sportsstore.sports_api.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {


    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getMyOrders(Long userId, Pageable pageable) {
        Page<Order> page = orderRepository.findMyOrders(userId, pageable);

        Page<OrderResponse> mapped = page.map(order -> new OrderResponse(
                order.getId(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getCreatedAt()
        ));

        return new PageResponse<>(
                mapped.getContent(),
                mapped.getNumber(),
                mapped.getSize(),
                mapped.getTotalElements(),
                mapped.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Orden no encontrada o no pertenece al usuario"
                ));

        List<OrderItemResponse> items = order.getItems().stream()
                .map(this::mapItem)
                .toList();

        return new OrderDetailResponse(
                order.getId(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                items
        );
    }

    private OrderItemResponse mapItem(OrderItem item) {
        return new OrderItemResponse(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getBrand(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }
}
