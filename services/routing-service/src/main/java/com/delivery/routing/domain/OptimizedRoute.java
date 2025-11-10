package com.delivery.routing.domain;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
@Builder
public class OptimizedRoute {
    UUID routeId;
    int batchId;
    List<RouteStep> steps;
    double totalDistanceKm;
    double estimatedTimeMinutes;
    RouteMetadata metadata;

    @Value
    @Builder
    public static class RouteMetadata {
        String algorithm;
        long optimizationTimeMs;
        int orderCount;
    }
}

