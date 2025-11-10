package com.delivery.order.domain;

import com.delivery.common.domain.OrderStatus;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_status", columnList = "status"),
        @Index(name = "idx_order_created_at", columnList = "createdAt"),
        @Index(name = "idx_order_executive", columnList = "assignedExecutiveId")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private UUID restaurantId;

    @Column(columnDefinition = "geometry(Point,4326)", nullable = false)
    @JsonIgnore
    private Point deliveryLocation;

    @JsonGetter("deliveryLocation")
    public Map<String, Double> getDeliveryLocationJson() {
        if (deliveryLocation == null) {
            return null;
        }
        return Map.of(
                "latitude", deliveryLocation.getY(),
                "longitude", deliveryLocation.getX()
        );
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private Integer preparationTimeMinutes;

    private UUID assignedExecutiveId;
    private Integer batchId;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant assignedAt;
    private Instant pickedUpAt;
    private Instant deliveredAt;

    @Version
    private Long version;
}

