package com.delivery.routing.service;

import com.delivery.routing.algorithm.DistanceMatrixBuilder;
import com.delivery.routing.algorithm.TSPOptimizer;
import com.delivery.routing.domain.*;
import com.delivery.routing.exception.InvalidBatchException;
import com.delivery.routing.exception.RouteOptimizationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Service
@Slf4j
@RequiredArgsConstructor
public class RouteOptimizationService {
    private final TSPOptimizer tspOptimizer;
    private final DistanceMatrixBuilder distanceMatrixBuilder;
    private final ExecutorService routingExecutorService;

    @Value("${routing.optimization.exact-algorithm-threshold:10}")
    private int exactAlgorithmThreshold;

    @Value("${routing.optimization.max-batch-size:10}")
    private int maxBatchSize;

    private static final int PICKUP_GRACE_PERIOD_MINUTES = 10;
    private static final int DELIVERY_SLA_MINUTES = 40;
    private static final int PICKUP_SERVICE_TIME_MINUTES = 2;
    private static final int DELIVERY_SERVICE_TIME_MINUTES = 3;

    public OptimizedRoute findOptimalRoute(DeliveryBatch batch, ExecutiveLocation executiveLocation) {
        validateBatchAndLocation(batch, executiveLocation);

        log.info("Starting route optimization for batch: {}", batch.getId());

        try {
            CompletableFuture<DistanceMatrix> distanceMatrixFuture =
                    CompletableFuture.supplyAsync(
                            () -> distanceMatrixBuilder.build(batch, executiveLocation),
                            routingExecutorService
                    );

            CompletableFuture<Map<String, TimeWindow>> timeWindowsFuture =
                    CompletableFuture.supplyAsync(
                            () -> calculateTimeWindows(batch),
                            routingExecutorService
                    );

            CompletableFuture.allOf(distanceMatrixFuture, timeWindowsFuture).join();

            DistanceMatrix distanceMatrix = distanceMatrixFuture.join();
            Map<String, TimeWindow> timeWindows = timeWindowsFuture.join();

            return selectAndApplyOptimizationAlgorithm(batch, distanceMatrix, timeWindows);
        } catch (Exception e) {
            log.error("Route optimization failed for batch: {}", batch.getId(), e);
            throw new RouteOptimizationException("Failed to optimize route for batch: " + batch.getId(), e);
        }
    }

    private OptimizedRoute selectAndApplyOptimizationAlgorithm(
            DeliveryBatch batch,
            DistanceMatrix distanceMatrix,
            Map<String, TimeWindow> timeWindows
    ) {
        int orderCount = batch.getOrders().size();

        if (orderCount <= exactAlgorithmThreshold) {
            log.info("Using exact DP algorithm for {} orders", orderCount);
            return tspOptimizer.solveExact(distanceMatrix, timeWindows, batch);
        } else {
            log.info("Using Christofides heuristic for {} orders", orderCount);
            return tspOptimizer.solveHeuristic(distanceMatrix, timeWindows, batch);
        }
    }

    private Map<String, TimeWindow> calculateTimeWindows(DeliveryBatch batch) {
        Map<String, TimeWindow> timeWindows = new HashMap<>();

        batch.getOrders().forEach(order -> {
            String restaurantKey = "R_" + order.getOrderId();
            String deliveryKey = "C_" + order.getOrderId();

            timeWindows.put(restaurantKey, TimeWindow.builder()
                    .earliest(0)
                    .latest(order.getPreparationTimeMinutes() + PICKUP_GRACE_PERIOD_MINUTES)
                    .serviceTime(PICKUP_SERVICE_TIME_MINUTES)
                    .build());

            timeWindows.put(deliveryKey, TimeWindow.builder()
                    .earliest(order.getPreparationTimeMinutes())
                    .latest(order.getPreparationTimeMinutes() + DELIVERY_SLA_MINUTES)
                    .serviceTime(DELIVERY_SERVICE_TIME_MINUTES)
                    .build());
        });

        return timeWindows;
    }

    private void validateBatchAndLocation(DeliveryBatch batch, ExecutiveLocation executiveLocation) {
        if (batch == null) {
            throw new InvalidBatchException("Batch cannot be null");
        }
        if (batch.getOrders() == null || batch.getOrders().isEmpty()) {
            throw new InvalidBatchException("Batch must contain at least one order");
        }
        if (batch.getOrders().size() > maxBatchSize) {
            throw new InvalidBatchException("Batch size exceeds maximum allowed: " + maxBatchSize);
        }
        if (executiveLocation == null) {
            throw new InvalidBatchException("Executive location cannot be null");
        }
        if (executiveLocation.getLocation() == null) {
            throw new InvalidBatchException("Executive location coordinates cannot be null");
        }
    }
}

