package com.delivery.routing.domain;

import com.delivery.common.domain.Location;
import com.delivery.common.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderInfo {
    private UUID orderId;
    private UUID restaurantId;
    private Location restaurantLocation;
    private Location deliveryLocation;
    private int preparationTimeMinutes;
    private OrderStatus status;
    private Instant createdAt;
}

