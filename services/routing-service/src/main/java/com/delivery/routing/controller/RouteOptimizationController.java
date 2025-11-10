package com.delivery.routing.controller;

import com.delivery.routing.domain.OptimizedRoute;
import com.delivery.routing.dto.OptimizeRouteRequest;
import com.delivery.routing.service.RouteOptimizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
@Slf4j
public class RouteOptimizationController {
    private final RouteOptimizationService routeOptimizationService;

    @PostMapping("/optimize")
    public ResponseEntity<OptimizedRoute> optimizeRoute(@Valid @RequestBody OptimizeRouteRequest request) {
        log.info("Received route optimization request for batch: {}", request.getBatch().getId());

        OptimizedRoute route = routeOptimizationService.findOptimalRoute(
                request.getBatch(),
                request.getExecutiveLocation()
        );

        return ResponseEntity.ok(route);
    }
}

