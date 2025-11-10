package com.delivery.routing.util;

import com.delivery.common.domain.Location;
import org.springframework.stereotype.Component;

@Component
public class GeoCalculator {

    public double haversineDistance(Location from, Location to) {
        return com.delivery.common.util.GeoCalculator.haversineDistance(from, to);
    }

    public double calculateTravelTime(Location from, Location to, double speedKmHr) {
        return com.delivery.common.util.GeoCalculator.calculateTravelTime(from, to, speedKmHr);
    }

    public boolean isWithinRadius(Location center, Location location, double radiusKm) {
        return com.delivery.common.util.GeoCalculator.isWithinRadius(center, location, radiusKm);
    }
}

