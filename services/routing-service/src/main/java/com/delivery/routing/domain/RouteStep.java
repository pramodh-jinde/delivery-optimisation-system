package com.delivery.routing.domain;

import com.delivery.common.domain.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteStep {
    private int sequence;
    private UUID locationId;
    private LocationType type;
    private Location location;
    private double distanceFromPreviousKm;
    private double timeFromPreviousMinutes;
    private double estimatedArrivalTimeMinutes;
    private String instructions;

    public enum LocationType {
        EXECUTIVE_START,
        RESTAURANT_PICKUP,
        CUSTOMER_DELIVERY
    }
}

