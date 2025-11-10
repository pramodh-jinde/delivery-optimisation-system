package com.delivery.routing.algorithm;

import com.delivery.routing.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class TSPOptimizer {
    private final MinimumSpanningTreeBuilder mstBuilder;
    private final TwoOptOptimizer twoOptOptimizer;

    private static final int TWO_OPT_MAX_ITERATIONS = 100;

    public OptimizedRoute solveExact(
            DistanceMatrix matrix,
            Map<String, TimeWindow> timeWindows,
            DeliveryBatch batch
    ) {
        long startTime = System.currentTimeMillis();
        int n = matrix.getLocations().size();

        log.info("Solving exact TSP for {} locations", n);

        double[][] dp = new double[1 << n][n];
        int[][] parent = new int[1 << n][n];

        initializeDPArrays(dp, parent);

        dp[1][0] = 0;

        performDynamicProgramming(dp, parent, matrix, n);

        int finalMask = (1 << n) - 1;
        int bestEnd = findBestEndNode(dp, finalMask, n);

        List<Integer> path = reconstructPath(parent, finalMask, bestEnd);
        path = enforcePickupDeliveryConstraints(path, batch);

        List<RouteStep> steps = convertToRouteSteps(path, matrix, batch);

        long optimizationTime = System.currentTimeMillis() - startTime;

        return buildOptimizedRoute(
                batch,
                steps,
                path,
                matrix,
                "EXACT_DP",
                optimizationTime
        );
    }

    public OptimizedRoute solveHeuristic(
            DistanceMatrix matrix,
            Map<String, TimeWindow> timeWindows,
            DeliveryBatch batch
    ) {
        long startTime = System.currentTimeMillis();
        int n = matrix.getLocations().size();

        log.info("Solving heuristic TSP for {} locations", n);

        List<Edge> mst = mstBuilder.buildMST(matrix);
        Set<Integer> oddVertices = findOddDegreeVertices(mst, n);
        List<Edge> matching = minimumWeightPerfectMatching(oddVertices, matrix);

        List<Edge> eulerianGraph = new ArrayList<>(mst);
        eulerianGraph.addAll(matching);

        List<Integer> tour = findEulerianTour(eulerianGraph, n);
        List<Integer> hamiltonianTour = convertToHamiltonian(tour);

        hamiltonianTour = twoOptOptimizer.optimize(hamiltonianTour, matrix, TWO_OPT_MAX_ITERATIONS);
        hamiltonianTour = enforcePickupDeliveryConstraints(hamiltonianTour, batch);

        List<RouteStep> steps = convertToRouteSteps(hamiltonianTour, matrix, batch);

        long optimizationTime = System.currentTimeMillis() - startTime;

        return buildOptimizedRoute(
                batch,
                steps,
                hamiltonianTour,
                matrix,
                "CHRISTOFIDES",
                optimizationTime
        );
    }

    private void initializeDPArrays(double[][] dp, int[][] parent) {
        for (double[] row : dp) {
            Arrays.fill(row, Double.MAX_VALUE);
        }
        for (int[] row : parent) {
            Arrays.fill(row, -1);
        }
    }

    private void performDynamicProgramming(double[][] dp, int[][] parent, DistanceMatrix matrix, int n) {
        for (int mask = 1; mask < (1 << n); mask++) {
            for (int last = 0; last < n; last++) {
                if ((mask & (1 << last)) == 0 || dp[mask][last] == Double.MAX_VALUE) {
                    continue;
                }

                for (int next = 0; next < n; next++) {
                    if ((mask & (1 << next)) != 0) {
                        continue;
                    }

                    int newMask = mask | (1 << next);
                    double newCost = dp[mask][last] + matrix.getTime(last, next);

                    if (newCost < dp[newMask][next]) {
                        dp[newMask][next] = newCost;
                        parent[newMask][next] = last;
                    }
                }
            }
        }
    }

    private int findBestEndNode(double[][] dp, int finalMask, int n) {
        double minCost = Double.MAX_VALUE;
        int bestEnd = -1;

        for (int i = 0; i < n; i++) {
            if (dp[finalMask][i] < minCost) {
                minCost = dp[finalMask][i];
                bestEnd = i;
            }
        }

        return bestEnd;
    }

    private Set<Integer> findOddDegreeVertices(List<Edge> mst, int n) {
        int[] degree = new int[n];
        for (Edge edge : mst) {
            degree[edge.getFrom()]++;
            degree[edge.getTo()]++;
        }

        Set<Integer> oddVertices = new HashSet<>();
        for (int i = 0; i < n; i++) {
            if (degree[i] % 2 == 1) {
                oddVertices.add(i);
            }
        }
        return oddVertices;
    }

    private List<Edge> minimumWeightPerfectMatching(Set<Integer> oddVertices, DistanceMatrix matrix) {
        List<Edge> matching = new ArrayList<>();
        List<Integer> vertices = new ArrayList<>(oddVertices);
        boolean[] matched = new boolean[matrix.getLocations().size()];

        for (int i = 0; i < vertices.size(); i++) {
            if (matched[vertices.get(i)]) {
                continue;
            }

            int bestMatch = findBestMatch(vertices, matched, i, matrix);

            if (bestMatch != -1) {
                int v1 = vertices.get(i);
                int v2 = vertices.get(bestMatch);
                matching.add(new Edge(v1, v2, matrix.getDistance(v1, v2)));
                matched[v1] = true;
                matched[v2] = true;
            }
        }

        return matching;
    }

    private int findBestMatch(List<Integer> vertices, boolean[] matched, int currentIndex, DistanceMatrix matrix) {
        int bestMatch = -1;
        double bestWeight = Double.MAX_VALUE;

        for (int j = currentIndex + 1; j < vertices.size(); j++) {
            int v2 = vertices.get(j);
            if (matched[v2]) {
                continue;
            }

            double weight = matrix.getDistance(vertices.get(currentIndex), v2);
            if (weight < bestWeight) {
                bestWeight = weight;
                bestMatch = j;
            }
        }

        return bestMatch;
    }

    private List<Integer> findEulerianTour(List<Edge> edges, int n) {
        Map<Integer, List<Integer>> adj = buildAdjacencyList(edges);
        List<Integer> tour = new ArrayList<>();
        dfsEulerian(0, adj, tour);
        return tour;
    }

    private Map<Integer, List<Integer>> buildAdjacencyList(List<Edge> edges) {
        Map<Integer, List<Integer>> adj = new HashMap<>();
        for (Edge edge : edges) {
            adj.computeIfAbsent(edge.getFrom(), k -> new ArrayList<>()).add(edge.getTo());
            adj.computeIfAbsent(edge.getTo(), k -> new ArrayList<>()).add(edge.getFrom());
        }
        return adj;
    }

    private void dfsEulerian(int v, Map<Integer, List<Integer>> adj, List<Integer> tour) {
        List<Integer> neighbors = adj.get(v);
        if (neighbors != null) {
            while (!neighbors.isEmpty()) {
                int next = neighbors.remove(0);
                adj.get(next).removeIf(u -> u == v);
                dfsEulerian(next, adj, tour);
            }
        }
        tour.add(v);
    }

    private List<Integer> convertToHamiltonian(List<Integer> eulerianTour) {
        List<Integer> hamiltonian = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();

        for (int vertex : eulerianTour) {
            if (!visited.contains(vertex)) {
                hamiltonian.add(vertex);
                visited.add(vertex);
            }
        }

        return hamiltonian;
    }

    private List<Integer> enforcePickupDeliveryConstraints(List<Integer> tour, DeliveryBatch batch) {
        List<Integer> constrainedTour = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();

        constrainedTour.add(tour.get(0));
        visited.add(tour.get(0));

        while (constrainedTour.size() < tour.size()) {
            Integer nextLocation = findNextValidLocation(tour, visited);

            if (nextLocation == null) {
                break;
            }

            constrainedTour.add(nextLocation);
            visited.add(nextLocation);
        }

        return constrainedTour;
    }

    private Integer findNextValidLocation(List<Integer> tour, Set<Integer> visited) {
        for (int location : tour) {
            if (visited.contains(location)) {
                continue;
            }

            if (canVisit(location, visited)) {
                return location;
            }
        }

        for (int location : tour) {
            if (visited.contains(location)) {
                continue;
            }

            if (isDeliveryLocation(location) && !visited.contains(location - 1)) {
                return location - 1;
            }
            return location;
        }

        return null;
    }

    private boolean canVisit(int location, Set<Integer> visited) {
        if (location == 0) {
            return true;
        }

        if (location % 2 == 1) {
            return true;
        }

        int pickupLocation = location - 1;
        return visited.contains(pickupLocation);
    }

    private boolean isDeliveryLocation(int location) {
        return location > 0 && location % 2 == 0;
    }

    private List<Integer> reconstructPath(int[][] parent, int mask, int end) {
        List<Integer> path = new ArrayList<>();
        int current = end;
        int currentMask = mask;

        while (current != -1) {
            path.add(0, current);
            int prevMask = currentMask ^ (1 << current);
            if (prevMask == 0) {
                break;
            }
            current = parent[currentMask][current];
            currentMask = prevMask;
        }

        return path;
    }

    private List<RouteStep> convertToRouteSteps(
            List<Integer> path,
            DistanceMatrix matrix,
            DeliveryBatch batch
    ) {
        List<RouteStep> steps = new ArrayList<>();
        double cumulativeTime = 0;

        for (int i = 0; i < path.size(); i++) {
            int current = path.get(i);
            double distanceFromPrevious = 0;
            double timeFromPrevious = 0;

            if (i > 0) {
                int previous = path.get(i - 1);
                distanceFromPrevious = matrix.getDistance(previous, current);
                timeFromPrevious = matrix.getTime(previous, current);
                cumulativeTime += timeFromPrevious;
            }

            DistanceMatrix.LocationMetadata metadata = matrix.getLocationMetadata().get(current);

            steps.add(buildRouteStep(
                    i,
                    current,
                    matrix,
                    metadata,
                    distanceFromPrevious,
                    timeFromPrevious,
                    cumulativeTime
            ));
        }

        return steps;
    }

    private RouteStep buildRouteStep(
            int index,
            int locationIndex,
            DistanceMatrix matrix,
            DistanceMatrix.LocationMetadata metadata,
            double distanceFromPrevious,
            double timeFromPrevious,
            double cumulativeTime
    ) {
        RouteStep.RouteStepBuilder builder = RouteStep.builder()
                .sequence(index + 1)
                .locationId(metadata != null ? metadata.getId() : null)
                .location(matrix.getLocations().get(locationIndex))
                .distanceFromPreviousKm(distanceFromPrevious)
                .timeFromPreviousMinutes(timeFromPrevious)
                .estimatedArrivalTimeMinutes(cumulativeTime);

        if (metadata != null) {
            builder.type(metadata.getType());
            builder.instructions(generateInstructions(metadata));
        } else {
            builder.type(RouteStep.LocationType.RESTAURANT_PICKUP);
            builder.instructions("Unknown location");
        }

        return builder.build();
    }

    private String generateInstructions(DistanceMatrix.LocationMetadata metadata) {
        return switch (metadata.getType()) {
            case EXECUTIVE_START -> "Start from executive location";
            case RESTAURANT_PICKUP -> "Pick up order from restaurant " + metadata.getId();
            case CUSTOMER_DELIVERY -> "Deliver order " + metadata.getId();
        };
    }

    private double calculateTotalDistance(List<Integer> path, DistanceMatrix matrix) {
        double total = 0;
        for (int i = 1; i < path.size(); i++) {
            total += matrix.getDistance(path.get(i - 1), path.get(i));
        }
        return total;
    }

    private double calculateTotalTime(List<Integer> path, DistanceMatrix matrix) {
        double total = 0;
        for (int i = 1; i < path.size(); i++) {
            total += matrix.getTime(path.get(i - 1), path.get(i));
        }
        return total;
    }

    private OptimizedRoute buildOptimizedRoute(
            DeliveryBatch batch,
            List<RouteStep> steps,
            List<Integer> path,
            DistanceMatrix matrix,
            String algorithm,
            long optimizationTime
    ) {
        return OptimizedRoute.builder()
                .routeId(UUID.randomUUID())
                .batchId(batch.getId())
                .steps(steps)
                .totalDistanceKm(calculateTotalDistance(path, matrix))
                .estimatedTimeMinutes(calculateTotalTime(path, matrix))
                .metadata(OptimizedRoute.RouteMetadata.builder()
                        .algorithm(algorithm)
                        .optimizationTimeMs(optimizationTime)
                        .orderCount(batch.getOrderCount())
                        .build())
                .build();
    }
}
