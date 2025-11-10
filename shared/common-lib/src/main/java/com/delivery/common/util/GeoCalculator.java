package com.delivery.common.util;

import com.delivery.common.domain.Location;

public final class GeoCalculator {
    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double DEFAULT_SPEED_KM_HR = 20.0;
    private static final int MINUTES_PER_HOUR = 60;

    private GeoCalculator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static double haversineDistance(Location from, Location to) {
        validateLocation(from);
        validateLocation(to);

        double lat1Rad = Math.toRadians(from.latitude());
        double lat2Rad = Math.toRadians(to.latitude());
        double deltaLat = Math.toRadians(to.latitude() - from.latitude());
        double deltaLon = Math.toRadians(to.longitude() - from.longitude());

        double a = calculateHaversineA(lat1Rad, lat2Rad, deltaLat, deltaLon);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    public static double calculateTravelTime(Location from, Location to, double speedKmHr) {
        if (speedKmHr <= 0) {
            throw new IllegalArgumentException("Speed must be positive");
        }
        double distance = haversineDistance(from, to);
        return (distance / speedKmHr) * MINUTES_PER_HOUR;
    }

    public static double calculateTravelTime(Location from, Location to) {
        return calculateTravelTime(from, to, DEFAULT_SPEED_KM_HR);
    }

    public static boolean isWithinRadius(Location center, Location location, double radiusKm) {
        if (radiusKm < 0) {
            throw new IllegalArgumentException("Radius cannot be negative");
        }
        return haversineDistance(center, location) <= radiusKm;
    }

    private static double calculateHaversineA(
            double lat1Rad,
            double lat2Rad,
            double deltaLat,
            double deltaLon
    ) {
        double sinDeltaLat = Math.sin(deltaLat / 2);
        double sinDeltaLon = Math.sin(deltaLon / 2);
        return sinDeltaLat * sinDeltaLat +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) * sinDeltaLon * sinDeltaLon;
    }

    private static void validateLocation(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }
    }
}

