package com.delivery.routing.algorithm;

import com.delivery.routing.domain.DistanceMatrix;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class TwoOptOptimizer {

    private static final double IMPROVEMENT_THRESHOLD = -0.001;

    public List<Integer> optimize(List<Integer> tour, DistanceMatrix matrix, int maxIterations) {
        List<Integer> bestTour = new ArrayList<>(tour);
        boolean improved = true;
        int iteration = 0;

        while (improved && iteration < maxIterations) {
            improved = performTwoOptIteration(bestTour, matrix);
            iteration++;
        }

        return bestTour;
    }

    private boolean performTwoOptIteration(List<Integer> tour, DistanceMatrix matrix) {
        boolean improved = false;

        for (int i = 1; i < tour.size() - 2; i++) {
            for (int j = i + 1; j < tour.size() - 1; j++) {
                double delta = calculate2OptDelta(tour, i, j, matrix);

                if (delta < IMPROVEMENT_THRESHOLD) {
                    reverse(tour, i + 1, j);
                    improved = true;
                }
            }
        }

        return improved;
    }

    private double calculate2OptDelta(List<Integer> tour, int i, int j, DistanceMatrix matrix) {
        int a = tour.get(i);
        int b = tour.get(i + 1);
        int c = tour.get(j);
        int d = tour.get(j + 1);

        double currentCost = matrix.getDistance(a, b) + matrix.getDistance(c, d);
        double newCost = matrix.getDistance(a, c) + matrix.getDistance(b, d);

        return newCost - currentCost;
    }

    private void reverse(List<Integer> tour, int start, int end) {
        while (start < end) {
            Collections.swap(tour, start, end);
            start++;
            end--;
        }
    }
}

