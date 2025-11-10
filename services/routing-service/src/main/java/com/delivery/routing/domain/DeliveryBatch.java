package com.delivery.routing.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryBatch {
    private int id;
    private UUID executiveId;
    private List<OrderInfo> orders;
    private BatchStatus status;
    private Instant createdAt;

    public int getOrderCount() {
        return orders != null ? orders.size() : 0;
    }

    public enum BatchStatus {
        PENDING,
        OPTIMIZING,
        OPTIMIZED,
        ASSIGNED,
        IN_PROGRESS,
        COMPLETED
    }
}

