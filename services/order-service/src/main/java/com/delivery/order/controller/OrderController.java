package com.delivery.order.controller;

import com.delivery.order.domain.Order;
import com.delivery.order.dto.AssignBatchRequest;
import com.delivery.order.dto.AssignBatchResponse;
import com.delivery.order.dto.CreateOrderRequest;
import com.delivery.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final OrderService orderService;
    private final GeometryFactory geometryFactory;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("Creating order for customer: {}", request.getCustomerId());

        Point deliveryLocation = geometryFactory.createPoint(
                new Coordinate(request.getDeliveryLocation().getLongitude(),
                        request.getDeliveryLocation().getLatitude())
        );

        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .restaurantId(request.getRestaurantId())
                .deliveryLocation(deliveryLocation)
                .preparationTimeMinutes(request.getPreparationTimeMinutes())
                .build();

        Order created = orderService.createOrder(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/assign-batch")
    public ResponseEntity<AssignBatchResponse> assignBatch(@Valid @RequestBody AssignBatchRequest request) {
        log.info("Assigning batch {} to executive: {}", request.getBatchId(), request.getExecutiveId());

        int assignedCount = orderService.assignOrdersToExecutive(
                request.getOrderIds(),
                request.getExecutiveId(),
                request.getBatchId()
        );

        return ResponseEntity.ok(new AssignBatchResponse(
                assignedCount,
                "Orders assigned successfully"
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable UUID id) {
        log.debug("Fetching order: {}", id);
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }
}

