package com.sportsstore.sports_api.repository;

import com.sportsstore.sports_api.domain.entities.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Historial paginado del usuario, más reciente primero
    @Query("SELECT o FROM Order o WHERE o.userId = :userId ORDER BY o.createdAt DESC")
    Page<Order> findMyOrders(@Param("userId") Long userId,Pageable pageable);

    // Detalle de una orden — verifica ownership en la misma query
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product " +
            "WHERE o.id = :orderId AND o.userId = :userId")
    Optional<Order> findByIdAndUserId(@Param("orderId") Long orderId,
                                      @Param("userId") Long userId);

}