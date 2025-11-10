package com.delivery.common.domain;

public record Location(double latitude, double longitude) {
    private static final double MIN_LATITUDE = -90.0;
    private static final double MAX_LATITUDE = 90.0;
    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;

    public Location {
        validateLatitude(latitude);
        validateLongitude(longitude);
    }

    public static Location of(double lat, double lon) {
        return new Location(lat, lon);
    }

    private static void validateLatitude(double latitude) {
        if (latitude < MIN_LATITUDE || latitude > MAX_LATITUDE) {
            throw new IllegalArgumentException(
                    String.format("Latitude must be between %f and %f, got: %f",
                            MIN_LATITUDE, MAX_LATITUDE, latitude)
            );
        }
    }

    private static void validateLongitude(double longitude) {
        if (longitude < MIN_LONGITUDE || longitude > MAX_LONGITUDE) {
            throw new IllegalArgumentException(
                    String.format("Longitude must be between %f and %f, got: %f",
                            MIN_LONGITUDE, MAX_LONGITUDE, longitude)
            );
        }
    }
}

