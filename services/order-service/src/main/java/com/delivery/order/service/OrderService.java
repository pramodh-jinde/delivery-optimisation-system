package com.delivery.order.service;

import com.delivery.common.domain.OrderStatus;
import com.delivery.order.domain.Order;
import com.delivery.order.exception.InvalidOrderException;
import com.delivery.order.exception.OrderNotFoundException;
import com.delivery.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;

    @Transactional
    public Order createOrder(Order order) {
        validateOrder(order);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(Instant.now());
        Order savedOrder = orderRepository.save(order);
        log.info("Created order: {}", savedOrder.getId());
        return savedOrder;
    }

    @Transactional
    public int assignOrdersToExecutive(List<UUID> orderIds, UUID executiveId, Integer batchId) {
        if (orderIds == null || orderIds.isEmpty()) {
            throw new InvalidOrderException("Order IDs list cannot be empty");
        }
        if (executiveId == null) {
            throw new InvalidOrderException("Executive ID cannot be null");
        }
        if (batchId == null) {
            throw new InvalidOrderException("Batch ID cannot be null");
        }

        log.info("Assigning {} orders to executive {} in batch {}", orderIds.size(), executiveId, batchId);

        int assignedCount = orderRepository.assignOrders(orderIds, executiveId, batchId, Instant.now());

        if (assignedCount == 0) {
            log.warn("No orders were assigned. Orders may not exist or are not in PENDING status");
        } else {
            log.info("Successfully assigned {} orders", assignedCount);
        }

        return assignedCount;
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByIds(List<UUID> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return List.of();
        }
        return orderRepository.findAllById(orderIds);
    }

    @Transactional(readOnly = true)
    public Order getOrderById(UUID orderId) {
        if (orderId == null) {
            throw new InvalidOrderException("Order ID cannot be null");
        }
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    private void validateOrder(Order order) {
        if (order == null) {
            throw new InvalidOrderException("Order cannot be null");
        }
        if (order.getCustomerId() == null) {
            throw new InvalidOrderException("Customer ID cannot be null");
        }
        if (order.getRestaurantId() == null) {
            throw new InvalidOrderException("Restaurant ID cannot be null");
        }
        if (order.getDeliveryLocation() == null) {
            throw new InvalidOrderException("Delivery location cannot be null");
        }
        if (order.getPreparationTimeMinutes() == null || order.getPreparationTimeMinutes() < 1) {
            throw new InvalidOrderException("Preparation time must be at least 1 minute");
        }
    }
}

