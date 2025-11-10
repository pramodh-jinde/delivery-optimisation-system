package com.delivery.order.repository;

import com.delivery.common.domain.OrderStatus;
import com.delivery.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    
    List<Order> findByStatus(OrderStatus status);
    
    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, Instant timestamp);
    
    @Modifying
    @Query("UPDATE Order o SET o.assignedExecutiveId = :execId, " +
           "o.batchId = :batchId, o.assignedAt = :assignedAt, o.status = 'ASSIGNED' " +
           "WHERE o.id IN :orderIds AND o.status = 'PENDING'")
    int assignOrders(
            @Param("orderIds") List<UUID> orderIds,
            @Param("execId") UUID executiveId,
            @Param("batchId") Integer batchId,
            @Param("assignedAt") Instant assignedAt
    );
}

