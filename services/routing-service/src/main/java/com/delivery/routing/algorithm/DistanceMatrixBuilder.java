package com.delivery.routing.algorithm;

import com.delivery.common.domain.Location;
import com.delivery.routing.domain.DeliveryBatch;
import com.delivery.routing.domain.DistanceMatrix;
import com.delivery.routing.domain.ExecutiveLocation;
import com.delivery.routing.domain.RouteStep;
import com.delivery.routing.util.GeoCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class DistanceMatrixBuilder {
    private final GeoCalculator geoCalculator;

    private static final double AVERAGE_SPEED_KM_HR = 20.0;
    private static final int MINUTES_PER_HOUR = 60;

    public DistanceMatrix build(DeliveryBatch batch, ExecutiveLocation executiveLocation) {
        List<Location> allLocations = new ArrayList<>();
        Map<Integer, DistanceMatrix.LocationMetadata> metadata = new HashMap<>();

        addExecutiveLocation(allLocations, metadata, executiveLocation);
        addOrderLocations(allLocations, metadata, batch);

        return DistanceMatrix.builder()
                .locations(allLocations)
                .distances(buildDistanceMatrix(allLocations))
                .times(buildTimeMatrix(allLocations))
                .locationMetadata(metadata)
                .build();
    }

    private void addExecutiveLocation(
            List<Location> locations,
            Map<Integer, DistanceMatrix.LocationMetadata> metadata,
            ExecutiveLocation executiveLocation
    ) {
        locations.add(executiveLocation.toLocation());
        metadata.put(0, DistanceMatrix.LocationMetadata.builder()
                .id(executiveLocation.getExecutiveId())
                .type(RouteStep.LocationType.EXECUTIVE_START)
                .build());
    }

    private void addOrderLocations(
            List<Location> locations,
            Map<Integer, DistanceMatrix.LocationMetadata> metadata,
            DeliveryBatch batch
    ) {
        int index = 1;
        for (var order : batch.getOrders()) {
            locations.add(order.getRestaurantLocation());
            metadata.put(index++, DistanceMatrix.LocationMetadata.builder()
                    .id(order.getRestaurantId())
                    .type(RouteStep.LocationType.RESTAURANT_PICKUP)
                    .build());

            locations.add(order.getDeliveryLocation());
            metadata.put(index++, DistanceMatrix.LocationMetadata.builder()
                    .id(order.getOrderId())
                    .type(RouteStep.LocationType.CUSTOMER_DELIVERY)
                    .build());
        }
    }

    private double[][] buildDistanceMatrix(List<Location> locations) {
        int n = locations.size();
        double[][] distances = new double[n][n];

        IntStream.range(0, n).parallel().forEach(i -> {
            for (int j = i + 1; j < n; j++) {
                double dist = geoCalculator.haversineDistance(locations.get(i), locations.get(j));
                distances[i][j] = distances[j][i] = dist;
            }
        });

        return distances;
    }

    private double[][] buildTimeMatrix(List<Location> locations) {
        int n = locations.size();
        double[][] times = new double[n][n];

        IntStream.range(0, n).parallel().forEach(i -> {
            for (int j = i + 1; j < n; j++) {
                double dist = geoCalculator.haversineDistance(locations.get(i), locations.get(j));
                double time = (dist / AVERAGE_SPEED_KM_HR) * MINUTES_PER_HOUR;
                times[i][j] = times[j][i] = time;
            }
        });

        return times;
    }
}

