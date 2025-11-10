package com.delivery.routing.exception;

public class RouteOptimizationException extends RuntimeException {
    public RouteOptimizationException(String message) {
        super(message);
    }

    public RouteOptimizationException(String message, Throwable cause) {
        super(message, cause);
    }
}

