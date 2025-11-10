package com.delivery.routing.domain;

import com.delivery.common.domain.Location;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Value
@Builder
public class DistanceMatrix {
    List<Location> locations;
    double[][] distances;
    double[][] times;
    Map<Integer, LocationMetadata> locationMetadata;

    public double getDistance(int from, int to) {
        return distances[from][to];
    }

    public double getTime(int from, int to) {
        return times[from][to];
    }

    @Value
    @Builder
    public static class LocationMetadata {
        UUID id;
        RouteStep.LocationType type;
    }
}

